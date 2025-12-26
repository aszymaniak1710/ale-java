package pl.degree.alertly.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import pl.degree.alertly.infrastructure.model.enums.Category;
import pl.degree.alertly.infrastructure.model.enums.Level;

import java.util.List;

@ConfigurationProperties(prefix = "alertly.default-user-settings")
@Data
public class UserAlertSettingsProperties {
    private Integer closeIncidentDistance;
    private Integer radius;
    private List<Category> category;
    private List<Level> level;
}
