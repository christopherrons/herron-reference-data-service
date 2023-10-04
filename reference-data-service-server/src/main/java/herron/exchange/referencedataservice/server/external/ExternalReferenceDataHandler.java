package herron.exchange.referencedataservice.server.external;

import com.herron.exchange.common.api.common.api.Instrument;
import herron.exchange.referencedataservice.server.external.eurex.EurexReferenceDataHandler;

import java.util.List;

public class ExternalReferenceDataHandler {
    private final EurexReferenceDataHandler eurexReferenceDataHandler;

    public ExternalReferenceDataHandler(EurexReferenceDataHandler eurexReferenceDataHandler) {
        this.eurexReferenceDataHandler = eurexReferenceDataHandler;
    }

    public List<Instrument> getInstruments() {
        return eurexReferenceDataHandler.getEurexInstruments();
    }
}
