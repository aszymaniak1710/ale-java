package pl.degree.alertly.infrastructure.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.degree.alertly.infrastructure.model.enums.Category;
import pl.degree.alertly.infrastructure.model.enums.Level;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_alert_settings")
@Data @NoArgsConstructor @AllArgsConstructor @Accessors(chain = true)
public class UserAlertSettingsEntity {

    @Id
    private String token;

    @Nullable
    private String deviceId;

    @Nonnull
    private Integer radius;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "varchar(20)[]")
    private List<Category> category;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "varchar(20)[]")
    private List<Level> level;

    @Nullable
    @Column(name = "\"from\"")
    private LocalDateTime from;

    @Nullable
    @Column(name = "\"to\"")
    private LocalDateTime to;
}
