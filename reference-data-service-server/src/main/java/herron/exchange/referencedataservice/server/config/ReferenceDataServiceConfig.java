package herron.exchange.referencedataservice.server.config;


import com.herron.exchange.common.api.common.api.referencedata.exchange.Market;
import com.herron.exchange.common.api.common.api.referencedata.exchange.Product;
import com.herron.exchange.common.api.common.api.referencedata.instruments.Instrument;
import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.common.api.common.cache.ReferenceDataCache;
import com.herron.exchange.common.api.common.enums.AuctionAlgorithmEnum;
import com.herron.exchange.common.api.common.enums.MatchingAlgorithmEnum;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronEquityInstrument;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronMarket;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronOrderbookData;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronProduct;
import com.herron.exchange.common.api.common.model.HerronBusinessCalendar;
import com.herron.exchange.common.api.common.model.HerronTradingCalendar;
import herron.exchange.referencedataservice.server.ReferenceDataServiceBootloader;
import herron.exchange.referencedataservice.server.external.ExternalReferenceDataHandler;
import herron.exchange.referencedataservice.server.external.eurex.EurexReferenceDataApiClient;
import herron.exchange.referencedataservice.server.external.eurex.EurexReferenceDataHandler;
import herron.exchange.referencedataservice.server.external.eurex.model.EurexApiClientProperties;
import herron.exchange.referencedataservice.server.repository.ReferenceDataRepository;
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
                .businessCalendar(HerronBusinessCalendar.defaultWeekendCalendar())
                .build();
    }

    @Bean
    public Product mockBitstampProduct(Market market) {
        return ImmutableHerronProduct.builder()
                .market(market)
                .productId(String.format("%s_equity", market.marketId()))
                .currency("usd")
                .build();
    }

    @Bean
    public Instrument mockBTCUSDInstrument(Product product) {
        return ImmutableHerronEquityInstrument.builder()
                .instrumentId(String.format("%s_btcusd", product.productId()))
                .product(product)
                .firstTradingDate(LocalDate.MIN)
                .lastTradingDate(LocalDate.MAX)
                .build();
    }

    @Bean
    public OrderbookData mockBTCUSD(Instrument instrument) {
        return ImmutableHerronOrderbookData.builder()
                .instrument(instrument)
                .orderbookId(instrument.instrumentId())
                .matchingAlgorithm(MatchingAlgorithmEnum.FIFO)
                .tradingCurrency(instrument.currency())
                .auctionAlgorithm(AuctionAlgorithmEnum.DUTCH)
                .tradingCalendar(HerronTradingCalendar.twentyFourSevenTradingCalendar())
                .tickValue(1)
                .tickSize(1)
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
    public EurexApiClientProperties eurexApiClientProperties(@Value("${reference-data.external.eurex.api-key}") String apiKey,
                                                             @Value("${reference-data.external.eurex.api-url}") String url,
                                                             @Value("${reference-data.external.eurex.contractRequestLimit}") int contractRequestLimit) {
        return new EurexApiClientProperties(url, apiKey, contractRequestLimit);
    }

    @Bean
    public EurexReferenceDataApiClient eurexReferenceDataApiClient(EurexApiClientProperties eurexApiClientProperties) {
        return new EurexReferenceDataApiClient(eurexApiClientProperties);
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
