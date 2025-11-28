package pl.degree.alertly.application.rest

import groovy.transform.CompileStatic
import pl.degree.alertly.application.rest.model.User

@CompileStatic
class UserFactory {

    static user(String username, Boolean isFriend){
        new User(username, isFriend)
    }
}
