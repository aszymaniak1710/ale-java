package pl.degree.alertly.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.degree.alertly.infrastructure.model.UserAlertSettingsEntity;
import pl.degree.alertly.infrastructure.repo.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserAlertSettingsEntity save(UserAlertSettingsEntity alertSettings) {
        return userRepository.save(alertSettings);
    }
}
