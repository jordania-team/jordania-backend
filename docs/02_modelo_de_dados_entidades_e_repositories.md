# 02 - Modelo de dados, entidades e repositories

O pacote `User` contem as entidades JPA que mapeiam o modelo novo do banco. Apesar do pacote se chamar `User`, ele contem tambem `Providers`, `Admins` e `Tutors`, porque todos fazem parte da identidade e perfil do usuario.

## `Providers`

Arquivo: `backend/api/src/main/java/com/jordania/api/User/Providers.java`

Mapeia a tabela `providers`.

Campos principais:

- `provider_subject`: chave primaria. E o subject recebido do provider social.
- `provider_name`: nome do provider, hoje `apple` ou `google`.

Exemplo conceitual:

```text
provider_subject = "001234.apple-sub"
provider_name    = "apple"
```

Essa tabela existe para separar a identidade externa do usuario interno da API.

O repository correspondente e `ProvidersRepository`, que estende `JpaRepository<Providers, String>`. A chave usada pelo repository e `provider_subject`.

## `Users`

Arquivo: `backend/api/src/main/java/com/jordania/api/User/Users.java`

Mapeia a tabela `users`.

Campos principais:

- `id`: UUID interno da API.
- `role`: enum `RoleType`, armazenado no PostgreSQL como `role_type`.
- `email`: email vindo do provider, quando disponivel.
- `provider`: relacao com `Providers`.
- `created_at`: criacao do usuario interno.
- `updated_at`: ultima atualizacao do usuario interno.

Quando um usuario novo faz login, a API cria um `Users` com:

- UUID novo;
- role `tutor`;
- provider social associado;
- email se o provider enviou.

Esse `id` vira o `sub` do JWT interno. Ou seja, quando o app chama endpoints autenticados, a API identifica o usuario pelo `users.id`.

O metodo `updateEmail` atualiza o email apenas quando o provider envia um valor nao vazio. Ele tambem atualiza `updated_at`.

O repository correspondente e `UsersRepository`. A consulta mais importante e:

```java
findByProviderProviderSubject(String provider_subject)
```

Ela encontra o usuario interno a partir do subject do provider social.

## `RoleType`

Arquivo: `backend/api/src/main/java/com/jordania/api/User/RoleType.java`

Define os roles aceitos:

```java
public enum RoleType {
    tutor,
    admin
}
```

Os nomes estao em minusculo para casar exatamente com o enum do PostgreSQL:

```sql
CREATE TYPE role_type AS ENUM ('tutor', 'admin');
```

Hoje, todo usuario criado automaticamente pelo login recebe role `tutor`.

## `Admins`

Arquivo: `backend/api/src/main/java/com/jordania/api/User/Admins.java`

Mapeia a tabela `admins`.

Campos principais:

- `id`: UUID da linha de admin.
- `user`: relacao com `Users`.

A tabela existe porque fazia parte da primeira fatia da modelagem. Nesta etapa ainda nao existem endpoints de administracao. Ela prepara o modelo para permitir que um `users` tambem tenha registro em `admins`.

O repository correspondente e `AdminsRepository`.

## `Tutors`

Arquivo: `backend/api/src/main/java/com/jordania/api/User/Tutors.java`

Mapeia a tabela `tutors`.

Campos principais:

- `id`: UUID do tutor.
- `user`: relacao um-para-um com `Users`.
- `name`: nome do tutor.
- `username`: nome publico unico.
- `is_private`: se o perfil e privado.
- `img_url`: URL ou texto da imagem, opcional.
- `birthday`: data de nascimento.
- `updated_at`: ultima atualizacao do tutor.
- `reports_counter`: contador de denuncias, inicia em `0`.

O construtor de `Tutors` gera UUID novo, associa o `Users`, inicia `reports_counter = 0` e chama `update`.

O metodo `update` atualiza:

- `name`
- `username`
- `is_private`
- `img_url`
- `birthday`
- `updated_at`

`img_url` e normalizado: string vazia vira `null`.

O repository correspondente e `TutorsRepository`. As consultas importantes sao:

```java
findByUserId(UUID user_id)
```

Usada para carregar o tutor do usuario autenticado em `/api/tutors/me`.

```java
findByUsername(String username)
```

Usada para impedir username duplicado.

## Relacionamento geral

O fluxo de relacionamento fica assim:

```text
providers.provider_subject
        |
        | users.provider_id
        v
users.id
        |
        | tutors.user_id
        v
tutors
```

Em palavras:

1. O provider social identifica uma pessoa por `provider_subject`.
2. A API transforma isso em um usuario interno em `users`.
3. O usuario interno pode criar um perfil de tutor em `tutors`.

