CREATE TABLE user_location (
                               user_token VARCHAR(20) PRIMARY KEY,
                               latitude DOUBLE PRECISION NOT NULL,
                               longitude DOUBLE PRECISION NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_location_token ON user_location(user_token);
CREATE INDEX idx_user_location_created_at ON user_location(created_at);