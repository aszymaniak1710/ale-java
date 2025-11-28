package pl.degree.alertly.application.rest.model;

public record UserLocation(
    String token,
    Double latitude,
    Double longitude
) {
}
