package pl.degree.alertly.infrastructure.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.degree.alertly.infrastructure.model.UserAlertSettingsEntity;

@Repository
public interface UserRepository extends JpaRepository<UserAlertSettingsEntity, String> {
}
