package pl.degree.alertly.application.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.degree.alertly.application.service.UserService;
import pl.degree.alertly.infrastructure.model.UserAlertSettingsEntity;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/alert/settings")
    public UserAlertSettingsEntity setAlertSettings(@RequestBody UserAlertSettingsEntity alertSettings) {
        return userService.save(alertSettings);
    }
}
