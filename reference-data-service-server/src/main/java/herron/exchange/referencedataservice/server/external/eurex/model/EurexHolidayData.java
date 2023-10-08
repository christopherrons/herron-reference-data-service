package herron.exchange.referencedataservice.server.external.eurex.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record EurexHolidayData(@JsonProperty("data") Data data) {

    public Set<String> getProductSpecificHolidays(String productId) {
        return getProductSpecificHolidays().getOrDefault(productId, Set.of());
    }

    public Map<String, Set<String>> getProductSpecificHolidays() {
        return data.holidays.data.stream()
                .filter(Holiday::isInstrumentSpecificHoliday)
                .collect(Collectors.groupingBy(Holiday::productId, Collectors.mapping(Holiday::holiday, Collectors.toSet())));
    }

    public Set<String> getExchangeHolidays(String productId) {
        return getExchangeHolidays().getOrDefault(productId, Set.of());
    }

    public Map<String, Set<String>> getExchangeHolidays() {
        return data.holidays.data.stream()
                .filter(Holiday::isExchangeHoliday)
                .collect(Collectors.groupingBy(Holiday::productId, Collectors.mapping(Holiday::holiday, Collectors.toSet())));
    }

    public record Data(@JsonProperty("Holidays") Holidays holidays) {
    }

    public record Holidays(@JsonProperty("date") String date,
                           @JsonProperty("data") List<Holiday> data) {
    }

    public record Holiday(
            @JsonProperty("ExchangeHoliday") String exchangeHolidayFlag,
            @JsonProperty("ProductID") String productId,
            @JsonProperty("Product") String product,
            @JsonProperty("Holiday") String holiday) {

        private boolean isInstrumentSpecificHoliday() {
            return !exchangeHolidayFlag.equals("1");
        }

        private boolean isExchangeHoliday() {
            return !isInstrumentSpecificHoliday();
        }
    }
}
