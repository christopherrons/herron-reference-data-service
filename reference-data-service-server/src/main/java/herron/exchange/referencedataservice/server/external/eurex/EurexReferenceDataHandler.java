package herron.exchange.referencedataservice.server.external.eurex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.herron.exchange.common.api.common.api.Instrument;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronFutureInstrument;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronOptionInstrument;
import herron.exchange.referencedataservice.server.external.eurex.model.EurexContractData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.herron.exchange.common.api.common.enums.OptionExerciseTyleEnum.AMERICAN;
import static com.herron.exchange.common.api.common.enums.OptionExerciseTyleEnum.EUROPEAN;
import static com.herron.exchange.common.api.common.enums.OptionTypeEnum.CALL;
import static com.herron.exchange.common.api.common.enums.OptionTypeEnum.PUT;
import static com.herron.exchange.common.api.common.enums.SettlementTypeEnum.CASH;
import static com.herron.exchange.common.api.common.enums.SettlementTypeEnum.PHYSICAL;

public class EurexReferenceDataHandler {
    private static final String MARKET_ID = "EUREX";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final EurexReferenceDataApiClient client;

    public EurexReferenceDataHandler(EurexReferenceDataApiClient client) {
        this.client = client;
    }

    public List<Instrument> getEurexInstruments() {
        return getInstruments("tmp");
    }

    private List<Instrument> getInstruments(String tmp) {
        var eurexReferenceData = client.fetchContractReferenceData();
        if (eurexReferenceData == null) {
            return List.of();
        }

        return mapEurexContractToInstrument(eurexReferenceData.data().contracts().data());
    }

    private List<Instrument> mapEurexContractToInstrument(List<EurexContractData.Contract> contracts) {
        List<Instrument> eurexInstruments = new ArrayList<>();
        for (var contractData : contracts) {
            if (contractData.isOption()) {
                eurexInstruments.add(mapOption(contractData));
            } else {
                eurexInstruments.add(mapFuture(contractData));
            }
        }
        return eurexInstruments;
    }

    private Instrument mapFuture(EurexContractData.Contract contract) {
        return ImmutableHerronFutureInstrument.builder()
                .contractSize(contract.contractSize())
                .instrumentId(contract.instrumentID())
                .marketId(MARKET_ID)
                .currency("EUR") //FIXME: Get from product info
                .underLyingInstrumentId(contract.instrumentID()) //FIXME: Get from product info
                .firstTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .lastTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .maturityDate(LocalDate.parse(contract.expirationDate(), DATE_TIME_FORMATTER))
                .settlementType(contract.isPhysical() ? PHYSICAL : CASH)
                .build();
    }

    private Instrument mapOption(EurexContractData.Contract contract) {
        return ImmutableHerronOptionInstrument.builder()
                .contractSize(contract.contractSize())
                .instrumentId(contract.instrumentID())
                .marketId(MARKET_ID)
                .currency("EUR") //FIXME: Get from product info
                .underLyingInstrumentId(contract.instrumentID()) //FIXME: Get from product info
                .firstTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .lastTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .maturityDate(LocalDate.parse(contract.expirationDate(), DATE_TIME_FORMATTER))
                .settlementType(contract.isPhysical() ? PHYSICAL : CASH)
                .strikePrice(Double.parseDouble(contract.strike()))
                .optionType(contract.isCall() ? CALL : PUT)
                .optionExerciseStyle(contract.isAmerican() ? AMERICAN : EUROPEAN)
                .build();
    }
}
