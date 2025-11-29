package pl.degree.alertly.infrastructure.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.degree.alertly.infrastructure.model.UserLocationEntity;

public interface UserLocationRepository extends JpaRepository<UserLocationEntity, String> {
}
