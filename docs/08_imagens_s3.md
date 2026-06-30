# 08 - Imagens no S3

Este fluxo usa S3 como storage de objetos e o banco apenas como referencia. A API grava a chave S3 em `tutors.img_url` e devolve `img_url` como URL de exibicao.

## Estado atual

Implementado para foto de perfil de tutor:

```http
POST /api/tutors/me/profile-image
DELETE /api/tutors/me/profile-image
GET /api/tutors/me
```

## Componentes

O fluxo envolve:

- app cliente: envia JWT interno e arquivo multipart para a API;
- API Spring Boot: valida autenticacao, valida arquivo, grava no S3 e atualiza o banco;
- PostgreSQL: guarda apenas a referencia da imagem;
- S3: guarda o objeto binario;
- IAM task role: autoriza a task do ECS a acessar o bucket;
- URL assinada: permite leitura temporaria da imagem sem tornar o bucket publico.

Classes principais:

- `S3Configuration`: cria `S3Client` e `S3Presigner`;
- `ImageStorageService`: valida imagem, faz `PutObject`, `DeleteObject` e gera URL de leitura;
- `TutorsService`: liga a imagem enviada ao tutor autenticado;
- `TutorsController`: expoe os endpoints HTTP.

## Variaveis

```bash
export POCAPI_IMAGES_BUCKET=pocapi-images-964775859627-us-east-1
export AWS_REGION=us-east-1
export POCAPI_IMAGES_PRESIGNED_URL_TTL=PT15M
export POCAPI_IMAGES_MAX_SIZE_BYTES=5242880
export POCAPI_IMAGES_MAX_FILE_SIZE=5MB
export POCAPI_IMAGES_MAX_REQUEST_SIZE=6MB
```

Em ECS, `POCAPI_IMAGES_BUCKET` vem do SSM:

```text
/pocapi/prod/s3/images-bucket
```

Opcionalmente, se no futuro houver CloudFront ou bucket publico controlado, configure:

```bash
export POCAPI_IMAGES_PUBLIC_BASE_URL=https://cdn.exemplo.com
```

Quando `POCAPI_IMAGES_PUBLIC_BASE_URL` esta vazio, a API gera URL assinada do S3.

## Permissoes AWS

A task do ECS usa `taskRoleArn`:

```text
arn:aws:iam::964775859627:role/pocapi-ecs-task-role
```

Essa role tem policy inline `pocapi-ecs-task-s3-policy` com:

- `s3:PutObject`;
- `s3:GetObject`;
- `s3:DeleteObject`;
- `s3:ListBucket` restrito ao prefixo `uploads/*`.

O recurso autorizado e:

```text
arn:aws:s3:::pocapi-images-964775859627-us-east-1/uploads/*
```

Por isso as chaves geradas pela API sempre comecam com `uploads/`.

## Fluxo de upload

Endpoint:

```http
POST /api/tutors/me/profile-image
```

Passo a passo:

1. O app envia `Authorization: Bearer <token>` e `multipart/form-data` com campo `file`.
2. Spring Security valida o JWT interno e exige `ROLE_TUTOR`.
3. O controller extrai `users.id` de `Jwt.getSubject()`.
4. `TutorsService` busca o perfil em `tutors` por `user_id`.
5. Se o tutor nao existir, retorna `404`.
6. `ImageStorageService` valida o arquivo:
   - arquivo obrigatorio;
   - tamanho maximo padrao de 5 MB;
   - `content-type` permitido: `image/jpeg`, `image/png`, `image/webp`.
7. A API gera uma chave S3 neste formato:

```text
uploads/tutors/{tutorId}/profile/{uuid}.{ext}
```

8. A API chama `s3Client.putObject(...)` no bucket `POCAPI_IMAGES_BUCKET`.
9. A API grava a chave S3 em `tutors.img_url`.
10. A resposta passa pelo resolvedor de imagem e retorna `img_url` como URL pronta para exibicao.

O banco nao recebe o binario da imagem. Ele guarda apenas a chave, por exemplo:

```text
uploads/tutors/2b3.../profile/8f2...png
```

## Fluxo de leitura

Endpoint:

```http
GET /api/tutors/me
```

Passo a passo:

1. O app chama o endpoint autenticado normalmente.
2. A API busca o tutor.
3. Se `tutors.img_url` estiver vazio, retorna `img_url: null`.
4. Se `tutors.img_url` comecar com `http://` ou `https://`, retorna o valor como esta para manter compatibilidade com dados antigos.
5. Se `tutors.img_url` for uma chave S3 e `POCAPI_IMAGES_PUBLIC_BASE_URL` estiver configurado, a API monta a URL publica usando esse base URL.
6. Se `tutors.img_url` for uma chave S3 e nao houver base URL publica, a API usa `S3Presigner` para gerar uma URL assinada temporaria.

O TTL padrao da URL assinada e `PT15M`. Quando a URL expirar, o app deve chamar `GET /api/tutors/me` novamente para receber uma URL nova.

## Fluxo de delecao

Endpoint:

```http
DELETE /api/tutors/me/profile-image
```

Passo a passo:

1. A API busca o tutor autenticado.
2. Se nao houver imagem, retorna o tutor sem alteracao.
3. A API limpa `tutors.img_url`.
4. Se o valor antigo era uma URL externa, a API nao tenta apagar nada no S3.
5. Se o valor antigo era uma chave S3, a API chama `s3Client.deleteObject(...)`.
6. A resposta retorna `img_url: null`.

## Contrato com o app

O app nunca acessa AWS diretamente e nao precisa de credenciais S3.

Para exibir a imagem:

1. Chame `GET /api/tutors/me`.
2. Use o campo `img_url` retornado.
3. Se a imagem parar de carregar por expiracao da URL, chame `GET /api/tutors/me` de novo.

Para enviar a imagem:

1. Garanta que o perfil de tutor ja existe.
2. Envie o arquivo em `POST /api/tutors/me/profile-image`.
3. Atualize a UI com o `img_url` retornado.

Nao envie foto nova pelo campo `img_url` do `PUT /api/tutors/me`. Esse campo ainda existe no payload por compatibilidade, mas o caminho correto para novas imagens e o endpoint multipart.

## Passo a passo para testar foto de perfil

1. Garanta que o usuario tem JWT interno com `role=tutor`.
2. Crie o perfil se ainda nao existir:

```bash
curl -i -sS \
  -X PUT http://localhost:8080/api/tutors/me \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Nome Teste",
    "username": "nometeste",
    "is_private": false,
    "img_url": null,
    "birthday": "2000-01-01T00:00:00"
  }'
```

3. Envie uma imagem:

```bash
curl -i -sS \
  -X POST http://localhost:8080/api/tutors/me/profile-image \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/caminho/avatar.png"
```

4. Use o campo `img_url` da resposta para exibir a imagem no app.
5. Quando a URL expirar, chame `GET /api/tutors/me` de novo e use a nova `img_url`.

## Decisao de arquitetura

O bucket deve permanecer privado. O app nao precisa conhecer credenciais AWS. Ele chama a API, e a API devolve uma URL temporaria para leitura.

Para posts, evite guardar somente `img_url` como texto solto. O desenho recomendado para a tabela `images` e:

- `id`
- `post_id`
- `s3_key`
- `content_type`
- `byte_size`
- `width`
- `height`
- `position`
- `created_at`

Assim o mesmo `ImageStorageService` pode gerar URLs assinadas para fotos de perfil, pets e imagens de posts sem duplicar regra de bucket.
