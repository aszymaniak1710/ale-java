package pl.degree.alertly.application.factory

import pl.degree.alertly.infrastructure.model.IncidentEntity
import pl.degree.alertly.infrastructure.model.enums.Category
import pl.degree.alertly.infrastructure.model.enums.Level

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class IncidentEntityFactory {
    static createEntity(Category category = Category.SUSPICIOUS, Level level = Level.HIGH) {
        new IncidentEntity(null, category, level, 3.32, 5.32, "123", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", "jezyce")
    }
}
