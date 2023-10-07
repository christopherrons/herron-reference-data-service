package herron.exchange.referencedataservice.server.external.eurex;

import com.herron.exchange.common.api.common.api.Instrument;
import com.herron.exchange.common.api.common.api.OrderbookData;
import herron.exchange.referencedataservice.server.external.eurex.model.EurexContractData;
import herron.exchange.referencedataservice.server.external.eurex.model.EurexHolidayData;
import herron.exchange.referencedataservice.server.external.eurex.model.EurexProductData;
import herron.exchange.referencedataservice.server.external.eurex.model.EurexTradingHoursData;
import herron.exchange.referencedataservice.server.external.model.ReferenceDataResult;

import java.util.ArrayList;
import java.util.List;

import static herron.exchange.referencedataservice.server.external.eurex.EurexReferenceDataUtil.mapFuture;
import static herron.exchange.referencedataservice.server.external.eurex.EurexReferenceDataUtil.mapOption;

public class EurexReferenceDataHandler {
    private final EurexReferenceDataApiClient client;

    public EurexReferenceDataHandler(EurexReferenceDataApiClient client) {
        this.client = client;
    }

    public ReferenceDataResult getEurexReferenceData() {
        var eurexProductData = client.fetchProductData();
        if (eurexProductData == null) {
            return new ReferenceDataResult(List.of(), List.of());
        }

        EurexTradingHoursData eurexTradingHoursData = client.fetchTradingHourData();
        if (eurexTradingHoursData == null) {
            return new ReferenceDataResult(List.of(), List.of());
        }

        EurexHolidayData eurexHolidayData = client.fetchHolidayData();
        if (eurexHolidayData == null) {
            return new ReferenceDataResult(List.of(), List.of());
        }

        List<EurexContractData> eurexContractDataList = client.fetchContractData(eurexProductData);
        if (eurexContractDataList == null) {
            return new ReferenceDataResult(List.of(), List.of());
        }

        var instruments = mapInstruments(eurexContractDataList, eurexProductData);
        var orderbookData = mapOrderbookData(eurexContractDataList, eurexProductData);
        return new ReferenceDataResult(instruments, orderbookData);
    }

    private List<Instrument> mapInstruments(List<EurexContractData> eurexContractDataList, EurexProductData eurexProductData) {
        return eurexContractDataList.stream()
                .flatMap(eurexContractData -> mapInstruments(eurexContractData, eurexProductData).stream())
                .toList();
    }

    private List<Instrument> mapInstruments(EurexContractData eurexContractData, EurexProductData eurexProductData) {
        var productIdToProductInfo = eurexProductData.getByProductId();
        List<Instrument> instruments = new ArrayList<>();
        for (var contractData : eurexContractData.data().contracts().data()) {
            EurexProductData.ProductInfo productInfo = productIdToProductInfo.get(contractData.productID());
            if (contractData.isOption()) {
                instruments.add(mapOption(contractData, productInfo));
            } else {
                instruments.add(mapFuture(contractData, productInfo));
            }
        }
        return instruments;
    }

    private List<OrderbookData> mapOrderbookData(List<EurexContractData> eurexContractDataList, EurexProductData eurexProductData) {
        return eurexContractDataList.stream()
                .flatMap(eurexContractData -> mapOrderbookData(eurexContractData, eurexProductData).stream())
                .toList();
    }

    private List<OrderbookData> mapOrderbookData(EurexContractData eurexContractData, EurexProductData eurexProductData) {
        var productIdToProductInfo = eurexProductData.getByProductId();
        List<OrderbookData> orderbookData = new ArrayList<>();
        for (var contractData : eurexContractData.data().contracts().data()) {
            EurexProductData.ProductInfo productInfo = productIdToProductInfo.get(contractData.productID());
            orderbookData.add(EurexReferenceDataUtil.mapOrderbookData(contractData, productInfo));
        }
        return orderbookData;
    }

}
