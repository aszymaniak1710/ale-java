package pl.degree.alertly.application.rest.model;

public record UserLocation(
        String username,
        Double latitude,
        Double longitude
) {
}