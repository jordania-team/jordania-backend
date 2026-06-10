CREATE TABLE IF NOT EXISTS users (
    id          VARCHAR(36)  NOT NULL,
    email       VARCHAR(255) NOT NULL,
    provider    VARCHAR(50)  NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    name        VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_provider_id UNIQUE (provider_id)
    );