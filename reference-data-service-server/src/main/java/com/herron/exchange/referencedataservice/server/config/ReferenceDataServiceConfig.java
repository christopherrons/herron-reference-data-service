package com.herron.exchange.referencedataservice.server.config;


import com.herron.exchange.common.api.common.api.referencedata.exchange.Market;
import com.herron.exchange.common.api.common.api.referencedata.exchange.Product;
import com.herron.exchange.common.api.common.api.referencedata.instruments.Instrument;
import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.common.api.common.cache.ReferenceDataCache;
import com.herron.exchange.common.api.common.enums.AuctionAlgorithmEnum;
import com.herron.exchange.common.api.common.enums.MatchingAlgorithmEnum;
import com.herron.exchange.common.api.common.kafka.KafkaBroadcastHandler;
import com.herron.exchange.common.api.common.messages.common.DefaultBusinessCalendar;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableDefaultEquityInstrument;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableDefaultMarket;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableDefaultOrderbookData;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableDefaultProduct;
import com.herron.exchange.common.api.common.messages.trading.DefaultTradingCalendar;
import com.herron.exchange.integrations.eurex.EurexReferenceDataApiClient;
import com.herron.exchange.integrations.eurex.model.EurexApiClientProperties;
import com.herron.exchange.referencedataservice.server.ReferenceDataServiceBootloader;
import com.herron.exchange.referencedataservice.server.external.ExternalReferenceDataHandler;
import com.herron.exchange.referencedataservice.server.external.eurex.EurexReferenceDataHandler;
import com.herron.exchange.referencedataservice.server.repository.ReferenceDataRepository;
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
        return ImmutableDefaultMarket.builder()
                .marketId("bitstamp")
                .businessCalendar(DefaultBusinessCalendar.defaultWeekendCalendar())
                .build();
    }

    @Bean
    public Product mockBitstampProduct(Market market) {
        return ImmutableDefaultProduct.builder()
                .market(market)
                .productId(String.format("%s_equity", market.marketId()))
                .currency("usd")
                .build();
    }

    @Bean
    public Instrument mockBTCUSDInstrument(Product product) {
        return ImmutableDefaultEquityInstrument.builder()
                .instrumentId(String.format("%s_btcusd", product.productId()))
                .product(product)
                .firstTradingDate(LocalDate.MIN)
                .lastTradingDate(LocalDate.MAX)
                .build();
    }

    @Bean
    public OrderbookData mockBTCUSD(Instrument instrument) {
        return ImmutableDefaultOrderbookData.builder()
                .instrument(instrument)
                .orderbookId(instrument.instrumentId())
                .matchingAlgorithm(MatchingAlgorithmEnum.FIFO)
                .tradingCurrency(instrument.currency())
                .auctionAlgorithm(AuctionAlgorithmEnum.DUTCH)
                .tradingCalendar(DefaultTradingCalendar.twentyFourSevenTradingCalendar())
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

    @Bean
    public KafkaBroadcastHandler kafkaBroadcastHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaBroadcastHandler(kafkaTemplate);
    }

    @Bean(initMethod = "init")
    public ReferenceDataServiceBootloader referenceDataServiceBootloader(ReferenceDataRepository referenceDataRepository,
                                                                         ExternalReferenceDataHandler externalReferenceDataHandler,
                                                                         KafkaBroadcastHandler kafkaBroadcastHandler) {
        return new ReferenceDataServiceBootloader(referenceDataRepository, externalReferenceDataHandler, kafkaBroadcastHandler);
    }
}
