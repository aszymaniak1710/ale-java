CREATE SEQUENCE incidents_sequence START 1;

CREATE TABLE incidents (
    id INT PRIMARY KEY DEFAULT nextval('incidents_sequence'),
    category VARCHAR(20) NOT NULL,
    level VARCHAR(10) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    width DOUBLE PRECISION NOT NULL,
    uid VARCHAR(255) NOT NULL,
    time TIMESTAMP NOT NULL,
    description TEXT,
    district VARCHAR(20)
);