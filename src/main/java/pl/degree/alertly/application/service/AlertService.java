package pl.degree.alertly.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.degree.alertly.infrastructure.repo.IncidentRepository;
import pl.degree.alertly.infrastructure.repo.UserAlertSettingsRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final UserAlertSettingsRepository userAlertSettingsRepository;
    private final IncidentRepository incidentRepository;
    private final AlertSenderService alertSenderService;

    @Transactional(readOnly = true)
    public void sendProperAlerts() {
        var usersSettings = userAlertSettingsRepository.findAll();
        var notExpiredIncidents = incidentRepository.findByTimeAfter(LocalDateTime.now().minusMinutes(120));
        notExpiredIncidents.forEach(incident -> usersSettings.forEach(user -> {
            try {
                alertSenderService.processAlertForUser(incident, user);
            } catch (RuntimeException e) {
                System.err.println("Error processing alert for user " + user.getToken() + ": " + e.getMessage());
            }
        }));
    }
}