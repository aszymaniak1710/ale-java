package pl.degree.alertly.application.factory

import groovy.transform.CompileStatic
import pl.degree.alertly.application.rest.model.Location
import pl.degree.alertly.infrastructure.model.UserLocationEntity

import java.time.LocalDateTime

@CompileStatic
class UserLocationEntityFactory {

    static UserLocationEntity createEntity(String token, Location userlocation) {
        new UserLocationEntity(token, userlocation.latitude(), userlocation.longitude(), LocalDateTime.now())
    }
}
