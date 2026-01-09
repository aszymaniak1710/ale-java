package pl.degree.alertly.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.degree.alertly.infrastructure.config.UserAlertSettingsProperties;
import pl.degree.alertly.infrastructure.model.IncidentDeviceId;
import pl.degree.alertly.infrastructure.model.IncidentEntity;
import pl.degree.alertly.infrastructure.model.UserAlertSettingsEntity;
import pl.degree.alertly.infrastructure.model.UserMessagingEntity;
import pl.degree.alertly.infrastructure.model.enums.MessageQuantity;
import pl.degree.alertly.infrastructure.repo.UserLocationRepository;
import pl.degree.alertly.infrastructure.repo.UserMessagingRepository;
import pl.degree.alertly.infrastructure.sender.FirebaseSenderService;

import static pl.degree.alertly.infrastructure.model.enums.MessageQuantity.ONCE;
import static pl.degree.alertly.infrastructure.model.enums.MessageQuantity.TWICE;

@Service
@RequiredArgsConstructor
public class AlertSenderService {

    private final UserLocationRepository userLocationRepository;
    private final UserMessagingRepository userMessagingRepository;
    private final UserAlertSettingsProperties properties;
    private final FirebaseSenderService firebaseSenderService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAlertForUser(IncidentEntity incident, UserAlertSettingsEntity user) {
        if (isUserInterestedInIncident(incident, user)) {
            if (userInRadius(incident, user)) {
                processUserInRadius(incident, user);
            } else {
                resetUserMessaging(incident, user);
            }
        }
    }

    private void resetUserMessaging(IncidentEntity incident, UserAlertSettingsEntity user) {
        userMessagingRepository.findById(new IncidentDeviceId(incident.getId(), user.getToken()))
                .ifPresent(userMessagingRepository::delete);
    }

    private void processUserInRadius(IncidentEntity incident, UserAlertSettingsEntity user) {
        var userMessage = userMessagingRepository.findById(new IncidentDeviceId(incident.getId(), user.getToken()));
        if (userMessage.isPresent()) {
            if (userMessage.get().getMessageQuantity() == ONCE) {
                if (isUserClose(incident, user.getToken(), user.getRadius())) {
                    send(incident, user.getDeviceId(), TWICE);
                }
            }
        } else {
            send(incident, user.getDeviceId(), ONCE);
        }
    }

    private void send(IncidentEntity incident, String deviceId, MessageQuantity quantity) {
        firebaseSenderService.send(deviceId, incident, quantity);
        userMessagingRepository
                .save(new UserMessagingEntity(new IncidentDeviceId(incident.getId(), deviceId), quantity));
    }

    private boolean isUserClose(IncidentEntity incident, String userToken, Integer userRadius) {
        var closeDistance = properties.getCloseIncidentDistance();
        return userRadius >= closeDistance * 2 && distance(incident, userToken) <= closeDistance;
    }

    private boolean isUserInterestedInIncident(IncidentEntity incident, UserAlertSettingsEntity user) {
        return shouldSendByCategory(incident, user)
                && shouldSendByLevel(incident, user)
                && shouldSendByTimePeriod(incident, user);
    }

    private boolean userInRadius(IncidentEntity incident, UserAlertSettingsEntity user) {
        var userLocation = userLocationRepository.findById(user.getToken()).orElse(null);
        if (userLocation == null)
            return false;
        var distance = distance(incident.getLatitude(), incident.getWidth(), userLocation.getLatitude(),
                userLocation.getLongitude());
        return distance <= user.getRadius();
    }

    private double distance(IncidentEntity incident, String userToken) {
        var userLocation = userLocationRepository.findById(userToken).orElseThrow();
        return distance(incident.getLatitude(), incident.getWidth(), userLocation.getLatitude(),
                userLocation.getLongitude());
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
