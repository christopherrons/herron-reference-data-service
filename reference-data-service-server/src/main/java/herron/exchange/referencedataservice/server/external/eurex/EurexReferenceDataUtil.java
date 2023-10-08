package herron.exchange.referencedataservice.server.external.eurex;

import com.herron.exchange.common.api.common.api.referencedata.exchange.BusinessCalendar;
import com.herron.exchange.common.api.common.api.referencedata.exchange.Market;
import com.herron.exchange.common.api.common.api.referencedata.exchange.Product;
import com.herron.exchange.common.api.common.api.referencedata.exchange.TradingCalendar;
import com.herron.exchange.common.api.common.api.referencedata.instruments.Instrument;
import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.common.api.common.messages.refdata.*;
import com.herron.exchange.common.api.common.model.ImmutableHerronBusinessCalendar;
import com.herron.exchange.common.api.common.model.ImmutableHerronTradingCalendar;
import herron.exchange.referencedataservice.server.external.eurex.model.EurexContractData;
import herron.exchange.referencedataservice.server.external.eurex.model.EurexHolidayData;
import herron.exchange.referencedataservice.server.external.eurex.model.EurexProductData;
import herron.exchange.referencedataservice.server.external.eurex.model.EurexTradingHoursData;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

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
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static Market mapMarket(EurexHolidayData holidayData) {
        return ImmutableHerronMarket.builder()
                .marketId(MARKET_ID)
                .businessCalendar(mapMarketBusinessCalendar(holidayData))
                .build();
    }

    public static BusinessCalendar mapMarketBusinessCalendar(EurexHolidayData holidayData) {
        List<LocalDate> holidays = holidayData.getExchangeHolidays().values().stream()
                .flatMap(Collection::stream)
                .map(holiday -> LocalDate.parse(holiday, DATE_TIME_FORMATTER))
                .toList();
        return ImmutableHerronBusinessCalendar.builder()
                .calendarId(String.format("%s Business Calendar", MARKET_ID))
                .holidays(holidays)
                .build();
    }

    public static Product mapProduct(Market market,
                                     EurexProductData.ProductInfo productInfo,
                                     EurexHolidayData holidayData) {
        return ImmutableHerronProduct.builder()
                .productId(productInfo.productId())
                .market(market)
                .currency(productInfo.currency())
                .businessCalendar(mapProductBusinessCalendar(productInfo, holidayData))
                .build();
    }

    public static BusinessCalendar mapProductBusinessCalendar(EurexProductData.ProductInfo productInfo,
                                                              EurexHolidayData holidayData) {
        List<LocalDate> holidays = Stream.concat(
                        holidayData.getExchangeHolidays(productInfo.productId()).stream(),
                        holidayData.getProductSpecificHolidays(productInfo.productId()).stream()
                )
                .map(holiday -> LocalDate.parse(holiday, DATE_TIME_FORMATTER))
                .toList();
        return ImmutableHerronBusinessCalendar.builder()
                .calendarId(String.format("%s Business Calendar", MARKET_ID))
                .holidays(holidays)
                .build();
    }

    public static OrderbookData mapOrderbookData(EurexContractData.Contract contract,
                                                 EurexProductData.ProductInfo productInfo,
                                                 Instrument instrument,
                                                 EurexTradingHoursData.TradingHour tradingHour) {
        return ImmutableHerronOrderbookData.builder()
                .orderbookId(contract.isin())
                .instrument(instrument)
                .tradingCurrency(productInfo.currency())
                .auctionAlgorithm(DUTCH)
                .tickSize(productInfo.tickSize())
                .tickValue(productInfo.tickValue())
                .matchingAlgorithm(productInfo.currency().equals("EUR") ? FIFO : PRO_RATA)
                .tradingCalendar(mapTradingCalendar(tradingHour))
                .build();
    }

    public static TradingCalendar mapTradingCalendar(EurexTradingHoursData.TradingHour tradingHour) {
        var builder = ImmutableHerronTradingCalendar.builder();
        var startContinuousTrading = toLocalTime(tradingHour.startContinuousTrading());
        var endContinuousTrading = toLocalTime(tradingHour.eEndContinuousTrading());
        var endOpenAuction = toLocalTime(tradingHour.endOpeningAuction());
        endOpenAuction = startContinuousTrading.isAfter(endOpenAuction) ? startContinuousTrading : endOpenAuction;
        var startOpenAuction = endOpenAuction.minusMinutes(5);
        var endClosingAuction = StringUtils.isNotEmpty(tradingHour.endClosingAuction()) ? toLocalTime(tradingHour.endClosingAuction()) : null;

        builder.preTradingHours(new TradingCalendar.TradingHours(LocalTime.MIDNIGHT, startOpenAuction));
        builder.openAuctionTradingHours(new TradingCalendar.TradingHours(startOpenAuction, startContinuousTrading));

        if (endClosingAuction != null) {
            builder.closeAuctionTradingHours(new TradingCalendar.TradingHours(endContinuousTrading, endClosingAuction));
        }
        return builder
                .calendarId(String.format("%s Trading Calendar", tradingHour.productId()))
                .continuousTradingHours(new TradingCalendar.TradingHours(startContinuousTrading, endContinuousTrading))
                .build();
    }

    private static LocalTime toLocalTime(String time) {
        return LocalTime.parse(time, TIME_FORMATTER);
    }

    public static Instrument mapFuture(EurexContractData.Contract contract,
                                       EurexProductData.ProductInfo productInfo,
                                       Product product) {
        return ImmutableHerronFutureInstrument.builder()
                .contractSize(contract.contractSize() != 0 ? contract.contractSize() : productInfo.contractSize())
                .instrumentId(contract.isin())
                .product(product)
                .currency(productInfo.currency())
                .underLyingInstrumentId(productInfo.underlyingIsin())
                .firstTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .lastTradingDate(LocalDate.parse(contract.lastTradingDate(), DATE_TIME_FORMATTER))
                .maturityDate(LocalDate.parse(contract.expirationDate(), DATE_TIME_FORMATTER))
                .settlementType(contract.isPhysical() ? PHYSICAL : CASH)
                .build();
    }

    public static Instrument mapOption(EurexContractData.Contract contract,
                                       EurexProductData.ProductInfo productInfo,
                                       Product product) {
        return ImmutableHerronOptionInstrument.builder()
                .contractSize(contract.contractSize() != 0 ? contract.contractSize() : productInfo.contractSize())
                .instrumentId(contract.isin())
                .product(product)
                .currency(productInfo.currency())
                .underLyingInstrumentId(productInfo.underlyingIsin())
                .firstTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .lastTradingDate(LocalDate.parse(contract.lastTradingDate(), DATE_TIME_FORMATTER))
                .maturityDate(LocalDate.parse(contract.expirationDate(), DATE_TIME_FORMATTER))
                .settlementType(contract.isPhysical() ? PHYSICAL : CASH)
                .strikePrice(Double.parseDouble(contract.strike()))
                .optionType(contract.isCall() ? CALL : PUT)
                .optionExerciseStyle(contract.isAmerican() ? AMERICAN : EUROPEAN)
                .build();
    }

    public static String createTradingHoursQuery() {
        String data = "    ProductID\\n" +
                "    Product\\n" +
                "    EndOpeningAuction\\n" +
                "    EndClosingAuction\\n" +
                "    EndContinuousTrading\\n" +
                "    StartContinuousTrading";
        return String.format("{\"query\":\"query{TradingHours { date data { %s }}}\"}", data);
    }

    public static String createHolidayQuery() {
        String data = "    ProductID\\n" +
                "    Product\\n" +
                "    ExchangeHoliday\\n" +
                "    Holiday";
        return String.format("{\"query\":\"query{Holidays { date data { %s }}}\"}", data);
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