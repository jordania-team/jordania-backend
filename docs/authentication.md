# Autenticacao social

Esta integracao usa o token de identidade emitido pela Apple apenas para
comprovar a identidade do usuario. A API valida esse token e emite seu proprio
JWT, que o app envia no header `Authorization: Bearer <token>`.

## Implementado

- Login com Apple no app iOS.
- Nonce aleatorio e validacao do nonce pela API.
- Validacao de assinatura, emissor, audiencia e expiracao do token Apple.
- JWT interno HS256 com duracao configuravel.
- Sessao salva no Keychain do dispositivo.
- Endpoints `/api/**` protegidos.
- Tarefas vinculadas ao usuario autenticado.
- Base de verificacao para Google pronta no backend.

## Configuracao externa obrigatoria

1. No Apple Developer, habilite `Sign in with Apple` para o App ID cujo bundle
   identifier e `rodrigoborges.TarefasPOC`.
2. No Xcode, selecione o time correto em Signing & Capabilities e confirme que
   o capability `Sign in with Apple` esta ativo.
3. Crie um segredo JWT de pelo menos 32 bytes:

   ```bash
   openssl rand -hex 32
   ```

4. Grave os parametros no SSM Parameter Store:

   ```bash
   aws ssm put-parameter \
     --name /pocapi/prod/auth/jwt-secret \
     --type SecureString \
     --value 'SUBSTITUA_PELO_SEGREDO' \
     --overwrite

   aws ssm put-parameter \
     --name /pocapi/prod/auth/apple-client-id \
     --type String \
     --value 'rodrigoborges.TarefasPOC' \
     --overwrite
   ```

5. Atualize a policy inline da role `pocapi-ecs-execution-role` com
   `infra/iam/ecs-execution-ssm-policy.json`.
6. Registre uma nova revisao usando `infra/ecs/task-definition.json`.
7. Somente depois de publicar a nova imagem no ECR, atualize o servico ECS para
   a nova revisao.

## Restricoes desta etapa

- O login Google no iOS ainda depende da criacao de um OAuth Client ID para o
  bundle identifier deste app.
- Nao existe refresh token. A sessao atual expira conforme `AUTH_JWT_TTL`.
- O ALB atual usa HTTP. Antes de usar autenticacao fora de uma POC controlada,
  configure dominio, certificado ACM e listener HTTPS.
- Tarefas anteriores a migration V2 ficam sem proprietario e nao aparecem para
  usuarios autenticados.
