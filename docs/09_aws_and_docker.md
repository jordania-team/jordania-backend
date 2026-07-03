# 10 - Preparacao da API para Docker na AWS

Este documento registra a base da mudanca entre o uso local da API em Docker e a execucao do mesmo container em AWS.

Antes do deploy, cada integrante conseguia rodar e testar a API localmente, normalmente com `docker-compose.yml`: um container para PostgreSQL e outro para a API Spring Boot. Na AWS, o container da API nao deve depender desse ambiente local. Ele precisa receber configuracoes externas em tempo de execucao e se conectar a servicos fora do container.

Este documento nao descreve como criar VPC, ECS, ECR, RDS, S3, IAM, SSM ou load balancer. O foco aqui e o que a API precisa deixar de assumir como local para funcionar corretamente em um runtime AWS.

## Ideia principal

A imagem Docker da API deve ser a mesma independente do ambiente. O que muda entre local e AWS sao as variaveis de ambiente, secrets e servicos acessados pelo container.

Na pratica:

- o Dockerfile deve gerar a imagem da API sem gravar secrets dentro dela;
- a API nao deve depender de `localhost` para banco, storage ou autenticacao;
- o `docker-compose.yml` deve ser tratado como ferramenta local de desenvolvimento;
- em AWS, banco, secrets, bucket e porta devem vir do ambiente de runtime;
- o profile `local` nao deve ser ativado em producao.

## Banco de dados

No ambiente local, o `docker-compose.yml` sobe um PostgreSQL com:

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/pocdb
SPRING_DATASOURCE_USERNAME: pocuser
SPRING_DATASOURCE_PASSWORD: pocpass
```

No `application.yml`, ainda existe default local:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pocdb
    username: pocuser
    password: pocpass
```

Para AWS, a API nao pode usar `localhost` nem depender do service name `postgres` do Compose. O container precisa receber:

