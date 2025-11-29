package pl.degree.alertly.application.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import pl.degree.alertly.application.rest.model.User
import pl.degree.alertly.application.service.UserService
import pl.degree.alertly.infrastructure.model.UserInfoEntity
import pl.degree.alertly.infrastructure.model.enums.Category
import pl.degree.alertly.infrastructure.repo.UserAlertSettingsRepository
import pl.degree.alertly.infrastructure.repo.UserInfoRepository
import spock.lang.ResourceLock
import spock.lang.Specification

import static pl.degree.alertly.application.factory.UserFactory.user

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") @ResourceLock("UserAlertSettingsRepository")
class UserInfoSpec extends Specification {

    @LocalServerPort
    int port

    WebTestClient client

    @Autowired
    UserInfoRepository userInfoRepository
    @Autowired
    UserAlertSettingsRepository userSettingsRepository
    @Autowired
    UserService userService

    def setup() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:${port}")
                .build()
        userSettingsRepository.deleteAll()
        userInfoRepository.deleteAll()
    }

    def "should register user"() {
        given:
        def username = "adam"
        def token = "ddd"
        when:
        def response = client.post()
                .uri("/user/register")
                .header("Authorization", token)
                .bodyValue(username)
                .exchange()
        then:
        response.expectStatus().isOk()
        def user = userInfoRepository.findById(token).orElse(null)
        user != null
        user.username == "adam"
        def userSettings = userSettingsRepository.findById(token).orElseThrow()
        userSettings.getRadius() == 10
        userSettings.getCategory() == List.of(Category.OTHER, Category.FIGHT, Category.SUSPICIOUS)
    }

    def "should set and get family numbers"() {
        given:
        def token = "123"
        def numbers = List.of("123456789", "987654321")
        userInfoRepository.saveAndFlush(userInfoEntity)
        def requestBody = ["123456789", "987654321"]
        when:
        def response = client.post()
                .uri("/user/familynumbers")
                .header("Authorization", token)
                .bodyValue(requestBody)
                .exchange()
        then:
        response.expectStatus().isOk()
        def user = userInfoRepository.findById(token).orElse(null)
        user.family_nr == ["123456789", "987654321"]
        and:
        def response2 = client.get()
                .uri("/user/familynumbers")
                .header("Authorization", token)
                .exchange()
        response2.expectStatus().isOk()
        response2.expectBody(List<String>).returnResult().responseBody == numbers
        where:
        userInfoEntity << [new UserInfoEntity("123", "adam", null, null), new UserInfoEntity("123", "adam", null, ["123456789"])]
    }

    def "should get usernames and set friends"() {
        given:
        def token = "t1"
        userInfoRepository.save(new UserInfoEntity(token, "adam", ["ewa"], []))
        userInfoRepository.save(new UserInfoEntity("t2", "ewa", [], []))
        userInfoRepository.saveAndFlush(new UserInfoEntity("t3", "tomek", [], []))

        when:
        def response = client.get()
                .uri("/user/list")
                .header("Authorization", token)
                .exchange()
        then:
        response.expectStatus().isOk()
        response.expectBodyList(User).contains(user("tomek", false), user("ewa", true))

        when:
        def response2 = client.post()
                .uri("/user/setfriends")
                .header("Authorization", token)
                .bodyValue(List.of("tomek", "ewa"))
                .exchange()
        then:
        response2.expectStatus().isOk()
        def user = userInfoRepository.findById(token).orElse(null)
        user.friends_un == List.of("tomek", "ewa")
    }
}
