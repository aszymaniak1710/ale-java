CREATE SEQUENCE alert_sequence START 1;

CREATE TABLE alerts (
    id INT PRIMARY KEY DEFAULT nextval('alert_sequence'),
    category VARCHAR(20) NOT NULL,
    level VARCHAR(10) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    width DOUBLE PRECISION NOT NULL,
    token VARCHAR(100) NOT NULL,
    time TIMESTAMP NOT NULL,
    description TEXT
);