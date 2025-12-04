package pl.degree.alertly.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.degree.alertly.infrastructure.model.IncidentEntity;
import pl.degree.alertly.infrastructure.model.UserAlertSettingsEntity;
import pl.degree.alertly.infrastructure.repo.IncidentRepository;
import pl.degree.alertly.infrastructure.repo.UserAlertSettingsRepository;
import pl.degree.alertly.infrastructure.repo.UserLocationRepository;
import pl.degree.alertly.infrastructure.sender.AlertSender;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final UserAlertSettingsRepository userAlertSettingsRepository;
    private final UserLocationRepository userLocationRepository;
    private final IncidentRepository incidentRepository;
    private final AlertSender alertSender;

    @Transactional(readOnly = true)
    public void sendProperAlerts() {
        var usersSettings = userAlertSettingsRepository.findAll();
        var notExpiredIncidents = incidentRepository.findByTimeAfter(LocalDateTime.now().minusMinutes(10));
        notExpiredIncidents.forEach(incident -> {
            usersSettings.forEach(user -> {
                if (shouldSendByCategory(incident, user) && shouldSendByLevel(incident, user) && shouldSendByTimePeriod(incident, user) && shouldSendByLocation(incident, user)) {
                    alertSender.send(incident.toString());
                }
            });
        });
    }

    private boolean shouldSendByLocation(IncidentEntity incident, UserAlertSettingsEntity user) {
        var userLocation = userLocationRepository.findById(user.getToken()).orElse(null);
        if (userLocation == null) return false;
        var distance = distance(incident.getLatitude(), incident.getWidth(), userLocation.getLatitude(), userLocation.getLongitude());
        return distance <= user.getRadius();
    }

    private boolean shouldSendByTimePeriod(IncidentEntity incident, UserAlertSettingsEntity user) {
        return incident.getTime().isBefore(user.getTo()) && incident.getTime().isAfter(user.getFrom());
    }

    private boolean shouldSendByLevel(IncidentEntity incident, UserAlertSettingsEntity user) {
        return user.getLevel().contains(incident.getLevel());
    }

    private boolean shouldSendByCategory(IncidentEntity incident, UserAlertSettingsEntity user) {
        return user.getCategory().contains(incident.getCategory());
    }

    private double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
