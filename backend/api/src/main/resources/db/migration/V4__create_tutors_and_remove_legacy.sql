CREATE TABLE tutors (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR NOT NULL,
    username VARCHAR NOT NULL,
    is_private BOOLEAN NOT NULL DEFAULT false,
    img_url VARCHAR,
    birthday TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    reports_counter INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_tutors_username UNIQUE (username),
    CONSTRAINT uk_tutors_user_id UNIQUE (user_id),
    CONSTRAINT ck_tutors_username CHECK (username ~ '^[a-z0-9][a-z0-9._]{2,29}$'),
    CONSTRAINT ck_tutors_birthday CHECK (birthday <= now()),
    CONSTRAINT ck_tutors_reports_counter CHECK (reports_counter >= 0)
);

DROP TABLE IF EXISTS tarefas CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
