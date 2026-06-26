CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE FOLLOWSTATUS AS ENUM ('pending', 'blocked', 'accepted', 'cancelled', 'rejected');

CREATE TABLE Followings (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    follower_id UUID NOT NULL,
    followed_id UUID NOT NULL,
    request_status FOLLOWSTATUS DEFAULT 'pending',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (follower_id) REFERENCES Tutors(id) ON DELETE CASCADE,
    FOREIGN KEY (followed_id) REFERENCES Tutors(id) ON DELETE CASCADE,

    CONSTRAINT unique_follower_following UNIQUE (follower_id, followed_id),
    CONSTRAINT check_not_self_follow CHECK (follower_id <> followed_id)
);  