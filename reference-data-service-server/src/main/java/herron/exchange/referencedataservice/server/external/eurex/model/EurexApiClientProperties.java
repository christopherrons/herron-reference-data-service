package herron.exchange.referencedataservice.server.external.eurex.model;

public record EurexApiClientProperties(String url, String apiKey, int contractRequestLimit) {
}
