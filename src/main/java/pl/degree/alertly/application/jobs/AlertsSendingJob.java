package pl.degree.alertly.application.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.degree.alertly.application.service.AlertService;

@Component
@RequiredArgsConstructor
public class AlertsSendingJob {

    private final AlertService alertService;

    @Scheduled(fixedRate = 60000)
    public void sendProper() {
        alertService.sendProperAlerts();
    }
}
