package pl.degree.alertly.infrastructure.sender;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertSender {

    //private final KafkaTemplate<String, String> kafkaTemplate;
//    @Value()

    public void send(String message) {
//        var topic = sukaPierdolonaSzmata
        //kafkaTemplate.send(topic, message);
       // System.out.println("Sent message: " + message + " to topic: " + topic);
    }
}
