package pl.degree.alertly.application.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.degree.alertly.infrastructure.model.IncidentEntity;
import pl.degree.alertly.infrastructure.repo.IncidentRepository;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentEntity save(IncidentEntity incident) {
        return incidentRepository.save(incident);
    }
}
