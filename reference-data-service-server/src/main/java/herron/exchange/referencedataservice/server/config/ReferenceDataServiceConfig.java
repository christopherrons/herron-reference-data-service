package herron.exchange.referencedataservice.server.config;


import com.herron.exchange.common.api.common.api.Instrument;
import com.herron.exchange.common.api.common.api.Market;
import com.herron.exchange.common.api.common.api.OrderbookData;
import com.herron.exchange.common.api.common.cache.ReferenceDataCache;
import com.herron.exchange.common.api.common.enums.AuctionAlgorithmEnum;
import com.herron.exchange.common.api.common.enums.MatchingAlgorithmEnum;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronEquityInstrument;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronMarket;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronOrderbookData;
import com.herron.exchange.common.api.common.model.BusinessCalendar;
import herron.exchange.referencedataservice.server.ReferenceDataServiceBootloader;
import herron.exchange.referencedataservice.server.external.ExternalReferenceDataHandler;
import herron.exchange.referencedataservice.server.external.eurex.EurexReferenceDataApiClient;
import herron.exchange.referencedataservice.server.external.eurex.EurexReferenceDataHandler;
import herron.exchange.referencedataservice.server.repository.ReferenceDataRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class ReferenceDataServiceConfig {

    @Bean
    public Market mockBitstampMarket() {
        return ImmutableHerronMarket.builder()
                .marketId("bitstamp")
                .businessCalendar(BusinessCalendar.defaultWeekendCalendar())
                .build();
    }

    @Bean
    public Instrument mockBTCUSDInstrument(@Qualifier("mockBitstampMarket") Market market) {
        return ImmutableHerronEquityInstrument.builder()
                .instrumentId("stock_btcusd_bitstamp")
                .marketId(market.marketId())
                .currency("usd")
                .firstTradingDate(LocalDate.MIN)
                .lastTradingDate(LocalDate.MAX)
                .build();
    }

    @Bean
    public OrderbookData mockBTCUSD(@Qualifier("mockBTCUSDInstrument") Instrument mockBTCUSDInstrument) {
        return ImmutableHerronOrderbookData.builder()
                .instrumentId(mockBTCUSDInstrument.instrumentId())
                .orderbookId(mockBTCUSDInstrument.instrumentId())
                .matchingAlgorithm(MatchingAlgorithmEnum.FIFO)
                .tradingCurrency(mockBTCUSDInstrument.currency())
                .minTradeVolume(0)
                .auctionAlgorithm(AuctionAlgorithmEnum.DUTCH)
                .build();
    }

    @Bean
    public ReferenceDataCache referenceDataCache(List<Market> markets,
                                                 List<Instrument> instruments,
                                                 List<OrderbookData> orderbookData) {
        var cache = ReferenceDataCache.getCache();
        markets.forEach(cache::addMarket);
        instruments.forEach(cache::addInstrument);
        orderbookData.forEach(cache::addOrderbookData);
        return cache;
    }

    @Bean
    public ReferenceDataRepository referenceDataRepository(ReferenceDataCache cache) {
        return new ReferenceDataRepository(cache);
    }

    @Bean
    public EurexReferenceDataApiClient eurexReferenceDataApiClient(@Value("reference-data.external.eurex.api-key") String apiKey) {
        return new EurexReferenceDataApiClient(apiKey);
    }

    @Bean
    public EurexReferenceDataHandler eurexReferenceDataHandler(EurexReferenceDataApiClient eurexReferenceDataApiClient) {
        return new EurexReferenceDataHandler(eurexReferenceDataApiClient);
    }

    @Bean
    public ExternalReferenceDataHandler externalReferenceDataHandler(EurexReferenceDataHandler eurexReferenceDataHandler) {
        return new ExternalReferenceDataHandler(eurexReferenceDataHandler);
    }

    @Bean(initMethod = "init")
    public ReferenceDataServiceBootloader referenceDataServiceBootloader(ReferenceDataRepository referenceDataRepository,
                                                                         KafkaTemplate<String, Object> kafkaTemplate,
                                                                         ExternalReferenceDataHandler externalReferenceDataHandler) {
        return new ReferenceDataServiceBootloader(referenceDataRepository, kafkaTemplate, externalReferenceDataHandler);
    }
}
