package pl.degree.alertly.infrastructure.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class UserAlertSettingsEntity {

    @Id
    private String userToken;
    Double radius;
    @ElementCollection(targetClass = Category.class)
    @Enumerated(EnumType.STRING)
    List<Category> Category;
    @ElementCollection(targetClass = Level.class)
    @Enumerated(EnumType.STRING)
    List<Level> level;
    LocalDateTime from;
    LocalDateTime to;
}
