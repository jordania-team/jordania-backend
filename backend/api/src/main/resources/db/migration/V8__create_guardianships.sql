CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE guardianships (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    pet_id UUID NOT NULL,
    tutor_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pet_id) REFERENCES pets(id),
    FOREIGN KEY (tutor_id) REFERENCES tutors(id),

    CONSTRAINT check_not_duplicate CHECK (tutor_id <> pet_id)
);
