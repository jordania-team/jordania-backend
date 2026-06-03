CREATE TABLE tarefas (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(120) NOT NULL,
    descricao TEXT,
    concluida BOOLEAN NOT NULL DEFAULT FALSE,
    criada_em TIMESTAMP NOT NULL DEFAULT now()
);