package com.herron.exchange.referencedataservice.server.external.model;


import com.herron.exchange.common.api.common.api.referencedata.instruments.Instrument;
import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.common.api.common.messages.refdata.Market;
import com.herron.exchange.common.api.common.messages.refdata.Product;

import java.util.List;
import java.util.stream.Stream;

public record ReferenceDataResult(List<Market> markets,
                                  List<Product> products,
                                  List<Instrument> instruments,
                                  List<OrderbookData> orderbookData) {
    public static ReferenceDataResult emptyResult() {
        return new ReferenceDataResult(List.of(), List.of(), List.of(), List.of());
    }

    public static ReferenceDataResult concat(ReferenceDataResult r1, ReferenceDataResult r2) {
        return new ReferenceDataResult(
                Stream.concat(r1.markets().stream(), r2.markets().stream()).toList(),
                Stream.concat(r1.products().stream(), r2.products().stream()).toList(),
                Stream.concat(r1.instruments().stream(), r2.instruments().stream()).toList(),
                Stream.concat(r1.orderbookData().stream(), r2.orderbookData().stream()).toList()
        );
    }
}
