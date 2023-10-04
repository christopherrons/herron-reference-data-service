package herron.exchange.referencedataservice.server.external.eurex;

import com.herron.exchange.common.api.common.http.HttpRequestHandler;
import herron.exchange.referencedataservice.server.external.eurex.model.EurexContractData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EurexReferenceDataApiClient {
    private static final String API_URL = "https://api.developer.deutsche-boerse.com/accesstot7-referencedata-1-1-0/";
    private static final Logger LOGGER = LoggerFactory.getLogger(EurexReferenceDataApiClient.class);
    private static final String API_KEY_HEADER = "X-DBP-APIKEY";
    private final String apiKey;

    public EurexReferenceDataApiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public EurexContractData fetchContractReferenceData() {
        return HttpRequestHandler.postRequest(API_URL, createContractQuery("FOOG"), Map.of(API_KEY_HEADER, apiKey), EurexContractData.class);
    }

    public static void main(String[] args) {
        var r = new EurexReferenceDataApiClient("e4182894-f5e1-42ae-90a7-7f04d760cd62");
        r.fetchContractReferenceData();
    }

    private static String createContractQuery(String product) {
        String data = " InstrumentID\\n" +
                "    ContractID\\n" +
                "    ContractSize\\n" +
                "    CallPut\\n" +
                "    Strike\\n" +
                "    SettlementType\\n" +
                "    FirstTradingDate\\n" +
                "    ContractDate\\n" +
                "    ExpirationDate\\n" +
                "    LastTradingDate\\n" +
                "    OptionsDelta\\n" +
                "    ExerciseStyle\\n" +
                "    PreviousDaySettlementPrice";
        return String.format("{\"query\":\"query{Contracts(filter: {Product: { eq: \\\"%s\\\" }}) { date data { %s }}}\"}", product, data);
    }
}
