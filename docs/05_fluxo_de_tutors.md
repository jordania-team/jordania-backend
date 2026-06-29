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
```

Ambos exigem access token JWT interno com `role=tutor`.

## GET /api/tutors/me

Fluxo:

1. Spring Security valida o JWT interno.
2. O claim `role=tutor` vira `ROLE_TUTOR`.
3. O controller extrai `users.id` de `Jwt.getSubject()`.
4. O service busca `tutors` por `user_id`.
5. Se nao existir tutor, retorna `404`.
6. Se existir, retorna `TutorsResponse`.

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
