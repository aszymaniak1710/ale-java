package pl.degree.alertly.application.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pl.degree.alertly.application.service.IncidentService;
import pl.degree.alertly.infrastructure.model.IncidentEntity;

import java.util.List;

@RestController
@RequestMapping("/incident")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    public IncidentEntity reportIndicent(@RequestBody IncidentEntity incident) {
        return incidentService.save(incident);
    }

    @GetMapping("/all")
    public List<IncidentEntity> getAllIncidents(){
        return incidentService.getAllIncidents();
    }
}
