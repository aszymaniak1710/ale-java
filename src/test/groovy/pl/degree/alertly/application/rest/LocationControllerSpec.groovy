package pl.degree.alertly.application.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import pl.degree.alertly.application.rest.model.UserLocation
import pl.degree.alertly.application.service.UserService
import pl.degree.alertly.infrastructure.model.IncidentEntity
import pl.degree.alertly.infrastructure.model.UserInfoEntity
import pl.degree.alertly.infrastructure.repo.UserInfoRepository
import pl.degree.alertly.infrastructure.repo.UserLocationRepository
import spock.lang.ResourceLock
import spock.lang.Specification

import static pl.degree.alertly.application.factory.LocationFactory.location
import static pl.degree.alertly.application.factory.UserLocationEntityFactory.createEntity
import static pl.degree.alertly.application.factory.UserLocationFactory.userLocation

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") @ResourceLock("UserLocationRepository")
class LocationControllerSpec extends Specification {

    @LocalServerPort
    int port

    WebTestClient client

    @Autowired
    UserLocationRepository locationRepository
    @Autowired
    UserInfoRepository userInfoRepository
    @Autowired
    UserService userService

    def setup() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:${port}")
                .build()
        locationRepository.deleteAll()
    }

    def "should create and update user location"() {
        given:
        def token = "123"
        def userLocation1 = location()
        def userLocation2 = location(3.21)
        when:
        client.post()
                .uri("/user/location")
                .header("Authorization", token)
                .bodyValue(userLocation1)
                .exchange()
        then:
        def location = locationRepository.findById(token).orElseThrow()
        def entity = createEntity(token, userLocation1)
        location.token == entity.token
        location.latitude == entity.latitude
        location.longitude == entity.longitude
        when:
        client.post()
                .uri("/user/location")
                .header("Authorization", token)
                .bodyValue(userLocation2)
                .exchange()
        then:
        def location2 = locationRepository.findById(token).orElseThrow()
        location2.latitude == createEntity(token, userLocation2).latitude
    }

    def "should provide friends locations"() {
        given:
        def token = "t1"
        userInfoRepository.save(new UserInfoEntity(token, "adam", ["t2"], []))
        userInfoRepository.save(new UserInfoEntity("t2", "ewa", ["t1"], []))
        userInfoRepository.save(new UserInfoEntity("t3", "sasha", ["t1", "t2", "t4"], []))
        userInfoRepository.saveAndFlush(new UserInfoEntity("t4", "tomek", [], []))
        locationRepository.save(createEntity("t3", location()))
        locationRepository.save(createEntity("t4", location(3.52, 7.42)))
        locationRepository.saveAndFlush(createEntity("t2", location(8.23, 5.80)))
        def expectedResult = List.of(userLocation("ewa", 8.23, 5.80), userLocation("sasha", 3.321, 4.321))
        when:
        def response = client.get()
                .uri("/user/friends/location")
                .header("Authorization", token)
                .exchange()
        then:
        response.expectStatus().isOk()
        response.expectBody(new ParameterizedTypeReference<List<UserLocation>>() {}).returnResult()
                .getResponseBody() == expectedResult
    }
}
