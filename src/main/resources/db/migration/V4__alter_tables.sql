ALTER TABLE user_alert_settings
    ALTER COLUMN category TYPE VARCHAR(20)[];

ALTER TABLE user_alert_settings
    ALTER COLUMN level TYPE VARCHAR(20)[];

ALTER TABLE user_alert_settings
    ADD CONSTRAINT category_valid CHECK (
        array_to_string(category, ',') ~
        '^(OTHER|FIGHT|SUSPICIOUS|GUN|JUNKIE)(,(OTHER|FIGHT|SUSPICIOUS|GUN|JUNKIE))*$'
        );

ALTER TABLE user_alert_settings
    ADD CONSTRAINT level_valid CHECK (
        array_to_string(level, ',') ~
        '^(HIGH|MEDIUM|LOW)(,(HIGH|MEDIUM|LOW))*$'
        );

DROP TYPE IF EXISTS category_enum;
DROP TYPE IF EXISTS level_enum;