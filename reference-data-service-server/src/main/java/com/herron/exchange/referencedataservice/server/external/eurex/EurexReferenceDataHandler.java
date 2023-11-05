package com.herron.exchange.referencedataservice.server.external.eurex;

import com.herron.exchange.common.api.common.api.referencedata.instruments.Instrument;
import com.herron.exchange.common.api.common.api.referencedata.orderbook.OrderbookData;
import com.herron.exchange.common.api.common.messages.refdata.Market;
import com.herron.exchange.common.api.common.messages.refdata.Product;
import com.herron.exchange.integrations.eurex.EurexReferenceDataApiClient;
import com.herron.exchange.integrations.eurex.model.EurexContractData;
import com.herron.exchange.integrations.eurex.model.EurexHolidayData;
import com.herron.exchange.integrations.eurex.model.EurexProductData;
import com.herron.exchange.integrations.eurex.model.EurexTradingHoursData;
import com.herron.exchange.referencedataservice.server.external.model.ReferenceDataResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.herron.exchange.referencedataservice.server.external.eurex.EurexReferenceDataUtil.*;

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
            return ReferenceDataResult.emptyResult();
        }


        var market = EurexReferenceDataUtil.mapMarket(eurexHolidayData);
        var products = eurexProductData.data().productInfos().data().stream()
                .map(p -> EurexReferenceDataUtil.mapProduct(market, p, eurexHolidayData))
                .collect(Collectors.toMap(Product::productId, Function.identity()));
        var instruments = mapInstruments(eurexContractDataList, eurexProductData, products).stream()
                .collect(Collectors.toMap(Instrument::instrumentId, Function.identity()));
        var orderbookData = mapOrderbookData(eurexContractDataList, eurexProductData, instruments, eurexTradingHoursData);

        ReferenceDataResult referenceData = new ReferenceDataResult(
                List.of(market),
                new ArrayList<>(products.values()),
                new ArrayList<>(instruments.values()),
                orderbookData
        );

        //Underlying contracts are not available at eurex
        ReferenceDataResult mockUnderlyingReference = mapMockUnderlyingEquities(eurexProductData, eurexContractDataList);

        LOGGER.info("Done fetching Eurex reference data.");
        return ReferenceDataResult.concat(referenceData, mockUnderlyingReference);
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

    private ReferenceDataResult mapMockUnderlyingEquities(EurexProductData eurexProductData,
                                                          List<EurexContractData> eurexContractDataList) {
        Map<String, EurexProductData.ProductInfo> productToInfo = eurexProductData.data().productInfos().data().stream()
                .collect(Collectors.toMap(EurexProductData.ProductInfo::product, Function.identity(), (current, other) -> current));
        Set<EurexProductData.ProductInfo> productInfos = eurexContractDataList.stream()
                .flatMap(k -> k.data().contracts().data().stream())
                .filter(c -> productToInfo.containsKey(c.product()))
                .map(c -> productToInfo.get(c.product()))
                .collect(Collectors.toSet());

        Market market = mockEquityMarket();
        Map<String, Product> products = productInfos.stream()
                .map(p -> mockEquityProduct(market, p))
                .collect(Collectors.toMap(Product::productId, Function.identity(), (current, now) -> current));
        Set<Instrument> instruments = productInfos.stream()
                .map(p -> mockEquity(p, products.get(p.underlyingIsin())))
                .collect(Collectors.toSet());
        List<OrderbookData> orderbookData = instruments.stream()
                .map(EurexReferenceDataUtil::mockOrderbookData)
                .toList();

        return new ReferenceDataResult(
                List.of(market),
                new ArrayList<>(products.values()),
                new ArrayList<>(instruments),
                orderbookData
        );
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
            } else if (contractData.isFuture()) {
                instruments.add(mapFuture(contractData, productInfo, product));
            } else {
                LOGGER.error("Unhandled contract type {}", contractData);
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
