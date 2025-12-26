package pl.degree.alertly.application.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.degree.alertly.application.service.UserService;
import pl.degree.alertly.infrastructure.model.UserAlertSettingsEntity;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserAlertSettingsController {

    private final UserService userService;

    @PostMapping("/alert/settings")
    public UserAlertSettingsEntity setAlertSettings(@RequestHeader("Authorization") String token,
            @RequestBody UserAlertSettingsEntity alertSettings) {
        alertSettings.setToken(token);
        return userService.setUserSettings(alertSettings);
    }

    @PostMapping("/alert/changedevice")
    public ResponseEntity<UserAlertSettingsEntity> changeDevice(@RequestHeader("Authorization") String token,
                                                                @RequestBody String deviceId) {
        return ResponseEntity.ok(
                userService.changeDevice(token, deviceId));
    }

    @GetMapping("/alert/settings")
    public UserAlertSettingsEntity setAlertSettings(@RequestHeader("Authorization") String token) {
        return userService.getUserSettings(token);
    }
}
