package herron.exchange.referencedataservice.server.external.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public record EurexContractData(@JsonProperty("data") Data data) {

    public record Data(@JsonProperty("Contracts") Contracts contracts) {
    }

    public record Contracts(@JsonProperty("date") String date,
                            @JsonProperty("data") List<Contract> data) {
    }

    public record Contract(
            @JsonProperty("InstrumentID") String instrumentID,
            @JsonProperty("ContractID") long contractID,
            @JsonProperty("ContractSize") int contractSize,
            @JsonProperty("CallPut") String callPut,
            @JsonProperty("Strike") String strike,
            @JsonProperty("SettlementType") String settlementType,
            @JsonProperty("FirstTradingDate") String firstTradingDate,
            @JsonProperty("ContractDate") String contractDate,
            @JsonProperty("ExpirationDate") String expirationDate,
            @JsonProperty("LastTradingDate") String lastTradingDate,
            @JsonProperty("OptionsDelta") String optionsDelta,
            @JsonProperty("ExerciseStyle") String exerciseStyle,
            @JsonProperty("PreviousDaySettlementPrice") double previousDaySettlementPrice) {

        public boolean isOption() {
            return StringUtils.isNotEmpty(strike);
        }

        public boolean isCall() {
            return callPut.equals("C");
        }

        public boolean isPhysical() {
            return settlementType.equals("PHYSICAL");
        }

        public boolean isAmerican() {
            return callPut.equals("AMERICAN");
        }
    }
}
