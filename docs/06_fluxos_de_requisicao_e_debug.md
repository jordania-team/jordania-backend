# 06 - Fluxos de requisicao e debug

Este documento junta o fluxo completo visto pelo app iOS e pelo backend.

## Fluxo completo de login e tutor

1. Usuario toca em login Apple ou Google no app iOS.
2. O provider devolve um `identityToken` para o app.
3. O app envia esse token para a API:

```http
POST /auth/login
```

4. A API valida o token com Apple ou Google.
5. A API cria ou reutiliza `providers`.
6. A API cria ou reutiliza `users`.
7. A API emite um JWT interno.
8. O app salva esse JWT.
9. O app chama:

```http
GET /api/tutors/me
Authorization: Bearer <jwt-interno>
```

10. Se retornar `404`, o app mostra formulario vazio.
11. O usuario preenche o formulario.
12. O app envia:

```http
PUT /api/tutors/me
Authorization: Bearer <jwt-interno>
```

13. A API cria ou atualiza `tutors`.
14. O app mostra o `Tutor data` retornado.

## Login request

Exemplo:

```json
{
  "provider": "apple",
  "identityToken": "token-da-apple",
  "name": "Nome vindo do app",
  "rawNonce": "nonce-original"
}
```

Para Google:

```json
{
  "provider": "google",
  "identityToken": "token-do-google"
}
```

## Login response

```json
{
  "token": "jwt-interno-da-api",
  "userId": "0a7aa899-482f-47e6-ba5b-b66d4c33da4b",
  "name": "Nome Teste",
  "email": "email@example.com",
  "role": "tutor"
}
```

Campos:

- `token`: JWT interno usado em `/api/**`.
- `userId`: `users.id`.
- `name`: nome do tutor se existir; senao, nome vindo do provider.
- `email`: email salvo em `users`.
- `role`: role atual, hoje normalmente `tutor`.

## Header obrigatorio para tutor

Todas as chamadas de tutor precisam enviar:

```http
Authorization: Bearer <token>
```

Se o token esta ausente, malformado, expirado ou com assinatura invalida:

```http
401 Unauthorized
```

## Interpretando o debug do front

Quando o app mostra:

```text
userId   0A7AA899-482F-47E6-BA5B-B66D4C33DA4B
email    ...
provider apple
role     tutor
token    presente
```

Isso indica que:

- `/auth/login` funcionou;
- a API retornou `LoginResponse`;
- o app guardou o token;
- o usuario interno existe ou foi criado em `users`;
- o provider foi salvo ou reutilizado em `providers`;
- o role retornado foi `tutor`.

Se depois disso `GET /api/tutors/me` retorna `404`, nao e logout. Significa apenas que ainda nao existe linha em `tutors` para esse `userId`.

Se retorna `401`, o problema e token ausente/invalido/expirado ou header `Authorization` nao enviado corretamente.

Se retorna `400` no `PUT`, o problema e payload invalido.

Se retorna `409`, o username ja pertence a outro tutor.

## Respostas esperadas

`POST /auth/login`

- `200`: login social validado e JWT interno emitido.
- `401`: token social invalido.
- `503`: configuracao de Apple/Google ausente no backend.

`GET /api/tutors/me`

- `200`: tutor existe.
- `401`: token interno ausente ou invalido.
- `403`: token valido, mas sem `ROLE_TUTOR`.
- `404`: tutor ainda nao criado.

`PUT /api/tutors/me`

- `200`: tutor criado ou atualizado.
- `400`: request invalido.
- `401`: token interno ausente ou invalido.
- `403`: token valido, mas sem `ROLE_TUTOR`.
- `409`: username duplicado.

## Payload correto de tutor

```json
{
  "name": "Nome Teste",
  "username": "usernameteste",
  "is_private": true,
  "img_url": "img.com",
  "birthday": "2003-10-31T00:00:00"
}
```

Detalhes importantes:

- `birthday` precisa ser ISO, nao `31/10/2003`.
- `username` nao aceita `@`.
- `is_private` precisa existir.
- `img_url` pode ser `null` ou string.

## Queries uteis

Ver migrations:

```sql
SELECT version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Ver providers:

```sql
SELECT provider_subject, provider_name
FROM providers;
```

Ver usuarios internos:

```sql
SELECT id, role, email, provider_id, created_at, updated_at
FROM users;
```

Ver tutors:

```sql
SELECT id, user_id, name, username, is_private, img_url, birthday, updated_at, reports_counter
FROM tutors;
```

Ver usuario junto com provider e tutor:

```sql
SELECT
    u.id,
    u.role,
    u.email,
    p.provider_name,
    p.provider_subject,
    t.id AS tutor_id,
    t.username,
    t.name
FROM users u
JOIN providers p ON p.provider_subject = u.provider_id
LEFT JOIN tutors t ON t.user_id = u.id
ORDER BY u.created_at DESC;
```

Ver se existe tutor orfao:

```sql
SELECT count(*)
FROM tutors t
LEFT JOIN users u ON u.id = t.user_id
WHERE u.id IS NULL;
```

O resultado esperado e `0`.

## HTTPS e AWS

O app usa:

```text
https://api.redepets.xyz
```

O HTTPS termina no ALB da AWS. O container Spring Boot escuta HTTP interno na porta `8080`.

Isso e normal:

- cliente externo fala HTTPS com o ALB;
- ALB encaminha para o container dentro da VPC;
- Spring Boot recebe HTTP interno.

Os dados sensiveis nao devem ser enviados em query string. O token vai no header `Authorization`, e o login/tutor usam body JSON. Assim, mesmo se houver redirecionamento de HTTP para HTTPS no ALB, o fluxo correto do app deve sempre chamar diretamente `https://api.redepets.xyz`, evitando enviar dados por HTTP.

