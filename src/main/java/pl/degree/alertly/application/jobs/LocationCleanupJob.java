package pl.degree.alertly.application.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.degree.alertly.infrastructure.repo.UserLocationRepository;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class LocationCleanupJob {

    private final UserLocationRepository userLocationRepository;

    @Scheduled(fixedRate = 60000) // co 60 sekund
    public void cleanOldLocations() {
        Instant threshold = Instant.now().minusSeconds(330);
        int count = userLocationRepository.deleteAllOlderThan(threshold);
        System.out.println("Removed " + count + " expired locations");
    }
}
