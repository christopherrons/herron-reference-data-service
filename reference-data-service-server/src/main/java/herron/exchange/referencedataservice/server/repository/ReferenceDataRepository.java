package herron.exchange.referencedataservice.server.repository;

import com.herron.exchange.common.api.common.api.Instrument;
import com.herron.exchange.common.api.common.api.Market;
import com.herron.exchange.common.api.common.api.OrderbookData;
import com.herron.exchange.common.api.common.cache.ReferenceDataCache;

import java.util.Collection;

public class ReferenceDataRepository {

    private final ReferenceDataCache cache;

    public ReferenceDataRepository(ReferenceDataCache cache) {
        this.cache = cache;
    }

    public Collection<Market> getMarkets() {
        //TODO: This should be loaded from a DB
        return cache.getMarkets();
    }

    public Collection<Instrument> getInstruments() {
        //TODO: This should be loaded from a DB
        return cache.getInstruments();
    }

    public Collection<OrderbookData> getOrderbookData() {
        //TODO: This should be loaded from a DB
        return cache.getOrderbookData();
    }
}
