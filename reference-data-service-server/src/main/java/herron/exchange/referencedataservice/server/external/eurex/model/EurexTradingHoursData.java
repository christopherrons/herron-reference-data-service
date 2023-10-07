package herron.exchange.referencedataservice.server.external.eurex.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EurexTradingHoursData(@JsonProperty("data") Data data) {

    public record Data(@JsonProperty("Holidays") TradingHours tradingHours) {
    }

    public record TradingHours(@JsonProperty("date") String date,
                               @JsonProperty("data") List<TradingHour> data) {
    }

    public record TradingHour(
            @JsonProperty("StartContinuousTrading") String startContinuousTrading,
            @JsonProperty("ProductID") String productId,
            @JsonProperty("Product") String product,
            @JsonProperty("EndContinuousTrading") String eEndContinuousTrading,
            @JsonProperty("EndClosingAuction") String endClosingAuction,
            @JsonProperty("EndOpeningAuction") String endOpeningAuction) {
    }
}
