package pl.degree.alertly.infrastructure.repo;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pl.degree.alertly.infrastructure.model.UserLocation;

import java.time.Instant;

public interface UserLocationRepository extends JpaRepository<UserLocation, String> {

    @Modifying
    @Transactional
    @Query("DELETE FROM UserLocation u WHERE u.createTimeStamp < :threshold")
    int deleteAllOlderThan(Instant threshold);
}
