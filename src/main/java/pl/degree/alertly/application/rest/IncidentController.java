package pl.degree.alertly.application.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.degree.alertly.application.service.IncidentService;
import pl.degree.alertly.infrastructure.model.IncidentEntity;

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
}
