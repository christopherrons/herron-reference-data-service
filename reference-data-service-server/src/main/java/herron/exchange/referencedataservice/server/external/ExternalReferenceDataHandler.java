package herron.exchange.referencedataservice.server.external;

import herron.exchange.referencedataservice.server.external.eurex.EurexReferenceDataHandler;
import herron.exchange.referencedataservice.server.external.model.ReferenceDataResult;

import java.util.List;

public class ExternalReferenceDataHandler {
    private final EurexReferenceDataHandler eurexReferenceDataHandler;

    public ExternalReferenceDataHandler(EurexReferenceDataHandler eurexReferenceDataHandler) {
        this.eurexReferenceDataHandler = eurexReferenceDataHandler;
    }

    public List<ReferenceDataResult> getReferenceData() {
        return List.of(eurexReferenceDataHandler.getEurexReferenceData());
    }
}
