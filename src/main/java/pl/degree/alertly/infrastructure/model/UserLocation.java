package pl.degree.alertly.infrastructure.model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_location")
@Data @Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserLocation {
    @Id @Nonnull
    private String token;
    @Nonnull Double latitude;
    @Nonnull private Double width;
    @Nonnull private LocalDateTime createTimeStamp;
}
