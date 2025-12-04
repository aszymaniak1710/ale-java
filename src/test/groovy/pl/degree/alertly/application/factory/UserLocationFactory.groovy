package pl.degree.alertly.application.factory

import groovy.transform.CompileStatic
import pl.degree.alertly.application.rest.model.UserLocation


@CompileStatic
class UserLocationFactory {

    static UserLocation userLocation(String username, Double latitude = 3.21, Double longitude = 4.321){
        new UserLocation(username, latitude, longitude)
    }
}