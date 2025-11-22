package pl.degree.alertly.infrastructure.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.degree.alertly.infrastructure.model.IncidentEntity;

@Repository
public interface IncidentRepository extends JpaRepository<IncidentEntity, Long> {
}
