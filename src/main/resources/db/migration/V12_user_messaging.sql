CREATE TABLE user_messaging (
    incident_id INT NOT NULL,
    device_id INT NOT NULL,
    message_quantity VARCHAR(20) NOT NULL,

    CONSTRAINT pk_user_messaging
        PRIMARY KEY (incident_id, user_id),

    CONSTRAINT fk_incident
        FOREIGN KEY (incident_id) REFERENCES alerts(id)
);