package pl.degree.alertly.infrastructure.model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_alert_settings")
@Data @NoArgsConstructor @AllArgsConstructor @Accessors(chain = true)
public class UserAlertSettingsEntity {

    @Id
    private String token;
    @Nonnull
    private Integer radius;
    @Column(columnDefinition = "varchar(20)[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Nonnull
    private List<String> category;
    @Column(columnDefinition = "varchar(20)[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Nonnull
    private List<String> level;
    @Column(name = "\"from\"")
    private LocalDateTime from;
    @Column(name = "\"to\"")
    private LocalDateTime to;
}
