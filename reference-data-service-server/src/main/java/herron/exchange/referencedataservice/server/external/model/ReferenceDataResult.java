package herron.exchange.referencedataservice.server.external.model;


import com.herron.exchange.common.api.common.api.referencedata.exchange.Market;
import com.herron.exchange.common.api.common.api.referencedata.exchange.Product;
import com.herron.exchange.common.api.common.api.referencedata.instruments.Instrument;
import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;

import java.util.List;

public record ReferenceDataResult(List<Market> markets,
                                  List<Product> products,
                                  List<Instrument> instruments,
                                  List<OrderbookData> orderbookData) {
    public static ReferenceDataResult emptyResult() {
        return new ReferenceDataResult(List.of(), List.of(), List.of(), List.of());
    }
}
