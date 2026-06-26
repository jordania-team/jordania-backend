CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE Tutors (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID,
    name VARCHAR(300) NOT NULL,
    username VARCHAR(300) NOT NULL,
    is_private BOOLEAN DEFAULT FALSE,
    img_url VARCHAR(300) NOT NULL,
    birthday TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reports_counter INT DEFAULT 0
    -- ,
    -- FOREIGN KEY (user_id)
);