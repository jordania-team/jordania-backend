# 07 - Refresh tokens e sessao

Refresh token permite renovar a sessao sem obrigar o usuario a refazer login social sempre que o access token expira.

## Dois tokens

O login retorna:

- `token`: JWT interno de curta duracao, usado em `Authorization`.
- `refreshToken`: token opaco de maior duracao, usado somente em `/auth/refresh`.

TTL padrao:

- access token: `AUTH_JWT_TTL`, default `PT1H`;
- refresh token: `AUTH_REFRESH_TTL`, default `P30D`.

## Armazenamento

O refresh token bruto aparece apenas na resposta da API. No banco, a API salva:

```text
SHA-256(refreshToken)
```

Isso reduz impacto caso alguem leia a tabela `refresh_tokens`.

## Rotacao

Cada chamada bem-sucedida a `/auth/refresh`:

1. valida o refresh token atual;
2. emite novo access token;
3. emite novo refresh token;
4. mantem a mesma `family_id`;
5. revoga o token antigo;
6. grava em `replaced_by` o hash do token novo.

Depois do refresh, o cliente deve descartar o refresh token antigo.

## Reuse detection

Se um refresh token ja revogado for usado novamente, a API entende que houve reutilizacao indevida.

Nesse caso:

- revoga todos os tokens ativos da mesma `family_id`;
- retorna `401 Unauthorized`;
- o cliente deve encerrar a sessao local e pedir novo login.

## Logout

`POST /auth/logout` exige access token valido e revoga todos os refresh tokens ativos do usuario.

O access token atual nao e colocado em blacklist. Ele continua tecnicamente valido ate `exp`, mas nao podera mais ser renovado depois do logout.

## Cuidados do cliente

- Guardar o refresh token em armazenamento seguro.
- Nunca enviar refresh token em endpoints que nao sejam `/auth/refresh`.
- Ao renovar, substituir atomica e imediatamente `token` e `refreshToken`.
- Se `/auth/refresh` retornar `401`, limpar sessao local e pedir login.
- Se duas chamadas tentarem renovar ao mesmo tempo com o mesmo refresh token, uma delas pode disparar reuse detection. O cliente deve serializar refresh.
