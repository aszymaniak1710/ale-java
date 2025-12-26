CREATE TYPE category_enum AS ENUM ('OTHER', 'FIGHT', 'SUSPICIOUS', 'GUN', 'JUNKIE');

CREATE TYPE level_enum AS ENUM ('HIGH', 'MEDIUM', 'LOW');

CREATE TABLE user_alert_settings (
    token VARCHAR(20) PRIMARY KEY,
    device_id VARCHAR(100),
    radius INT NOT NULL,
    category category_enum[] NOT NULL,
    level level_enum[] NOT NULL,
    "from" TIMESTAMP NOT NULL,
    "to" TIMESTAMP NOT NULL
);

CREATE TABLE user_info (
    uid VARCHAR(20) PRIMARY KEY,
    deviceid VARCHAR(20),
    username VARCHAR(20),
    friends_un TEXT[],
    family_nr TEXT[],
    CONSTRAINT family_nr_digits_only CHECK (
        family_nr IS NULL
            OR array_to_string(family_nr, ',') ~ '^([0-9]{9})(,[0-9]{9})*$'
        )
);