package herron.exchange.referencedataservice.server.config;


import com.herron.exchange.common.api.common.api.Instrument;
import com.herron.exchange.common.api.common.api.Market;
import com.herron.exchange.common.api.common.api.OrderbookData;
import com.herron.exchange.common.api.common.cache.ReferenceDataCache;
import com.herron.exchange.common.api.common.enums.AuctionAlgorithmEnum;
import com.herron.exchange.common.api.common.enums.MatchingAlgorithmEnum;
import com.herron.exchange.common.api.common.messages.refdata.HerronMarket;
import com.herron.exchange.common.api.common.messages.refdata.HerronOrderbookData;
import com.herron.exchange.common.api.common.messages.refdata.HerronStockInstrument;
import com.herron.exchange.common.api.common.model.BusinessCalendar;
import herron.exchange.referencedataservice.server.ReferenceDataServiceBootloader;
import herron.exchange.referencedataservice.server.repository.ReferenceDataRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

@Configuration
public class ReferenceDataServiceConfig {

    @Bean
    public Market mockBitstampMarket() {
        return new HerronMarket("bitstamp", BusinessCalendar.defaultWeekendCalendar());
    }

    @Bean
    public Instrument mockBTCUSDInstrument(@Qualifier("mockBitstampMarket") Market market) {
        return new HerronStockInstrument("stock_btcusd_bitstamp", market.marketId(), "usd");
    }

    @Bean
    public OrderbookData mockBTCUSD(@Qualifier("mockBTCUSDInstrument") Instrument mockBTCUSDInstrument) {
        return new HerronOrderbookData(
                mockBTCUSDInstrument.instrumentId(),
                mockBTCUSDInstrument.instrumentId(),
                MatchingAlgorithmEnum.FIFO,
                mockBTCUSDInstrument.currency(),
                0,
                AuctionAlgorithmEnum.DUTCH
        );
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

    @Bean(initMethod = "init")
    public ReferenceDataServiceBootloader referenceDataServiceBootloader(ReferenceDataRepository referenceDataRepository,
                                                                         KafkaTemplate<String, Object> kafkaTemplate) {
        return new ReferenceDataServiceBootloader(referenceDataRepository, kafkaTemplate);
    }
}
