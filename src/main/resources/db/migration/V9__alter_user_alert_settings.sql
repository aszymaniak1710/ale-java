ALTER TABLE user_alert_settings
    ALTER COLUMN "from" DROP NOT NULL;

ALTER TABLE user_alert_settings
    ALTER COLUMN "to" DROP NOT NULL;