package pl.degree.alertly.infrastructure.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncidentDeviceId implements Serializable {
    private Long incidentId;
    private String deviceId;
}
