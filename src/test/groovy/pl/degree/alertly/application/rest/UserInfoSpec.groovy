package pl.degree.alertly.application.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import pl.degree.alertly.application.rest.model.User
import pl.degree.alertly.application.service.UserService
import pl.degree.alertly.infrastructure.model.UserInfoEntity
import pl.degree.alertly.infrastructure.repo.UserInfoRepository
import spock.lang.Specification

import static pl.degree.alertly.application.rest.UserFactory.user

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserInfoSpec extends Specification {

    @LocalServerPort
    int port

    WebTestClient client

    @Autowired
    UserInfoRepository userInfoRepository
    @Autowired
    UserService userService

    def setup() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:${port}")
                .build()
    }

    def "should register user"() {
        given:
        def request = [username: "adam"]
        def token = "ddd"
        when:
        def response = client.post()
                .uri("/user/register")
                .header("Authorization", token)
                .bodyValue(request)
                .exchange()
        then:
        response.expectStatus().isOk()
        def user = userInfoRepository.findById(token).orElse(null)
        user != null
        user.username == "adam"
    }

    def "should set and get family numbers"() {
        given:
        def token = "123"
        def numbers = List.of("123456789", "987654321")
        userInfoRepository.saveAndFlush(new UserInfoEntity(token, "adam", null, null))
        def requestBody = [numbers: ["123456789", "987654321"]]
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
                .bodyValue(List.of(user("tomek", true), user("ewa", true)))
                .exchange()
        then:
        response2.expectStatus().isOk()
        def user = userInfoRepository.findById(token).orElse(null)
        user.friends_un == List.of("tomek", "ewa")
    }
}
