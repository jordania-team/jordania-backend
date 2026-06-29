# 04 - AuthService, JWT interno, roles e security

`AuthService` coordena login social, emissao de access token, refresh token, refresh de sessao e logout.

## Login

`login(LoginRequest)` faz:

1. valida o token social com `SocialTokenVerifier`;
2. normaliza provider para `apple` ou `google`;
3. cria ou reutiliza `providers`;
4. cria ou reutiliza `users`;
5. atualiza email quando o provider envia email valido;
6. usa `tutors.name` como nome de resposta quando ja existir;
7. emite access token com `InternalTokenService`;
8. emite refresh token com `RefreshTokenService`.

## Refresh

`refresh(String rawRefreshToken)` faz:

1. calcula SHA-256 do token recebido;
2. busca `refresh_tokens.token_hash`;
3. rejeita token inexistente, expirado ou revogado;
4. emite novo access token;
5. emite novo refresh token na mesma `family_id`;
6. marca o token antigo como revogado e preenche `replaced_by`;
7. retorna o mesmo contrato de `LoginResponse`.

Se um token ja revogado for usado novamente, a API considera reuse detection e revoga toda a familia.

## Logout

`logout(UUID userId)` revoga todos os refresh tokens ativos do usuario. O access token atual continua valido ate expirar, porque a API nao mantem blacklist de access token.

## JWT interno

`InternalTokenService` emite JWT HS256 com:

- `iss`: issuer configurado;
- `iat`;
- `exp`;
- `sub`: `users.id`;
- `provider`: `apple` ou `google`;
- `role`: `tutor` ou `admin`;
- `name`: quando disponivel;
- `email`: quando disponivel.

O ponto central e:

```text
sub = users.id
```

Controllers usam `Jwt.getSubject()` para identificar o usuario autenticado.

## SecurityConfig

Regras principais:

- `POST /auth/login`: publico.
- `POST /auth/refresh`: publico.
- `OPTIONS /**`: publico.
- `GET /actuator/health/**` e `/actuator/info`: publicos.
- `/dev/**`: publico, mas o controller so existe no profile `local`.
- `POST /auth/logout`: autenticado.
- `/users/**`: autenticado.
- `/api/tutors/**`: exige `ROLE_TUTOR`.
- `/api/**`: autenticado.

`DispatcherType.FORWARD` e `DispatcherType.ERROR` ficam liberados para preservar respostas reais de erro, como `400`, `404` e `409`, sem mascara-las como `401` ou `403`.

## Roles

O enum `RoleType` usa valores minusculos. O conversor de JWT transforma o claim `role` em authority Spring:

- `tutor` -> `ROLE_TUTOR`
- `admin` -> `ROLE_ADMIN`

Assim, `hasRole("TUTOR")` funciona sem exigir que o token carregue `ROLE_` no payload.

## DevAuthController

`DevAuthController` existe apenas com `@Profile("local")`.

Endpoint:

```http
POST /dev/login?userId=<uuid>
```

Ele busca um `Users` existente e emite access token + refresh token sem passar por Apple/Google. Ele nao cria usuarios e nao deve estar ativo em producao.
