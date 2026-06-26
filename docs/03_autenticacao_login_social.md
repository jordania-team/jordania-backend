# 03 - Autenticacao e login social

O login publico da API e:

```http
POST /auth/login
```

Ele recebe um token social Apple ou Google, valida esse token com as chaves publicas do provider e, se estiver correto, cria ou reutiliza o usuario interno da API.

## `AuthController`

Arquivo: `backend/api/src/main/java/com/jordania/api/Auth/AuthController.java`

Expoe o endpoint:

```java
@PostMapping("/login")
public LoginResponse login(@RequestBody @Valid LoginRequest request)
```

Ele delega a regra para `AuthService`.

Tambem trata erros:

- `InvalidIdentityTokenException` vira `401 Unauthorized`.
- `AuthConfigurationException` vira `503 Service Unavailable`.

Isso separa dois problemas:

- `401`: o token recebido do app e invalido.
- `503`: o backend esta sem configuracao necessaria, por exemplo client ID ausente.

## `LoginRequest`

Arquivo: `LoginRequest.java`

Formato recebido:

```json
{
  "provider": "apple",
  "identityToken": "...",
  "name": "Nome opcional",
  "rawNonce": "nonce usado no Apple Sign In"
}
```

Campos:

- `provider`: obrigatorio. Aceita `apple` ou `google`, sem diferenciar maiusculas/minusculas.
- `identityToken`: obrigatorio. E o token JWT emitido por Apple ou Google.
- `name`: opcional. No Apple, pode ser usado porque o nome nem sempre vem dentro do token.
- `rawNonce`: usado na validacao Apple.

## `AuthProvider`

Arquivo: `AuthProvider.java`

Enum interno:

```java
APPLE,
GOOGLE
```

O metodo `from` normaliza o texto recebido:

```java
AuthProvider.valueOf(value.trim().toUpperCase())
```

Assim, `apple`, `APPLE` ou ` Apple ` apontam para `APPLE`.

## `SocialTokenVerifier`

Arquivo: `SocialTokenVerifier.java`

E o componente que valida o token social recebido no login. Ele decide qual fluxo usar:

- Apple: `verifyApple`
- Google: `verifyGoogle`

Ele usa `NimbusJwtDecoder` com JWK Set URI de cada provider. Isso significa que a API nao confia no token apenas porque ele chegou do app; ela verifica assinatura, issuer, expiracao e audience.

## Validacao Apple

Configuracoes usadas:

- issuer esperado: `https://appleid.apple.com`
- JWK Set URI: `https://appleid.apple.com/auth/keys`
- audience esperada: `AUTH_APPLE_CLIENT_ID`

Validacoes:

1. `AUTH_APPLE_CLIENT_ID` precisa estar configurado.
2. O token precisa estar assinado por chave valida da Apple.
3. O token precisa estar dentro do prazo.
4. O claim `iss` precisa ser Apple.
5. O claim `aud` precisa conter o client ID configurado.
6. O nonce precisa bater.

O nonce funciona assim:

- o app gera um `rawNonce`;
- a Apple recebe o hash desse nonce no login;
- o token Apple volta com o claim `nonce`;
- a API calcula `sha256(rawNonce)` e compara com o claim.

Se nao bater, o login falha com `401`.

Quando passa, a API cria um `SocialIdentity` com:

- provider `APPLE`;
- subject vindo de `jwt.getSubject()`;
- name vindo de `LoginRequest.name`;
- email vindo do claim `email`.

## Validacao Google

Configuracoes usadas:

- issuer esperado: `https://accounts.google.com` ou `accounts.google.com`
- JWK Set URI: `https://www.googleapis.com/oauth2/v3/certs`
- audience esperada: `AUTH_GOOGLE_CLIENT_ID`

Validacoes:

1. `AUTH_GOOGLE_CLIENT_ID` precisa estar configurado.
2. O token precisa estar assinado por chave valida do Google.
3. O token precisa estar dentro do prazo.
4. O issuer precisa ser aceito.
5. O audience precisa conter o client ID configurado.

Quando passa, a API cria um `SocialIdentity` com:

- provider `GOOGLE`;
- subject vindo de `jwt.getSubject()`;
- name vindo do claim `name`;
- email vindo do claim `email`.

## `AudienceValidator`

Arquivo: `AudienceValidator.java`

Valida se o token recebido foi emitido para o app correto.

Ele olha o claim `aud` do JWT social e exige que ele contenha o client ID configurado no backend.

Sem essa validacao, a API poderia aceitar um token real da Apple/Google emitido para outro aplicativo, o que seria incorreto.

## `SocialIdentity`

Arquivo: `SocialIdentity.java`

E um DTO interno usado depois da validacao social.

Ele carrega apenas os dados que a API precisa:

```java
provider
subject
name
email
```

A partir dele, o `AuthService` cria ou atualiza `providers` e `users`.

## Erros comuns no login

`401 Unauthorized`:

- provider desconhecido;
- identity token invalido;
- token expirado;
- audience nao bate;
- issuer nao bate;
- nonce Apple ausente ou incorreto.

`503 Service Unavailable`:

- `AUTH_APPLE_CLIENT_ID` ausente no login Apple;
- `AUTH_GOOGLE_CLIENT_ID` ausente no login Google.

