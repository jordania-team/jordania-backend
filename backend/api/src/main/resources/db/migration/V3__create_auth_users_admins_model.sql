CREATE TYPE role_type AS ENUM ('tutor', 'admin');

DO $$
BEGIN
    IF EXISTS (
        SELECT provider_subject
        FROM usuarios
        GROUP BY provider_subject
        HAVING count(DISTINCT lower(provider)) > 1
    ) THEN
        RAISE EXCEPTION 'provider_subject must be globally unique across providers';
    END IF;
END $$;

CREATE TABLE providers (
    provider_subject VARCHAR(255) PRIMARY KEY,
    provider_name VARCHAR(20) NOT NULL,
    CONSTRAINT uk_providers_name_subject UNIQUE (provider_name, provider_subject)
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    role role_type NOT NULL,
    email VARCHAR(255),
    provider_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_users_provider
        FOREIGN KEY (provider_id) REFERENCES providers(provider_subject)
);

CREATE TABLE admins (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    CONSTRAINT fk_admins_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO providers (provider_subject, provider_name)
SELECT DISTINCT ON (provider_subject)
    provider_subject,
    lower(provider)
FROM usuarios
ORDER BY provider_subject, provider;

INSERT INTO users (id, role, email, provider_id, created_at, updated_at)
SELECT
    id,
    'tutor'::role_type,
    email,
    provider_subject,
    criado_em,
    atualizado_em
FROM usuarios;

CREATE INDEX idx_users_provider_id
    ON users(provider_id);

CREATE INDEX idx_admins_user_id
    ON admins(user_id);
