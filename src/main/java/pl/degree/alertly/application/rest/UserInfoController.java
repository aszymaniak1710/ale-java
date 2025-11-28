package pl.degree.alertly.application.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.degree.alertly.application.service.UserService;
import pl.degree.alertly.application.rest.model.User;
import pl.degree.alertly.infrastructure.model.UserInfoEntity;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserInfoController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserInfoEntity> registerUser(@RequestHeader("Authorization") String token, @RequestBody String username) {
        return ResponseEntity.ok(
                userService.registerUser(username, token)
        );
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsernames(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.getAllUsernames(token));
    }

    @PostMapping("/setfriends")
    public ResponseEntity<UserInfoEntity> updateFamilyNumbers(@RequestHeader("Authorization") String token, @RequestBody List<User> users) {
        return ResponseEntity.ok(userService.updateFriends(token, users));
    }

    @GetMapping("/familynumbers")
    public ResponseEntity<List<String>> getAllNumbers(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.getAllNumbers(token));
    }

    @PostMapping("/familynumbers")
    public ResponseEntity<UserInfoEntity> setFamilyNumbersForUser(@RequestHeader("Authorization") String token, @RequestBody List<String> numbers) {
        return ResponseEntity.ok(
                userService.updateFamilyNumbers(token, numbers)
        );
    }
}
