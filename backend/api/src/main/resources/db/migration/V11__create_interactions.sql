
-- enum com tipos de interacoes possiveis
CREATE TYPE interaction_type AS ENUM (
    'REACTION_1',
    'REACTION_2',
    'REACTION_3',
    'REACTION_4',
    'REACTION_5'
);

-- tabela de interacoes
--tutor reage a post
CREATE TABLE interactions (
    id UUID PRIMARY KEY,

    post_id UUID NOT NULL,
    tutor_id UUID NOT NULL,

    interacted_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    interaction_type VARCHAR(30) NOT NULL,

    CONSTRAINT fk_interactions_post
        FOREIGN KEY (post_id)
        REFERENCES posts(id),

    CONSTRAINT fk_interactions_tutor
        FOREIGN KEY (tutor_id)
        REFERENCES tutors(id),

    CONSTRAINT uk_interactions_post_tutor
        UNIQUE (post_id, tutor_id)
);

-- Busca rápida das interações de um post
CREATE INDEX idx_interactions_post_id
    ON interactions(post_id);

-- Busca rápida das interações feitas por um tutor
CREATE INDEX idx_interactions_tutor_id
    ON interactions(tutor_id);

