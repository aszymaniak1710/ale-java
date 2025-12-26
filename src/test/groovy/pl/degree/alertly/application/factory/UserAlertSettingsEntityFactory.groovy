package pl.degree.alertly.application.factory

import groovy.transform.CompileStatic
import pl.degree.alertly.infrastructure.model.UserAlertSettingsEntity
import pl.degree.alertly.infrastructure.model.enums.Category
import pl.degree.alertly.infrastructure.model.enums.Level

import java.time.LocalDateTime

@CompileStatic
class UserAlertSettingsEntityFactory {

    static createEntity(String token, String deviceId = "device", Integer radius = 12, List<Category> category = List.of(Category.FIGHT), List<Level> level = List.of(Level.MEDIUM), LocalDateTime from = null, LocalDateTime to = null) {
        new UserAlertSettingsEntity(token, deviceId, radius, category, level, from, to)
    }
}
