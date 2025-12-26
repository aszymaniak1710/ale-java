package pl.degree.alertly.infrastructure.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.degree.alertly.infrastructure.model.IncidentDeviceId;
import pl.degree.alertly.infrastructure.model.UserMessagingEntity;

public interface UserMessagingRepository extends JpaRepository<UserMessagingEntity, IncidentDeviceId> {
}
