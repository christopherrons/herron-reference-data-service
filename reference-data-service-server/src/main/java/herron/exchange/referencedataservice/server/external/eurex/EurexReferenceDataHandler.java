package herron.exchange.referencedataservice.server.external.eurex;

import com.herron.exchange.common.api.common.api.referencedata.exchange.Product;
import com.herron.exchange.common.api.common.api.referencedata.instruments.Instrument;
import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.integrations.generator.eurex.EurexReferenceDataApiClient;
import com.herron.exchange.integrations.generator.eurex.model.EurexContractData;
import com.herron.exchange.integrations.generator.eurex.model.EurexHolidayData;
import com.herron.exchange.integrations.generator.eurex.model.EurexProductData;
import com.herron.exchange.integrations.generator.eurex.model.EurexTradingHoursData;
import herron.exchange.referencedataservice.server.external.model.ReferenceDataResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static herron.exchange.referencedataservice.server.external.eurex.EurexReferenceDataUtil.*;
import static herron.exchange.referencedataservice.server.external.model.ReferenceDataResult.emptyResult;

public class EurexReferenceDataHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(EurexReferenceDataHandler.class);
    private final EurexReferenceDataApiClient client;

    public EurexReferenceDataHandler(EurexReferenceDataApiClient client) {
        this.client = client;
    }

    public ReferenceDataResult getEurexReferenceData() {
        LOGGER.info("Fetching Eurex reference data.");

        EurexProductData eurexProductData = client.fetchProductData();
        EurexTradingHoursData eurexTradingHoursData = client.fetchTradingHourData();
        EurexHolidayData eurexHolidayData = client.fetchHolidayData();
        List<EurexContractData> eurexContractDataList = client.fetchContractData(eurexProductData);
        if (isMissingData(eurexProductData, eurexTradingHoursData, eurexHolidayData, eurexContractDataList)) {
            return emptyResult();
        }

        var market = mapMarket(eurexHolidayData);
        var products = eurexProductData.data().productInfos().data().stream()
                .map(p -> mapProduct(market, p, eurexHolidayData))
                .collect(Collectors.toMap(Product::productId, Function.identity()));
        var instruments = mapInstruments(eurexContractDataList, eurexProductData, products).stream()
                .collect(Collectors.toMap(Instrument::instrumentId, Function.identity()));
        var orderbookData = mapOrderbookData(eurexContractDataList, eurexProductData, instruments, eurexTradingHoursData);

        LOGGER.info("Done fetching Eurex reference data.");
        return new ReferenceDataResult(
                List.of(market),
                new ArrayList<>(products.values()),
                new ArrayList<>(instruments.values()),
                orderbookData
        );
    }

    private boolean isMissingData(EurexProductData eurexProductData,
                                  EurexTradingHoursData eurexTradingHoursData,
                                  EurexHolidayData eurexHolidayData,
                                  List<EurexContractData> eurexContractDataList) {
        if (eurexProductData == null) {
            LOGGER.error("No Eurex Products found.");
            return true;
        }

        if (eurexTradingHoursData == null) {
            LOGGER.error("No Eurex trading hours found.");
            return true;
        }

        if (eurexHolidayData == null) {
            LOGGER.error("No Eurex holidays found.");
            return true;
        }

        if (eurexContractDataList == null) {
            LOGGER.error("No Eurex Contracts found.");
            return true;
        }
        return false;
    }

    private List<Instrument> mapInstruments(List<EurexContractData> eurexContractDataList,
                                            EurexProductData eurexProductData,
                                            Map<String, Product> productIdToProduct) {
        var productIdToProductInfo = eurexProductData.getByProductId();
        return eurexContractDataList.stream()
                .flatMap(eurexContractData -> mapInstruments(eurexContractData, productIdToProductInfo, productIdToProduct).stream())
                .toList();
    }

    private List<Instrument> mapInstruments(EurexContractData eurexContractData,
                                            Map<String, EurexProductData.ProductInfo> productIdToProductInfo,
                                            Map<String, Product> productIdToProduct) {
        List<Instrument> instruments = new ArrayList<>();
        for (var contractData : eurexContractData.data().contracts().data()) {
            EurexProductData.ProductInfo productInfo = productIdToProductInfo.get(contractData.productID());
            Product product = productIdToProduct.get(contractData.productID());
            if (contractData.isOption()) {
                instruments.add(mapOption(contractData, productInfo, product));
            } else {
                instruments.add(mapFuture(contractData, productInfo, product));
            }
        }
        return instruments;
    }

    private List<OrderbookData> mapOrderbookData(List<EurexContractData> eurexContractDataList,
                                                 EurexProductData eurexProductData,
                                                 Map<String, Instrument> instrumentIdToInstrument,
                                                 EurexTradingHoursData eurexTradingHoursData) {
        var productIdToProductInfo = eurexProductData.getByProductId();
        var productIdToTradingHours = eurexTradingHoursData.getByProductId();
        return eurexContractDataList.stream()
                .flatMap(eurexContractData -> mapOrderbookData(eurexContractData, productIdToProductInfo, instrumentIdToInstrument, productIdToTradingHours).stream())
                .toList();
    }

    private List<OrderbookData> mapOrderbookData(EurexContractData eurexContractData,
                                                 Map<String, EurexProductData.ProductInfo> productIdToProductInfo,
                                                 Map<String, Instrument> instrumentIdToInstrument,
                                                 Map<String, EurexTradingHoursData.TradingHour> productIdToTradingHours) {

        List<OrderbookData> orderbookData = new ArrayList<>();
        for (var contractData : eurexContractData.data().contracts().data()) {
            EurexProductData.ProductInfo productInfo = productIdToProductInfo.get(contractData.productID());
            var instrument = instrumentIdToInstrument.get(contractData.isin());
            var tradingHours = productIdToTradingHours.get(contractData.productID());
            orderbookData.add(EurexReferenceDataUtil.mapOrderbookData(contractData, productInfo, instrument, tradingHours));
        }

        return orderbookData;
    }
}
