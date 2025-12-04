package pl.degree.alertly.infrastructure.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.degree.alertly.infrastructure.model.IncidentEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<IncidentEntity, Long> {
    List<IncidentEntity> findByTimeAfter(LocalDateTime time);
}
