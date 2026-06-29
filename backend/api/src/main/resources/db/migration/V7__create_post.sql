-- V3__create_posts.sql

CREATE TABLE posts (
    id UUID PRIMARY KEY,

    tutor_id UUID NOT NULL,

    description VARCHAR(1000),

    posted_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    counter_1 INT NOT NULL DEFAULT 0,
    counter_2 INT NOT NULL DEFAULT 0,
    counter_3 INT NOT NULL DEFAULT 0,
    counter_4 INT NOT NULL DEFAULT 0,
    counter_5 INT NOT NULL DEFAULT 0,

    reports_counter INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_posts_tutor
        FOREIGN KEY (tutor_id)
        REFERENCES tutors(id)
);

CREATE INDEX idx_posts_tutor_id
    ON posts(tutor_id);

CREATE INDEX idx_posts_posted_at
    ON posts(posted_at DESC);