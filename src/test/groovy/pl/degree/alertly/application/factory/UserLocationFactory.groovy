package pl.degree.alertly.application.factory

import groovy.transform.CompileStatic
import pl.degree.alertly.application.rest.model.Location

@CompileStatic
class UserLocationFactory {

    static userLocation(Double latitude = 3.321, Double longitude = 4.321){
        new Location(latitude, longitude)
    }
}