# 05 - Fluxo de tutors

O fluxo de tutor e o primeiro recurso funcional depois da autenticacao. Ele permite que o usuario logado crie ou atualize seu perfil de tutor.

Base dos endpoints:

```http
/api/tutors
```

Endpoints implementados:

```http
GET /api/tutors/me
PUT /api/tutors/me
```

## `TutorsController`

Arquivo: `backend/api/src/main/java/com/jordania/api/Tutors/TutorsController.java`

O cliente envia o header:

```http
Authorization: Bearer <token>
```

A validacao do token e feita pelo Spring Security (Resource Server) antes do controller. Se o token for ausente, invalido ou expirado, o request nem chega ao controller e retorna:

```http
401 Unauthorized
```

Se o token for valido mas nao tiver `ROLE_TUTOR`, retorna `403 Forbidden`.

Com o token aceito, o controller le o usuario autenticado e extrai o `user_id` do claim `sub`:

```java
public TutorsResponse me(@AuthenticationPrincipal Jwt jwt) {
    return tutorsService.findMe(UUID.fromString(jwt.getSubject()));
}
```

Depois disso, chama o `TutorsService`.

## `GET /api/tutors/me`

Objetivo: buscar o tutor do usuario autenticado.

Fluxo:

1. Valida o JWT interno.
2. Extrai `user_id`.
3. Busca `tutors` por `user_id`.
4. Se existir, retorna `TutorsResponse`.
5. Se nao existir, retorna `404 Not Found`.

Respostas esperadas:

- `401`: sem token ou token invalido.
- `403`: token valido, mas sem `ROLE_TUTOR`.
- `404`: usuario autenticado, mas ainda sem tutor.
- `200`: tutor encontrado.

O `404` nao significa sessao invalida. No front, ele deve ser interpretado como "mostrar formulario vazio para criar tutor".

## `PUT /api/tutors/me`

Objetivo: criar ou atualizar o tutor do usuario autenticado.

Fluxo:

1. Valida o JWT interno.
2. Extrai `user_id`.
3. Confirma que existe `users.id = user_id`.
4. Normaliza e valida campos.
5. Busca tutor existente por `user_id`.
6. Se nao existir, cria.
7. Se existir, atualiza.
8. Retorna `TutorsResponse`.

## `TutorsRequest`

Arquivo: `TutorsRequest.java`

Payload esperado:

```json
{
  "name": "Nome Teste",
  "username": "usernameteste",
  "is_private": true,
  "img_url": "https://example.com/avatar.png",
  "birthday": "2003-10-31T00:00:00"
}
```

Campos:

- `name`: obrigatorio, nao pode ser vazio.
- `username`: obrigatorio, nao pode ser vazio.
- `is_private`: obrigatorio.
- `img_url`: opcional.
- `birthday`: obrigatorio, precisa ser `LocalDateTime` em formato ISO.

Formato correto de `birthday`:

```text
yyyy-MM-dd'T'HH:mm:ss
```

Exemplo:

```text
2003-10-31T00:00:00
```

Formato incorreto:

```text
31/10/2003
```

Esse formato gera `400 Bad Request`, porque o Jackson nao consegue converter para `LocalDateTime`.

## Validacao de `username`

Regex usada:

```text
^[a-z0-9][a-z0-9._]{2,29}$
```

Regras praticas:

- precisa comecar com letra minuscula ou numero;
- pode conter letras minusculas, numeros, ponto e underline;
- nao aceita `@`;
- precisa ter pelo menos 3 caracteres;
- maximo efetivo de 30 caracteres;
- e normalizado para lowercase antes de salvar.

Exemplos validos:

```text
rodrigo
rodrigo.borges
rodrigo_borges
user123
```

Exemplos invalidos:

```text
@rodrigo
rodrigo@
RoDrigo
ab
rodrigo-borges
```

## Validacao de `birthday`

O backend rejeita data no futuro:

```java
if (birthday.isAfter(LocalDateTime.now())) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST)
}
```

Tambem existe constraint no banco:

```sql
CONSTRAINT ck_tutors_birthday CHECK (birthday <= now())
```

## Duplicidade de username

Antes de salvar, o service procura:

```java
findByUsername(username)
```

Se o username ja pertence a outro tutor, retorna:

```http
409 Conflict
```

Se pertence ao mesmo tutor, a atualizacao e permitida.

## `TutorsResponse`

Arquivo: `TutorsResponse.java`

Resposta:

```json
{
  "id": "uuid-do-tutor",
  "user_id": "uuid-do-users-id",
  "name": "Nome Teste",
  "username": "usernameteste",
  "is_private": true,
  "img_url": "https://example.com/avatar.png",
  "birthday": "2003-10-31T00:00:00",
  "updated_at": "2026-06-24T11:51:38.154",
  "reports_counter": 0
}
```

Os nomes JSON seguem a modelagem em snake_case:

- `user_id`
- `is_private`
- `img_url`
- `updated_at`
- `reports_counter`

## Erros comuns do front

Enviar username com `@`:

```json
{
  "username": "teste@"
}
```

Resultado:

```http
400 Bad Request
```

Enviar birthday em formato brasileiro:

```json
{
  "birthday": "31/10/2003"
}
```

Resultado:

```http
400 Bad Request
```

Nao enviar token:

```http
GET /api/tutors/me
```

Resultado:

```http
401 Unauthorized
```

Buscar tutor antes de criar:

```http
GET /api/tutors/me
Authorization: Bearer <token-valido>
```

Resultado:

```http
404 Not Found
```

Esse caso e normal e deve abrir o formulario vazio no app.

