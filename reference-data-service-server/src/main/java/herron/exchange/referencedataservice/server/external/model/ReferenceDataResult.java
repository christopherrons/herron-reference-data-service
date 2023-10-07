package herron.exchange.referencedataservice.server.external.model;

import com.herron.exchange.common.api.common.api.Instrument;
import com.herron.exchange.common.api.common.api.OrderbookData;

import java.util.List;

public record ReferenceDataResult(List<Instrument> instruments, List<OrderbookData> orderbookData) {
}
