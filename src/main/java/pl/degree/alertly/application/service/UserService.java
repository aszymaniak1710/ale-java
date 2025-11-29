package pl.degree.alertly.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.degree.alertly.application.rest.model.User;
import pl.degree.alertly.application.rest.model.Location;
import pl.degree.alertly.application.rest.model.UserLocation;
import pl.degree.alertly.infrastructure.config.UserAlertSettingsProperties;
import pl.degree.alertly.infrastructure.model.UserAlertSettingsEntity;
import pl.degree.alertly.infrastructure.model.UserInfoEntity;
import pl.degree.alertly.infrastructure.model.UserLocationEntity;
import pl.degree.alertly.infrastructure.repo.UserInfoRepository;
import pl.degree.alertly.infrastructure.repo.UserAlertSettingsRepository;
import pl.degree.alertly.infrastructure.repo.UserLocationRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAlertSettingsProperties userAlertSettingsProperties;
    private final UserAlertSettingsRepository userAlertSettingsRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserLocationRepository userLocationRepository;

    @Transactional
    public UserAlertSettingsEntity setUserSettings(UserAlertSettingsEntity alertSettings) {
        return userAlertSettingsRepository.save(alertSettings);
    }

    @Transactional
    public UserInfoEntity registerUser(String username, String token) {
        if (userInfoRepository.existsById(token)) {
            throw new IllegalArgumentException("User already exists");
        }
        userAlertSettingsRepository.save(createUserSettings(token));
        return userInfoRepository.save(createUserInfo(token, username));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsernames(String token) {
        List<String> usernames = userInfoRepository.findAllUsernames();
        String hostUsername = userInfoRepository.findById(token).orElseThrow().getUsername();
        List<String> userFriends = userInfoRepository.findById(token).orElseThrow().getFriends_un();
        return usernames.stream()
                .filter(user -> !Objects.equals(user, hostUsername))
                .map((username -> new User(username, userFriends.contains(username))))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getAllNumbers(String token) {
        UserInfoEntity user = userInfoRepository.findById(token)
                .orElseThrow(() -> new RuntimeException("User not found: " + token));
        return user.getFamily_nr();
    }

    @Transactional
    public UserInfoEntity updateFriends(String token, List<String> friends) {
        UserInfoEntity user = userInfoRepository.findById(token)
                .orElseThrow(() -> new RuntimeException("User not found: " + token));

        user.setFriends_un(friends);
        return userInfoRepository.save(user);
    }

    @Transactional
    public UserInfoEntity updateFamilyNumbers(String token, List<String> familyNumbers) {
        UserInfoEntity user = userInfoRepository.findById(token)
                .orElseThrow(() -> new RuntimeException("User not found: " + token));

        user.setFamily_nr(familyNumbers);
        return userInfoRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserAlertSettingsEntity getUserSettings(String token) {
        return userAlertSettingsRepository.findById(token).orElseThrow();
    }

    @Transactional
    public UserLocationEntity newLocation(String token, Location location) {
        return userLocationRepository.save(createUserLocationEntity(token, location));
    }

    @Transactional(readOnly = true)
    public List<UserLocation> friendsLocation(String token) {
        List<UserLocation> result = new ArrayList<>();
        for (UserInfoEntity user : userInfoRepository.findUsersWhoHaveMeAsFriend(token)) {
            userLocationRepository.findById(user.getToken())
                    .ifPresent(loc -> result.add(
                            createUserLocation(user.getUsername(), new Location(loc.getLatitude(), loc.getLongitude()))
                    ));
        }
        return result;
    }

    private UserInfoEntity createUserInfo(String token, String username) {
        UserInfoEntity user = new UserInfoEntity();
        user.setToken(token);
        user.setUsername(username);
        return user;
    }

    private UserAlertSettingsEntity createUserSettings(String token) {
        return new UserAlertSettingsEntity()
                .setToken(token)
                .setRadius(userAlertSettingsProperties.getRadius())
                .setCategory(userAlertSettingsProperties.getCategory())
                .setLevel(userAlertSettingsProperties.getLevel());
    }

    private UserLocationEntity createUserLocationEntity(String token, Location location) {
        return new UserLocationEntity()
                .setToken(token)
                .setLatitude(location.latitude())
                .setLongitude(location.longitude())
                .setCreateTimeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

    private UserLocation createUserLocation(String username, Location location) {
        return new UserLocation(username, location.latitude(), location.longitude());
    }


}
