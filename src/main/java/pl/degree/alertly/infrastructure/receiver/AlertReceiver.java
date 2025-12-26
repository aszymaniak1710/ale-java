package pl.degree.alertly.infrastructure.receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import pl.degree.alertly.infrastructure.model.IncidentEntity;
import pl.degree.alertly.infrastructure.model.enums.Category;
import pl.degree.alertly.infrastructure.model.enums.Level;
import pl.degree.alertly.infrastructure.repo.IncidentRepository;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Kafka Consumer for scraped incidents from Python scraper.
 * Messages are sent with a unique externalId (MD5 hash of title+source)
 * to prevent duplicate incidents when the same event is scraped multiple times.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertReceiver {

    private final IncidentRepository incidentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();  // Create directly

    @KafkaListener(topics = "${spring.kafka.topic.alert}", groupId = "alertly-consumer")
    public void receive(String message) {
        try {
            log.info("[Kafka] Received message: {}", message);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(message, Map.class);

            String externalId = (String) data.get("externalId");
            String token = (String) data.get("token");

            // Check if this scraped incident already exists (by token which contains externalId)
            if (token != null && token.startsWith("SCRAPER_")) {
                if (incidentRepository.existsByToken(token)) {
                    log.info("[Kafka] Skipping duplicate incident: {}", externalId);
                    return;
                }
            }

            // Map to entity
            IncidentEntity entity = new IncidentEntity();
            entity.setCategory(Category.valueOf((String) data.getOrDefault("category", "OTHER")));
            entity.setLevel(Level.valueOf((String) data.getOrDefault("level", "LOW")));
            entity.setLatitude((Double) data.get("latitude"));
            entity.setWidth((Double) data.get("width"));  // width = longitude in your model
            entity.setToken(token);
            entity.setDescription((String) data.get("description"));
            entity.setDistrict((String) data.get("district"));

            // Parse time
            String timeStr = (String) data.get("time");
            if (timeStr != null) {
                timeStr = timeStr.replace("Z", "");
                if (timeStr.contains("T")) {
                    entity.setTime(LocalDateTime.parse(timeStr));
                } else {
                    entity.setTime(LocalDateTime.now());
                }
            } else {
                entity.setTime(LocalDateTime.now());
            }

            // Save to database
            IncidentEntity saved = incidentRepository.save(entity);
            log.info("[Kafka] Saved scraped incident: id={}, token={}", saved.getId(), token);

        } catch (Exception e) {
            log.error("[Kafka] Error processing message: {}", e.getMessage(), e);
        }
    }
}
