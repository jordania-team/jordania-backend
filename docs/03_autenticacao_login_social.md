# 03 - Autenticacao e login social

O login social serve para provar identidade. A API nao usa o token Apple/Google para proteger endpoints internos; ela emite tokens proprios.

## Fluxo de login

1. O app recebe um identity token Apple ou Google.
2. O app chama `POST /auth/login`.
3. A API valida assinatura, issuer, audiencia e expiracao do token social.
4. Para Apple, a API tambem valida o nonce.
5. A API resolve ou cria `providers`.
6. A API resolve ou cria `users`.
7. A API busca `tutors.name`, se existir.
8. A API emite JWT interno.
9. A API emite refresh token opaco.
10. O app salva `token` e `refreshToken`.

## Request

```http
POST /auth/login
Content-Type: application/json
```

```json
{
  "provider": "google",
  "identityToken": "identity-token",
  "name": "Nome opcional",
  "rawNonce": null
}
```

Para Apple, `rawNonce` e obrigatorio.

## Response

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

## Configuracao

Variaveis relevantes:

- `AUTH_JWT_SECRET`: segredo HS256 com pelo menos 32 bytes.
- `AUTH_JWT_ISSUER`: issuer do JWT interno, default `pocapi`.
- `AUTH_JWT_TTL`: duracao do access token, default `PT1H`.
- `AUTH_REFRESH_TTL`: duracao do refresh token, default `P30D`.
- `AUTH_APPLE_CLIENT_ID`: audiencia esperada para Apple.
- `AUTH_GOOGLE_CLIENT_ID`: audiencia esperada para Google.

## Erros

Token social invalido, provider nao suportado, nonce Apple ausente/invalido ou refresh token invalido retornam:

```http
401 Unauthorized
```

Configuracao ausente de Apple/Google retorna:

```http
503 Service Unavailable
```

## Sessao no app

O app deve usar o access token para endpoints protegidos:

```http
Authorization: Bearer <token>
```

Quando receber `401` por token expirado, o app deve chamar `/auth/refresh` com o refresh token atual e substituir os dois tokens salvos pelo novo par retornado.
