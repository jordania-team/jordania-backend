CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE Guardianships (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    pet_id UUID NOT NULL,
    tutor_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pet_id) REFERENCES Pets(id),
    FOREIGN KEY (tutor_id) REFERENCES Tutors(id)
);  