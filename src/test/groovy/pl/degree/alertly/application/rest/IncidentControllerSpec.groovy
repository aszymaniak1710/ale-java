package pl.degree.alertly.application.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import pl.degree.alertly.application.service.UserService
import pl.degree.alertly.infrastructure.model.IncidentEntity
import pl.degree.alertly.infrastructure.model.enums.Category
import pl.degree.alertly.infrastructure.repo.IncidentRepository
import spock.lang.ResourceLock
import spock.lang.Specification

import static pl.degree.alertly.application.factory.IncidentEntityFactory.createEntity
import static org.assertj.core.api.Assertions.assertThat

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") @ResourceLock("IncidentRepository")
class IncidentControllerSpec extends Specification{

    @LocalServerPort
    int port

    WebTestClient client

    @Autowired
    IncidentRepository incidentRepository
    @Autowired
    UserService userService

    def setup() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:${port}")
                .build()
        incidentRepository.deleteAll()
    }

    def "should get all incidents and add one"() {
        given:
        def incident = createEntity()
        incident = incidentRepository.saveAndFlush(incident)
        when:
        def response = client.get()
                .uri("/incident/all")
                .exchange()
        then:
        response.expectStatus().isOk()
        response.expectBody(new ParameterizedTypeReference<List<IncidentEntity>>() {}).returnResult()
                .getResponseBody() == List.of(incident)

        when:
        def incident2 = createEntity(Category.FIGHT)
        def response2 = client.post()
                .uri("/incident")
                .header("Authorization", "123")
                .bodyValue(incident2)
                .exchange()
        then:
        response2.expectStatus().isOk()
        def incidents = incidentRepository.findAll()
        assertThat(incidents)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
        .containsExactlyInAnyOrderElementsOf(List.of(incident, incident2))
    }
}
