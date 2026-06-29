# 05 - Fluxo de tutors

O fluxo de tutor permite que o usuario autenticado crie, atualize e consulte seu perfil de tutor.

Base:

```http
/api/tutors
```

Endpoints:

```http
GET /api/tutors/me
PUT /api/tutors/me
POST /api/tutors/me/profile-image
DELETE /api/tutors/me/profile-image
```

Todos exigem access token JWT interno com `role=tutor`.

## GET /api/tutors/me

Fluxo:

1. Spring Security valida o JWT interno.
2. O claim `role=tutor` vira `ROLE_TUTOR`.
3. O controller extrai `users.id` de `Jwt.getSubject()`.
4. O service busca `tutors` por `user_id`.
5. Se nao existir tutor, retorna `404`.
6. Se existir, retorna `TutorsResponse`.

Quando `tutors.img_url` guarda uma chave S3, a resposta converte esse valor para uma URL assinada de exibicao. Se o valor for uma URL `http://` ou `https://`, a API devolve como esta, para manter compatibilidade com dados antigos.

`404` aqui nao significa sessao invalida. Significa apenas que o usuario ainda nao criou perfil de tutor.

## PUT /api/tutors/me

Payload:

```json
{
  "name": "Nome Teste",
  "username": "usernameteste",
  "is_private": true,
  "img_url": "https://example.com/avatar.png",
  "birthday": "2003-10-31T00:00:00"
}
```

Fluxo:

1. Spring Security valida o JWT interno e a role.
2. O controller extrai `users.id`.
3. O service confirma que `users.id` existe.
4. O service normaliza `name`, `username` e `img_url`.
5. O service rejeita `birthday` no futuro.
6. O service valida duplicidade de username.
7. Se nao existir tutor, cria.
8. Se existir tutor, atualiza.

Para fotos novas, prefira `POST /api/tutors/me/profile-image` em vez de enviar `img_url` manualmente no `PUT`. O campo no `PUT` continua aceito para compatibilidade.

## POST /api/tutors/me/profile-image

Upload da foto de perfil do tutor autenticado.

Regras:

- exige `ROLE_TUTOR`;
- exige que o perfil de tutor ja exista;
- recebe `multipart/form-data` com campo `file`;
- aceita `image/jpeg`, `image/png` e `image/webp`;
- limite padrao: 5 MB;
- grava no S3 em `uploads/tutors/{tutorId}/profile/{uuid}.ext`;
- atualiza `tutors.img_url` com a chave S3;
- retorna `TutorsResponse` com `img_url` pronto para exibicao.

Exemplo:

```bash
curl -i -sS \
  -X POST http://localhost:8080/api/tutors/me/profile-image \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/caminho/avatar.png"
```

## DELETE /api/tutors/me/profile-image

Remove a foto de perfil do tutor autenticado.

Fluxo:

1. Busca o tutor atual.
2. Limpa `tutors.img_url`.
3. Se o valor antigo era uma chave S3, remove o objeto do bucket.
4. Retorna `TutorsResponse` com `img_url: null`.

## Validacoes

`username` precisa seguir:

```text
^[a-z0-9][a-z0-9._]{2,29}$
```

Regras praticas:

- comeca com letra minuscula ou numero;
- permite letras minusculas, numeros, ponto e underline;
- nao permite `@` ou hifen;
- tem entre 3 e 30 caracteres;
- e salvo em lowercase.

`birthday` deve vir em formato ISO `yyyy-MM-dd'T'HH:mm:ss` e nao pode estar no futuro.

## Resposta

```json
{
  "id": "uuid-do-tutor",
  "user_id": "uuid-do-users-id",
  "name": "Nome Teste",
  "username": "usernameteste",
  "is_private": true,
  "img_url": "https://example.com/avatar.png",
  "birthday": "2003-10-31T00:00:00",
  "updated_at": "2026-06-26T12:00:00",
  "reports_counter": 0
}
```

## Relacao com refresh token

Refresh token nao muda a regra dos endpoints de tutor. O cliente deve sempre enviar access token em `Authorization`. Se o access token expirar, renova em `/auth/refresh` e repete a chamada com o novo access token.
