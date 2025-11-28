package pl.degree.alertly.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.degree.alertly.application.rest.model.User;
import pl.degree.alertly.infrastructure.model.UserAlertSettingsEntity;
import pl.degree.alertly.infrastructure.model.UserInfoEntity;
import pl.degree.alertly.infrastructure.repo.UserInfoRepository;
import pl.degree.alertly.infrastructure.repo.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;

    @Transactional
    public UserAlertSettingsEntity setUserSettings(UserAlertSettingsEntity alertSettings) {
        return userRepository.save(alertSettings);
    }

    @Transactional
    public UserInfoEntity registerUser(String username, String token) {
        if (userInfoRepository.existsById(token)) {
            throw new IllegalArgumentException("User already exists");
        }
        return userInfoRepository.save(createUserInfo(token, username));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsernames(String token) {
        List<String> usernames = userInfoRepository.findAllUsernames();
        List<String> userFriends = userInfoRepository.findById(token).orElseThrow().getFriends_un();
        return usernames.stream()
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
    public UserInfoEntity updateFriends(String token, List<User> users) {
        UserInfoEntity user = userInfoRepository.findById(token)
                .orElseThrow(() -> new RuntimeException("User not found: " + token));

        List<String> friends = users.stream().filter(User::isFriend).map(User::username).toList();
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

    private UserInfoEntity createUserInfo(String token, String username) {
        UserInfoEntity user = new UserInfoEntity();
        user.setToken(token);
        user.setUsername(username);
        return user;
    }
}
