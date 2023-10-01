package herron.exchange.referencedataservice.server.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.herron.exchange.common.api.common.api.Instrument;
import herron.exchange.referencedataservice.server.external.model.EurexContractData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EurexReferenceDataApiClient {
    private static final String API_URL = "https://api.developer.deutsche-boerse.com/accesstot7-referencedata-1-1-0/";
    private static final Logger LOGGER = LoggerFactory.getLogger(EurexReferenceDataApiClient.class);
    private static final ObjectMapper CONTRACT_OBJECT_MAPPER = new ObjectMapper();

    public List<Instrument> getEurexInstruments() {
        var eurexReferenceData = fetchContractReferenceData();
        if (eurexReferenceData == null) {
            return List.of();
        }

        return mapEurexContractToInstrument(eurexReferenceData.data().contractData());
    }

    private List<Instrument> mapEurexContractToInstrument(List<EurexContractData.Contract> contracts) {
        List<Instrument> eurexInstruments = new ArrayList<>();
        for (var contractData : contracts) {
            eurexInstruments.add(
              new
            );
        }
        return eurexInstruments
    }

    private Instrument mapFuture(EurexContractData.Contract contract) {
        return new HerronIn
    }

    private EurexContractData fetchContractReferenceData() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("X-DBP-APIKEY", "e4182894-f5e1-42ae-90a7-7f04d760cd62");
            connection.setDoOutput(true);

            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            byte[] postDataBytes = createContractQuery("FOOG");

            outputStream.write(postDataBytes);
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            LOGGER.info("Response Code: {}", responseCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();

            return CONTRACT_OBJECT_MAPPER.convertValue(response, EurexContractData.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] createContractQuery(String product) {
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
                "    PreviousDaySettlementPrice";
        return String.format("{\"query\":\"query{Contracts(filter: {Product: { eq: \\\"%s\\\" }}) { date data { %s }}}\"}", product, data).
                getBytes(StandardCharsets.UTF_8);
    }
}
