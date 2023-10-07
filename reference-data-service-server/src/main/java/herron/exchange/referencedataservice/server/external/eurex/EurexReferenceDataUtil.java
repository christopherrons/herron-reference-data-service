package herron.exchange.referencedataservice.server.external.eurex;

import com.herron.exchange.common.api.common.api.Instrument;
import com.herron.exchange.common.api.common.api.OrderbookData;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronFutureInstrument;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronOptionInstrument;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronOrderbookData;
import herron.exchange.referencedataservice.server.external.eurex.model.EurexContractData;
import herron.exchange.referencedataservice.server.external.eurex.model.EurexProductData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.herron.exchange.common.api.common.enums.AuctionAlgorithmEnum.DUTCH;
import static com.herron.exchange.common.api.common.enums.MatchingAlgorithmEnum.FIFO;
import static com.herron.exchange.common.api.common.enums.MatchingAlgorithmEnum.PRO_RATA;
import static com.herron.exchange.common.api.common.enums.OptionExerciseTyleEnum.AMERICAN;
import static com.herron.exchange.common.api.common.enums.OptionExerciseTyleEnum.EUROPEAN;
import static com.herron.exchange.common.api.common.enums.OptionTypeEnum.CALL;
import static com.herron.exchange.common.api.common.enums.OptionTypeEnum.PUT;
import static com.herron.exchange.common.api.common.enums.SettlementTypeEnum.CASH;
import static com.herron.exchange.common.api.common.enums.SettlementTypeEnum.PHYSICAL;

public class EurexReferenceDataUtil {
    private static final String MARKET_ID = "EUREX";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static OrderbookData mapOrderbookData(EurexContractData.Contract contract, EurexProductData.ProductInfo productInfo) {
        return ImmutableHerronOrderbookData.builder()
                .orderbookId(contract.isin())
                .instrumentId(contract.isin())
                .tradingCurrency(productInfo.currency())
                .auctionAlgorithm(DUTCH)
                .tickSize(productInfo.tickSize())
                .tickValue(productInfo.tickValue())
                .matchingAlgorithm(productInfo.currency().equals("EUR") ? FIFO : PRO_RATA)
                .build();
    }

    public static Instrument mapFuture(EurexContractData.Contract contract, EurexProductData.ProductInfo productInfo) {
        return ImmutableHerronFutureInstrument.builder()
                .contractSize(contract.contractSize() != 0 ? contract.contractSize() : productInfo.contractSize())
                .instrumentId(contract.isin())
                .marketId(MARKET_ID)
                .currency(productInfo.currency())
                .underLyingInstrumentId(productInfo.underlyingIsin())
                .firstTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .lastTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .maturityDate(LocalDate.parse(contract.expirationDate(), DATE_TIME_FORMATTER))
                .settlementType(contract.isPhysical() ? PHYSICAL : CASH)
                .build();
    }

    public static Instrument mapOption(EurexContractData.Contract contract, EurexProductData.ProductInfo productInfo) {
        return ImmutableHerronOptionInstrument.builder()
                .contractSize(contract.contractSize() != 0 ? contract.contractSize() : productInfo.contractSize())
                .instrumentId(contract.isin())
                .marketId(MARKET_ID)
                .currency(productInfo.currency())
                .underLyingInstrumentId(productInfo.underlyingIsin())
                .firstTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .lastTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .maturityDate(LocalDate.parse(contract.expirationDate(), DATE_TIME_FORMATTER))
                .settlementType(contract.isPhysical() ? PHYSICAL : CASH)
                .strikePrice(Double.parseDouble(contract.strike()))
                .optionType(contract.isCall() ? CALL : PUT)
                .optionExerciseStyle(contract.isAmerican() ? AMERICAN : EUROPEAN)
                .build();
    }

    public static String createHolidayQuery() {
        String data = "    ProductID\\n" +
                "    Product\\n" +
                "    EndOpeningAuction\\n" +
                "    EndClosingAuction\\n" +
                "    EndContinuousTrading\\n" +
                "    StartContinuousTrading";
        return String.format("{\"query\":\"query{Holidays { date data { %s }}}\"}", data);
    }

    public static String createTradingHoursQuery() {
        String data = "    ProductID\\n" +
                "    Product\\n" +
                "    ExchangeHoliday\\n" +
                "    Holiday";
        return String.format("{\"query\":\"query{TradingHours { date data { %s }}}\"}", data);
    }

    public static String createProductQuery() {
        String data = "    ProductID\\n" +
                "    Currency\\n" +
                "    Product\\n" +
                "    UnderlyingName\\n" +
                "    UnderlyingISIN\\n" +
                "    TickSize\\n" +
                "    TickValue\\n" +
                "    ContractSize\\n" +
                "    Underlying";
        return String.format("{\"query\":\"query{ProductInfos { date data { %s }}}\"}", data);
    }

    public static String createContractQuery(String product) {
        String data = " InstrumentID\\n" +
                "    ContractID\\n" +
                "    ProductID\\n" +
                "    ContractSize\\n" +
                "    CallPut\\n" +
                "    Strike\\n" +
                "    SettlementType\\n" +
                "    FirstTradingDate\\n" +
                "    ContractDate\\n" +
                "    ExpirationDate\\n" +
                "    LastTradingDate\\n" +
                "    OptionsDelta\\n" +
                "    ExerciseStyle\\n" +
                "    ISIN\\n" +
                "    PreviousDaySettlementPrice";
        return String.format("{\"query\":\"query{Contracts(filter: {Product: { eq: \\\"%s\\\" }}) { date data { %s }}}\"}", product, data);
    }
}
