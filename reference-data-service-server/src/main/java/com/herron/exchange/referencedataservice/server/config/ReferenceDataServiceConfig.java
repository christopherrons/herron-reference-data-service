package com.herron.exchange.referencedataservice.server.config;


import com.herron.exchange.common.api.common.api.referencedata.instruments.Instrument;
import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.common.api.common.cache.ReferenceDataCache;
import com.herron.exchange.common.api.common.enums.AuctionAlgorithmEnum;
import com.herron.exchange.common.api.common.enums.MatchingAlgorithmEnum;
import com.herron.exchange.common.api.common.kafka.KafkaBroadcastHandler;
import com.herron.exchange.common.api.common.messages.common.BusinessCalendar;
import com.herron.exchange.common.api.common.messages.common.Timestamp;
import com.herron.exchange.common.api.common.messages.refdata.*;
import com.herron.exchange.common.api.common.messages.trading.TradingCalendar;
import com.herron.exchange.integrations.eurex.EurexReferenceDataApiClient;
import com.herron.exchange.integrations.eurex.model.EurexApiClientProperties;
import com.herron.exchange.referencedataservice.server.ReferenceDataServiceBootloader;
import com.herron.exchange.referencedataservice.server.external.ExternalReferenceDataHandler;
import com.herron.exchange.referencedataservice.server.external.eurex.EurexReferenceDataHandler;
import com.herron.exchange.referencedataservice.server.repository.ReferenceDataRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class ReferenceDataServiceConfig {

    @Bean
    public Market mockBitstampMarket() {
        return ImmutableMarket.builder()
                .marketId("bitstamp")
                .businessCalendar(BusinessCalendar.noHolidayCalendar())
                .build();
    }

    @Bean
    public Product mockBitstampProduct(Market mockBitstampMarket) {
        return ImmutableProduct.builder()
                .market(mockBitstampMarket)
                .productId(String.format("%s_equity", mockBitstampMarket.marketId()))
                .build();
    }

    @Bean
    public Instrument mockBTCUSDInstrument(Product mockBitstampProduct) {
        return ImmutableCryptoEquityInstrument.builder()
                .instrumentId(String.format("%s_btcusd", mockBitstampProduct.productId()))
                .product(mockBitstampProduct)
                .firstTradingDate(Timestamp.from(LocalDate.MIN))
                .lastTradingDate(Timestamp.from(LocalDate.MAX))
                .priceModelParameters(ImmutableIntangiblePriceModelParameters.builder().build())
                .currency("usd")
                .token("btc")
                .build();
    }

    @Bean
    public Instrument mockBTCEURInstrument(Product mockBitstampProduct) {
        return ImmutableCryptoEquityInstrument.builder()
                .instrumentId(String.format("%s_btceur", mockBitstampProduct.productId()))
                .product(mockBitstampProduct)
                .currency("eur")
                .token("btc")
                .firstTradingDate(Timestamp.from(LocalDate.MIN))
                .lastTradingDate(Timestamp.from(LocalDate.MAX))
                .priceModelParameters(ImmutableIntangiblePriceModelParameters.builder().build())
                .build();
    }

    @Bean
    public Instrument mockBTCGBPInstrument(Product mockBitstampProduct) {
        return ImmutableCryptoEquityInstrument.builder()
                .instrumentId(String.format("%s_btcgbp", mockBitstampProduct.productId()))
                .product(mockBitstampProduct)
                .currency("gbp")
                .token("btc")
                .firstTradingDate(Timestamp.from(LocalDate.MIN))
                .lastTradingDate(Timestamp.from(LocalDate.MAX))
                .priceModelParameters(ImmutableIntangiblePriceModelParameters.builder().build())
                .build();
    }

    @Bean
    public OrderbookData mockBTCEUROrderbookData(@Qualifier("mockBTCEURInstrument") Instrument mockBTCEURInstrument) {
        return ImmutableDefaultOrderbookData.builder()
                .instrument(mockBTCEURInstrument)
                .orderbookId(mockBTCEURInstrument.instrumentId())
                .matchingAlgorithm(MatchingAlgorithmEnum.FIFO)
                .tradingCurrency(mockBTCEURInstrument.currency())
                .auctionAlgorithm(AuctionAlgorithmEnum.DUTCH)
                .tradingCalendar(TradingCalendar.twentyFourSevenTradingCalendar())
                .tickValue(1)
                .tickSize(1)
                .build();
    }

    @Bean
    public OrderbookData mockBTCGBPOrderbookData(@Qualifier("mockBTCGBPInstrument") Instrument mockBTCGBPInstrument) {
        return ImmutableDefaultOrderbookData.builder()
                .instrument(mockBTCGBPInstrument)
                .orderbookId(mockBTCGBPInstrument.instrumentId())
                .matchingAlgorithm(MatchingAlgorithmEnum.FIFO)
                .tradingCurrency(mockBTCGBPInstrument.currency())
                .auctionAlgorithm(AuctionAlgorithmEnum.DUTCH)
                .tradingCalendar(TradingCalendar.twentyFourSevenTradingCalendar())
                .tickValue(1)
                .tickSize(1)
                .build();
    }

    @Bean
    public OrderbookData mockBTCUSDOrderbookData(@Qualifier("mockBTCUSDInstrument") Instrument mockBTCUSDInstrument) {
        return ImmutableDefaultOrderbookData.builder()
                .instrument(mockBTCUSDInstrument)
                .orderbookId(mockBTCUSDInstrument.instrumentId())
                .matchingAlgorithm(MatchingAlgorithmEnum.FIFO)
                .tradingCurrency(mockBTCUSDInstrument.currency())
                .auctionAlgorithm(AuctionAlgorithmEnum.DUTCH)
                .tradingCalendar(TradingCalendar.twentyFourSevenTradingCalendar())
                .tickValue(1)
                .tickSize(1)
                .build();
    }

    @Bean
    public ReferenceDataCache referenceDataCache(List<Market> markets,
                                                 List<Product> products,
                                                 List<Instrument> instruments,
                                                 List<OrderbookData> orderbookData) {
        var cache = ReferenceDataCache.getCache();
        markets.forEach(cache::addMarket);
        products.forEach(cache::addProduct);
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
                                                                         ExternalReferenceDataHandler externalReferenceDataHandler,
                                                                         KafkaBroadcastHandler kafkaBroadcastHandler) {
        return new ReferenceDataServiceBootloader(referenceDataRepository, externalReferenceDataHandler, kafkaBroadcastHandler);
    }
}
