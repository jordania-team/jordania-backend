CREATE TABLE Tutors (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(300),
    username VARCHAR(300),
    private BOOLEAN DEFAULT FALSE,
    img_url VARCHAR(300),
    birthday TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reports_counter INT DEFAULT 0,
    FOREIGN KEY (user_id)
);