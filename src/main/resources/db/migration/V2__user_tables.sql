CREATE TABLE user_alert_settings (
    uid VARCHAR(255) PRIMARY KEY,
    device_id VARCHAR(255),
    radius INT NOT NULL,
    category VARCHAR(20)[] NOT NULL,
    level VARCHAR(20)[] NOT NULL,
    "from" TIMESTAMP,
    "to" TIMESTAMP
    CONSTRAINT category_valid CHECK (
        array_to_string(category, ',') ~
        '^(OTHER|FIGHT|SUSPICIOUS|GUN|JUNKIE)(,(OTHER|FIGHT|SUSPICIOUS|GUN|JUNKIE))*$'
        )
    CONSTRAINT level_valid CHECK (
        array_to_string(level, ',') ~
        '^(HIGH|MEDIUM|LOW)(,(HIGH|MEDIUM|LOW))*$'
        )
);

CREATE TABLE user_info (
    uid VARCHAR(255) PRIMARY KEY,
    username VARCHAR(20),
    friends_un TEXT[],
    family_nr TEXT[],
    CONSTRAINT family_nr_digits_only CHECK (
        family_nr IS NULL
            OR array_to_string(family_nr, ',') = ''
            OR array_to_string(family_nr, ',') ~ '^([0-9]{9})(,[0-9]{9})*$'
        )
);