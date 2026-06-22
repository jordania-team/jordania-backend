CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE SPECIES AS ENUM ('dog', 'cat', 'fish', 'bird', 'other');

CREATE TABLE Pets (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(300) NOT NULL,
    username VARCHAR(300) NOT NULL,
    img_url VARCHAR(300) NOT NULL,
    birthday TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    species SPECIES NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    -- ,
    -- FOREIGN KEY (user_id)
);