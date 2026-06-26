# 04 - AuthService, JWT interno, roles e security

Depois que o token Apple/Google e validado, a API nao usa esse token social para proteger os endpoints internos. Ela emite um JWT proprio, chamado aqui de JWT interno.

O token social serve para provar identidade no login. O JWT interno serve para chamar `/api/**`.

## `AuthService`

Arquivo: `AuthService.java`

Fluxo do metodo `login`:

1. Recebe `LoginRequest`.
2. Chama `SocialTokenVerifier.verify`.
3. Recebe um `SocialIdentity`.
4. Normaliza o provider para `apple` ou `google`.
5. Faz upsert em `providers`.
6. Faz upsert em `users`.
7. Busca nome do tutor, se ja existir.
8. Emite JWT interno.
9. Retorna `LoginResponse`.

## Upsert em `providers`

A busca e feita por `identity.subject()`, que vira `provider_subject`.

Se o provider ainda nao existe:

```java
new Providers(identity.subject(), providerName)
```

Se ja existe, a API confirma se o `provider_name` e o mesmo. Se o mesmo `provider_subject` aparecer com outro provider, a API rejeita:

```text
Provider subject already belongs to another provider
```

Essa protecao existe porque `provider_subject` e chave primaria global em `providers`.

## Upsert em `users`

Depois de resolver `providers`, a API procura:

```java
findByProviderProviderSubject(providers.getProvider_subject())
```

Se nao existir usuario interno, cria:

```java
new Users(providers, identity.email())
```

Esse construtor define:

- `id`: UUID novo;
- `role`: `tutor`;
- `provider`: provider social validado;
- `email`: email vindo do provider, se houver;
- timestamps atuais.

Se o usuario ja existir, a API atualiza o email quando o provider envia um email valido.

## Nome retornado no login

O `LoginResponse.name` tenta usar primeiro o nome salvo em `tutors`:

```java
tutorsRepository.findByUserId(savedUsers.getId())
```

Se o tutor ainda nao existir, usa o nome vindo do provider social.

Isso permite que, depois que o usuario cria o perfil de tutor, o login passe a refletir o nome do perfil.

## `LoginResponse`

Formato retornado:

```json
{
  "token": "jwt-interno",
  "userId": "uuid-do-users-id",
  "name": "Nome",
  "email": "email@example.com",
  "role": "tutor"
}
```

O app iOS guarda `token` e usa esse valor no header:

```http
Authorization: Bearer <token>
```

## `InternalTokenService`

Arquivo: `InternalTokenService.java`

Emite o JWT interno com algoritmo HS256.

Configuracoes usadas:

- `AUTH_JWT_SECRET`: segredo usado para assinar.
- `AUTH_JWT_ISSUER`: issuer, default `pocapi`.
- `AUTH_JWT_TTL`: tempo de vida, default `PT1H`.

Claims emitidos:

- `iss`: issuer da API.
- `iat`: data de emissao.
- `exp`: expiracao.
- `sub`: `users.id`.
- `provider`: `apple` ou `google`.
- `role`: `tutor` ou `admin`.
- `name`: quando disponivel.
- `email`: quando disponivel.

O ponto mais importante e:

```text
sub = users.id
```

Isso faz com que endpoints internos consigam identificar o usuario autenticado sem consultar o provider social novamente.

## Validacao do JWT interno (Resource Server)

A validacao do JWT interno nos endpoints `/api/**` e feita pelo Spring Security, atraves do `JwtDecoder` configurado em `SecurityConfig`. Nao existe mais validacao manual em controller, e a classe `InternalTokenVerifier` foi removida.

O `JwtDecoder` (Nimbus, HS256) verifica automaticamente:

1. Assinatura com `AUTH_JWT_SECRET`.
2. Claim `exp` ainda no futuro.
3. Claim `iss` igual ao issuer configurado (`AUTH_JWT_ISSUER`).
4. Header `typ = JWT`.

Se o token e ausente, invalido ou expirado, o Resource Server responde `401 Unauthorized` antes de chegar ao controller. O `sub` do token (igual a `users.id`) fica disponivel no `Authentication`, e o `TutorsController` o le via `Jwt.getSubject()`.

## `SecurityConfig`

Arquivo: `SecurityConfig.java`

A autenticacao e a autorizacao estao centralizadas no Spring Security. O filtro registra o Resource Server e define regras explicitas:

```java
.authorizeHttpRequests(authorize -> authorize
    .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
    .requestMatchers(HttpMethod.GET, "/actuator/health/**", "/actuator/info").permitAll()
    .requestMatchers("/api/tutors/**").hasRole("TUTOR")
    .requestMatchers("/api/**").authenticated()
    .anyRequest().authenticated()
)
.oauth2ResourceServer(oauth2 -> oauth2
    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
)
```

Pontos importantes:

- `POST /auth/login`, `OPTIONS /**`, `/actuator/health` e `/actuator/info` sao publicos.
- `/api/tutors/**` exige `ROLE_TUTOR`; qualquer outro `/api/**` exige autenticacao.
- A ordem importa: a regra de `/api/tutors/**` vem antes da regra mais ampla `/api/**`.
- `DispatcherType.FORWARD` e `DispatcherType.ERROR` sao liberados. Isso e necessario porque o `AuthorizationFilter` roda em todos os dispatches; sem isso, um `404`, `400` ou `409` lancado pelo service seria reencaminhado para `/error` e mascarado como `401` ou `403`.

## Beans definidos em `SecurityConfig`

- `SecretKey`: cria a chave HS256 a partir de `AUTH_JWT_SECRET`.
- `JwtEncoder`: usado por `InternalTokenService` para emitir o JWT interno.
- `JwtDecoder`: valida o JWT interno (assinatura, `exp`, issuer e `typ`); usado pelo Resource Server.
- `jwtAuthenticationConverter`: converte o claim `role` em authority Spring (`ROLE_TUTOR` ou `ROLE_ADMIN`), preservando valores que ja venham como `ROLE_*`.

## Roles e authorities

O enum `RoleType` usa valores em minusculo (`tutor`, `admin`). O `jwtAuthenticationConverter` mapeia esses valores para authorities em maiusculo com prefixo `ROLE_`:

- `role=tutor` -> `ROLE_TUTOR`
- `role=admin` -> `ROLE_ADMIN`

Assim, regras como `hasRole("TUTOR")` e `hasRole("ADMIN")` funcionam de forma consistente. As regras de admin ja estao preparadas para endpoints futuros.

## Beneficio da centralizacao

Como a protecao de `/api/**` esta no Spring Security, qualquer novo endpoint `/api/**` ja nasce protegido. Os controllers nao precisam validar o token manualmente: basta declarar a regra adequada em `SecurityConfig` e ler o usuario autenticado via `Jwt`.

