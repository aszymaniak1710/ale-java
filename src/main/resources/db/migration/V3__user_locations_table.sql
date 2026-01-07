CREATE TABLE user_location (
                               uid VARCHAR(255) PRIMARY KEY,
                               latitude DOUBLE PRECISION NOT NULL,
                               longitude DOUBLE PRECISION NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_location_token ON user_location(uid);