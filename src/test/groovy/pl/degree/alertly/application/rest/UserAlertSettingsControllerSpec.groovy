package pl.degree.alertly.application.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import pl.degree.alertly.application.service.UserService
import pl.degree.alertly.infrastructure.model.UserAlertSettingsEntity
import pl.degree.alertly.infrastructure.model.enums.Category
import pl.degree.alertly.infrastructure.repo.UserAlertSettingsRepository
import spock.lang.ResourceLock
import spock.lang.Specification

import static pl.degree.alertly.application.factory.UserAlertSettingsEntityFactory.createEntity


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") @ResourceLock("UserAlertSettingsRepository")
class UserAlertSettingsControllerSpec extends Specification {

    @LocalServerPort
    int port

    WebTestClient client

    @Autowired
    UserAlertSettingsRepository userSettingsRepository
    @Autowired
    UserService userService

    def setup() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:${port}")
                .build()
        userSettingsRepository.deleteAll()
    }

    def "should get and set user alert settings"() {
        given:
        def token = "3321"
        def settings = createEntity(token)
        userSettingsRepository.saveAndFlush(settings)
        when:
        def response = client.get()
                .uri("/user/alert/settings")
                .header("Authorization", token)
                .exchange()
        then:
        response.expectStatus().isOk()
        response.expectBody(UserAlertSettingsEntity).returnResult().responseBody == settings

        when:
        def response2 = client.post()
                .uri("/user/alert/settings")
                .header("Authorization", token)
                .bodyValue(settings.setCategory(List.of(Category.SUSPICIOUS, Category.OTHER)))
                .exchange()
        then:
        response2.expectStatus().isOk()
        def settings2 = userSettingsRepository.findById(token).orElse(null)
        settings2 != null
        settings2.category == List.of(Category.SUSPICIOUS, Category.OTHER)
    }
}
