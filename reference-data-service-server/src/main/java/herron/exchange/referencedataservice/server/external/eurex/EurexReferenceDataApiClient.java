package herron.exchange.referencedataservice.server.external.eurex;

import com.herron.exchange.common.api.common.http.HttpRequestHandler;
import herron.exchange.referencedataservice.server.external.eurex.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static herron.exchange.referencedataservice.server.external.eurex.EurexReferenceDataUtil.*;

public class EurexReferenceDataApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(EurexReferenceDataApiClient.class);
    private static final String API_KEY_HEADER = "X-DBP-APIKEY";
    private final String apiKey;
    private final String apiUrl;

    public EurexReferenceDataApiClient(EurexApiClientProperties eurexApiClientProperties) {
        this.apiKey = eurexApiClientProperties.apiKey();
        this.apiUrl = eurexApiClientProperties.url();
    }

    public List<EurexContractData> fetchContractData(EurexProductData productData) {
        List<EurexContractData> contractData = new ArrayList<>();
        for (var productInfo : productData.data().productInfos().data()) {
            var result = HttpRequestHandler.postRequest(apiUrl, createContractQuery(productInfo.product()), Map.of(API_KEY_HEADER, apiKey), EurexContractData.class);
            if (result == null) {
                continue;
            }
            contractData.add(result);
        }
        return contractData;
    }

    public EurexProductData fetchProductData() {
        return HttpRequestHandler.postRequest(apiUrl, createProductQuery(), Map.of(API_KEY_HEADER, apiKey), EurexProductData.class);
    }

    public EurexHolidayData fetchHolidayData() {
        return HttpRequestHandler.postRequest(apiUrl, createHolidayQuery(), Map.of(API_KEY_HEADER, apiKey), EurexHolidayData.class);
    }

    public EurexTradingHoursData fetchTradingHourData() {
        return HttpRequestHandler.postRequest(apiUrl, createTradingHoursQuery(), Map.of(API_KEY_HEADER, apiKey), EurexTradingHoursData.class);
    }
}
