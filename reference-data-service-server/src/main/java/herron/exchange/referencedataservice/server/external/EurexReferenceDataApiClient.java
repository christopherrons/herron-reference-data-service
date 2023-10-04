package herron.exchange.referencedataservice.server.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.herron.exchange.common.api.common.api.Instrument;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronFutureInstrument;
import com.herron.exchange.common.api.common.messages.refdata.ImmutableHerronOptionInstrument;
import herron.exchange.referencedataservice.server.external.model.EurexContractData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.herron.exchange.common.api.common.enums.OptionExerciseTyleEnum.AMERICAN;
import static com.herron.exchange.common.api.common.enums.OptionExerciseTyleEnum.EUROPEAN;
import static com.herron.exchange.common.api.common.enums.OptionTypeEnum.CALL;
import static com.herron.exchange.common.api.common.enums.OptionTypeEnum.PUT;
import static com.herron.exchange.common.api.common.enums.SettlementTypeEnum.CASH;
import static com.herron.exchange.common.api.common.enums.SettlementTypeEnum.PHYSICAL;

public class EurexReferenceDataApiClient {
    private static final String API_URL = "https://api.developer.deutsche-boerse.com/accesstot7-referencedata-1-1-0/";
    private static final Logger LOGGER = LoggerFactory.getLogger(EurexReferenceDataApiClient.class);
    private static final ObjectMapper CONTRACT_OBJECT_MAPPER = new ObjectMapper();
    private static final String MARKET_ID = "EUREX";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<Instrument> getEurexInstruments() {
        var eurexReferenceData = fetchContractReferenceData();
        if (eurexReferenceData == null) {
            return List.of();
        }

        return mapEurexContractToInstrument(eurexReferenceData.data().contracts().data());
    }

    private List<Instrument> mapEurexContractToInstrument(List<EurexContractData.Contract> contracts) {
        List<Instrument> eurexInstruments = new ArrayList<>();
        for (var contractData : contracts) {
            if (contractData.isOption()) {
                eurexInstruments.add(mapOption(contractData));
            } else {
                eurexInstruments.add(mapFuture(contractData));
            }
        }
        return eurexInstruments;
    }

    private Instrument mapFuture(EurexContractData.Contract contract) {
        return ImmutableHerronFutureInstrument.builder()
                .contractSize(contract.contractSize())
                .instrumentId(contract.instrumentID())
                .marketId(MARKET_ID)
                .currency("EUR") //FIXME: Get from product info
                .underLyingInstrumentId(contract.instrumentID()) //FIXME: Get from product info
                .firstTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .lastTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .maturityDate(LocalDate.parse(contract.expirationDate(), DATE_TIME_FORMATTER))
                .settlementType(contract.isPhysical() ? PHYSICAL : CASH)
                .build();
    }

    private Instrument mapOption(EurexContractData.Contract contract) {
        return ImmutableHerronOptionInstrument.builder()
                .contractSize(contract.contractSize())
                .instrumentId(contract.instrumentID())
                .marketId(MARKET_ID)
                .currency("EUR") //FIXME: Get from product info
                .underLyingInstrumentId(contract.instrumentID()) //FIXME: Get from product info
                .firstTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .lastTradingDate(LocalDate.parse(contract.firstTradingDate(), DATE_TIME_FORMATTER))
                .maturityDate(LocalDate.parse(contract.expirationDate(), DATE_TIME_FORMATTER))
                .settlementType(contract.isPhysical() ? PHYSICAL : CASH)
                .strikePrice(Double.parseDouble(contract.strike()))
                .optionType(contract.isCall() ? CALL : PUT)
                .optionExerciseStyle(contract.isAmerican() ? AMERICAN : EUROPEAN)
                .build();
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
            byte[] postDataBytes = createContractQuery("FOOG"); //FIXME Add dynamic loading

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

            return CONTRACT_OBJECT_MAPPER.readValue(response.toString(), EurexContractData.class);
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
                "    ExerciseStyle\\n" +
                "    PreviousDaySettlementPrice";
        return String.format("{\"query\":\"query{Contracts(filter: {Product: { eq: \\\"%s\\\" }}) { date data { %s }}}\"}", product, data).
                getBytes(StandardCharsets.UTF_8);
    }
}