```bash
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

Esses valores devem apontar para o banco real usado pelo ambiente AWS. A API continua usando Flyway normalmente, portanto as migrations em `src/main/resources/db/migration` sao executadas no banco configurado quando a aplicacao sobe.

Pontos importantes:

- `localhost` dentro do container significa o proprio container, nao o banco externo;
- o nome `postgres` so existe na rede criada pelo `docker-compose.yml`;
- `spring.jpa.hibernate.ddl-auto=validate` exige que o schema esteja coerente com as migrations;
- Flyway deve continuar ativo para versionar o schema.

## Secrets e autenticacao

No local, o Compose injeta um segredo de desenvolvimento:

```yaml
AUTH_JWT_SECRET: local-development-secret-change-before-deploy-1234567890
```

Em AWS, esse valor precisa ser substituido por um segredo real, externo a imagem Docker. A API exige que `AUTH_JWT_SECRET` tenha pelo menos 32 bytes, pois ele assina os JWTs internos com HS256.

Variaveis obrigatorias para autenticacao:

```bash
AUTH_JWT_SECRET
AUTH_APPLE_CLIENT_ID
AUTH_GOOGLE_CLIENT_ID
```

Variaveis opcionais, mas relevantes:

```bash
AUTH_JWT_ISSUER
AUTH_JWT_TTL
AUTH_REFRESH_TTL
```

O token Apple ou Google continua sendo usado apenas no login. Depois disso, a API emite o JWT interno e o app usa:

```http
Authorization: Bearer <token>
```

O segredo JWT, as credenciais de banco e os client IDs nao devem ser copiados para o Dockerfile, para o jar, nem para arquivos versionados com valores reais.

## Profile local e endpoints de desenvolvimento

A API possui `DevAuthController` ativo somente com:

```bash
SPRING_PROFILES_ACTIVE=local
```

Esse profile serve para facilitar testes locais com:

```http
POST /dev/login?userId=<uuid>
```

Em AWS, esse profile nao deve ser ativado. O login esperado para o ambiente publicado e o fluxo real de `/auth/login`, validando token Apple ou Google.

Mesmo que a seguranca tenha regra permitindo `/dev/**`, o controller so existe quando o profile `local` esta ativo. Portanto, a regra operacional e simples: nao configurar `SPRING_PROFILES_ACTIVE=local` fora do ambiente de desenvolvimento.

## Porta e health check

O Dockerfile expoe a porta `8080`:

```dockerfile
EXPOSE 8080
```

A aplicacao tambem permite configurar a porta por variavel:

```bash
SERVER_PORT=8080
```

Para AWS, o container deve expor a mesma porta que o runtime espera encaminhar. O endpoint de health ja esta liberado sem autenticacao:

```http
GET /actuator/health
```

Esse endpoint deve ser usado para verificar se a API subiu. Ele nao substitui testes funcionais, mas confirma que o processo Spring Boot esta respondendo.

## Storage de imagens

Localmente, a API pode subir sem bucket configurado, mas endpoints de upload para S3 dependem de:

```bash
POCAPI_IMAGES_BUCKET
AWS_REGION
```

Em AWS, a API deve receber o nome do bucket e a regiao por ambiente. O codigo usa o SDK da AWS (`S3Client` e `S3Presigner`), entao credenciais nao devem ser colocadas na aplicacao. O runtime AWS deve fornecer a identidade usada pelo container.

Variaveis adicionais de imagem:

```bash
POCAPI_IMAGES_PRESIGNED_URL_TTL
POCAPI_IMAGES_PUBLIC_BASE_URL
POCAPI_IMAGES_MAX_SIZE_BYTES
POCAPI_IMAGES_MAX_FILE_SIZE
POCAPI_IMAGES_MAX_REQUEST_SIZE
```

Se `POCAPI_IMAGES_PUBLIC_BASE_URL` nao estiver configurada, a API gera URL assinada temporaria para leitura das imagens.

## Logs e comportamento de producao

Para desenvolvimento, `show-sql` ajuda a debugar. Em AWS, o ideal e desativar SQL verboso:

```bash
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_FORMAT_SQL=false
```

A API deve logar no stdout/stderr do container. O runtime AWS fica responsavel por coletar esses logs. Nao e necessario gravar arquivos de log dentro da imagem ou do filesystem do container.

## Dockerfile e build da imagem

O Dockerfile atual faz build do jar e copia o artefato para uma imagem final com JRE:

```dockerfile
FROM eclipse-temurin:21-jdk AS build
...
FROM eclipse-temurin:21-jre
...
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Para rodar em AWS, o ponto principal e manter a imagem independente do ambiente:

- nao colocar secrets no Dockerfile;
- nao depender do `docker-compose.yml` para a aplicacao iniciar;
- nao assumir que o banco roda no mesmo host;
- publicar a porta que o runtime vai encaminhar;
- garantir que a aplicacao falhe ao iniciar quando uma configuracao obrigatoria estiver ausente, como `AUTH_JWT_SECRET`.

Os testes podem continuar rodando localmente ou em CI antes da imagem ser publicada. O container de runtime nao deve depender de testes manuais entre maquinas dos integrantes para funcionar.

## Checklist da API para AWS

Antes de publicar uma imagem da API, validar:

- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` apontam para o banco do ambiente AWS;
- `AUTH_JWT_SECRET` e diferente do segredo local e tem pelo menos 32 bytes;
- `AUTH_APPLE_CLIENT_ID` e `AUTH_GOOGLE_CLIENT_ID` correspondem aos apps reais usados no login;
- `SPRING_PROFILES_ACTIVE=local` nao esta ativo;
- `SERVER_PORT` bate com a porta exposta pelo container;
- `/actuator/health` responde sem autenticacao;
- `POCAPI_IMAGES_BUCKET` e `AWS_REGION` estao configurados quando upload de imagens estiver habilitado;
- logs SQL verbosos estao desligados em producao;
- nenhuma credencial real foi gravada no Dockerfile, no `application.yml`, no Compose ou em arquivos versionados.

## Resumo

A mudanca central nao e reescrever a API. E separar o que e local do que e runtime:

- local: `docker-compose.yml`, banco `postgres`, secrets fake, profile `local`, testes manuais dos integrantes;
- AWS: mesma imagem Docker, configuracao por variaveis/secrets, banco externo, storage S3, health check e logs do container.

Com essa separacao, a imagem Docker deixa de representar "a API rodando na maquina de alguem" e passa a representar "a API pronta para receber configuracao do ambiente onde for executada".
