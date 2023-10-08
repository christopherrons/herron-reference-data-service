package herron.exchange.referencedataservice.server.external.eurex.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record EurexProductData(@JsonProperty("data") Data data) {

    public Map<String, ProductInfo> getByProductId() {
        return data().productInfos().data().stream().collect(Collectors.toMap(EurexProductData.ProductInfo::productId, Function.identity()));
    }

    public record Data(@JsonProperty("ProductInfos") ProductInfos productInfos) {
    }

    public record ProductInfos(@JsonProperty("date") String date,
                               @JsonProperty("data") List<ProductInfo> data) {
    }

    public record ProductInfo(
            @JsonProperty("ProductID") String productId,
            @JsonProperty("Currency") String currency,
            @JsonProperty("Product") String product,
            @JsonProperty("TickValue") double tickValue,
            @JsonProperty("TickSize") double tickSize,
            @JsonProperty("ContractSize") int contractSize,
            @JsonProperty("Underlying") String underlying,
            @JsonProperty("UnderlyingName") String underlyingName,
            @JsonProperty("UnderlyingISIN") String underlyingIsin) {
    }
}
