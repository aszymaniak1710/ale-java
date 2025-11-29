package pl.degree.alertly.infrastructure.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import pl.degree.alertly.infrastructure.model.enums.Category;
import pl.degree.alertly.infrastructure.model.enums.Level;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
@Data @NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class IncidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alert_seq")
    @SequenceGenerator(name = "alert_seq", sequenceName = "alert_sequence", allocationSize = 1)
    private Long id;
    private Category category;
    private Level level;
    private Double latitude;
    private Double width;
    private String token;
    private LocalDateTime time;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String district;
}
