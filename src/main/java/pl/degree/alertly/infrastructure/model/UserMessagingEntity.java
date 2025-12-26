package pl.degree.alertly.infrastructure.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.degree.alertly.infrastructure.model.enums.MessageQuantity;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_messaging")
public class UserMessagingEntity {
    @EmbeddedId
    private IncidentDeviceId incidentDeviceId;
    @Enumerated(EnumType.STRING)
    private MessageQuantity messageQuantity;
}
