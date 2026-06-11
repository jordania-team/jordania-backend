CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    provider VARCHAR(20) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    nome VARCHAR(120),
    email VARCHAR(255),
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_usuarios_provider_subject UNIQUE (provider, provider_subject)
);

ALTER TABLE tarefas
    ADD COLUMN usuario_id UUID;

ALTER TABLE tarefas
    ADD CONSTRAINT fk_tarefas_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id);

CREATE INDEX idx_tarefas_usuario_id
    ON tarefas(usuario_id);
