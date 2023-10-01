package herron.exchange.referencedataservice.server.external.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EurexContractData(@JsonProperty("data") ContractData data) {

    public record ContractData(@JsonProperty("date") String date,
                               @JsonProperty("data") List<Contract> contractData) {
    }

    public record Contract(@JsonProperty("InstrumentID") int instrumentID,
                           @JsonProperty("ContractID") int contractID,
                           @JsonProperty("ContractSize") double contractSize,
                           @JsonProperty("CallPut") String callPut,
                           @JsonProperty("Strike") String strike,
                           @JsonProperty("SettlementType") String settlementType,
                           @JsonProperty("FirstTradingDate") String firstTradingDate,
                           @JsonProperty("ContractDate") String contractDate,
                           @JsonProperty("ExpirationDate") String expirationDate,
                           @JsonProperty("LastTradingDate") String lastTradingDate,
                           @JsonProperty("OptionsDelta") String optionsDelta,
                           @JsonProperty("PreviousDaySettlementPrice") double previousDaySettlementPrice) {
    }
}
