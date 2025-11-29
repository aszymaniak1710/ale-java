package pl.degree.alertly.application.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.degree.alertly.application.rest.model.Location;
import pl.degree.alertly.application.rest.model.UserLocation;
import pl.degree.alertly.application.service.UserService;
import pl.degree.alertly.infrastructure.model.UserLocationEntity;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserLocationController {

    private final UserService userService;

    @PostMapping("/location")
    public ResponseEntity<UserLocationEntity> location(@RequestHeader("Authorization") String token, @RequestBody Location location) {
        return ResponseEntity.ok(
                userService.newLocation(token, location)
        );
    }

    @GetMapping("/friends/location")
    public ResponseEntity<List<UserLocation>> friendsLocation(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(
                userService.friendsLocation(token)
        );
    }
}
