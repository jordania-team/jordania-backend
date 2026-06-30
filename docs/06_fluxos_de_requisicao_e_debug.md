# 06 - Fluxos de requisicao e debug

Este arquivo reune exemplos de chamadas e consultas uteis para testar a API.

## Login social

```bash
curl -i -sS \
  -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "provider": "google",
    "identityToken": "IDENTITY_TOKEN"
  }'
```

Resposta esperada:

```json
{
  "token": "jwt-interno",
  "userId": "uuid",
  "name": "Nome ou null",
  "email": "email@example.com",
  "role": "tutor",
  "refreshToken": "token-opaco",
  "refreshExpiresAt": "2026-07-26T12:00:00Z"
}
```

## Refresh

```bash
curl -i -sS \
  -X POST http://localhost:8080/auth/refresh \
  -H 'Content-Type: application/json' \
  -d '{
    "refreshToken": "REFRESH_TOKEN_ATUAL"
  }'
```

Ao receber sucesso, o cliente deve substituir o access token e o refresh token salvos.

## Logout

```bash
curl -i -sS \
  -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

Resposta esperada:

```http
204 No Content
```

## Sessao atual

```bash
curl -i -sS \
  http://localhost:8080/users/me \
  -H "Authorization: Bearer $TOKEN"
```

Resposta:

```json
{
  "id": "uuid",
  "email": "email@example.com",
  "provider": "google",
  "role": "tutor",
  "name": "Nome do tutor ou null"
}
```

## Tutor atual

```bash
curl -i -sS \
  http://localhost:8080/api/tutors/me \
  -H "Authorization: Bearer $TOKEN"
```

Criar ou atualizar tutor:

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

Enviar foto de perfil:

```bash
curl -i -sS \
  -X POST http://localhost:8080/api/tutors/me/profile-image \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/caminho/avatar.png"
```

Remover foto de perfil:

```bash
curl -i -sS \
  -X DELETE http://localhost:8080/api/tutors/me/profile-image \
  -H "Authorization: Bearer $TOKEN"
```

Quando a API usa S3 privado, `img_url` na resposta e uma URL assinada temporaria. Se ela expirar no app, chame `GET /api/tutors/me` de novo.

## DevAuth local

Com profile local ativo:

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Emitir tokens para um usuario existente:

```bash
curl -i -sS \
  -X POST "http://localhost:8080/dev/login?userId=USER_UUID"
```

Esse endpoint nao cria usuario. Ele so existe para testar fluxos protegidos sem Apple/Google.

## Debug SQL

Ver usuarios:

```sql
SELECT u.id, u.email, u.role, p.provider_name, p.provider_subject
FROM users u
JOIN providers p ON p.provider_subject = u.provider_id;
```

Ver tutors:

```sql
SELECT id, user_id, name, username, is_private, img_url, updated_at
FROM tutors;
```

Ver refresh tokens:

```sql
SELECT id, user_id, family_id, expires_at, revoked_at, replaced_by, created_at
FROM refresh_tokens
ORDER BY created_at DESC;
```

Ver tokens ativos por usuario:

```sql
SELECT id, family_id, expires_at, created_at
FROM refresh_tokens
WHERE user_id = 'USER_UUID'
  AND revoked_at IS NULL
ORDER BY created_at DESC;
```

## Leituras de status

- `401` em endpoint protegido: access token ausente, invalido ou expirado.
- `403` em `/api/tutors/**`: token valido, mas sem `ROLE_TUTOR`.
- `404` em `/api/tutors/me`: usuario autenticado ainda nao tem tutor.
- `401` em `/auth/refresh`: refresh token invalido, expirado ou reutilizado.
