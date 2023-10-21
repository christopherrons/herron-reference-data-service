package com.herron.exchange.referencedataservice.server.repository;


import com.herron.exchange.common.api.common.api.referencedata.exchange.Market;
import com.herron.exchange.common.api.common.api.referencedata.exchange.Product;
import com.herron.exchange.common.api.common.api.referencedata.instruments.Instrument;
import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.common.api.common.cache.ReferenceDataCache;

import java.util.Collection;

public class ReferenceDataRepository {

    private final ReferenceDataCache cache;

    public ReferenceDataRepository(ReferenceDataCache cache) {
        this.cache = cache;
    }

    public Collection<Market> getMarkets() {
        //TODO: This should be loaded from a DB not a cache
        return cache.getMarkets();
    }

    public Collection<Instrument> getInstruments() {
        //TODO: This should be loaded from a DB not a cache
        return cache.getInstruments();
    }

    public Collection<OrderbookData> getOrderbookData() {
        //TODO: This should be loaded from a DB not a cache
        return cache.getOrderbookData();
    }

    public Collection<Product> getProducts() {
        //TODO: This should be loaded from a DB not a cache
        return cache.getProducts();
    }
}
