package pl.degree.alertly.infrastructure.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class IncidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alert_seq")
    @SequenceGenerator(name = "alert_seq", sequenceName = "alert_sequence", allocationSize = 1)
    private Long id;
    private String category;
    private String level;
    private Double latitude;
    private Double width;
    private String token;
    private LocalDateTime time;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String district;
}
