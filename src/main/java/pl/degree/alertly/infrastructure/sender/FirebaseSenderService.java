package pl.degree.alertly.infrastructure.sender;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.degree.alertly.infrastructure.model.IncidentEntity;
import pl.degree.alertly.infrastructure.model.enums.MessageQuantity;

import static pl.degree.alertly.infrastructure.model.enums.MessageQuantity.ONCE;

@Service
@RequiredArgsConstructor
public class FirebaseSenderService {

    public void send(String deviceId, IncidentEntity incident, MessageQuantity quantity) {
        String title = quantity == ONCE ? "Incydent w pobliżu" : "Jesteś blisko incydentu!";
        String body = String.format(
                "%s (%s) w %s.",
                incident.getCategory(),
                incident.getLevel(),
                incident.getDistrict());
        Message message = Message.builder()
                .setToken(deviceId)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                .build();

        String response;
        try {
            response = FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Push sent, id: " + response);
    }
}