package herron.exchange.referencedataservice.server;

import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import com.herron.exchange.common.api.common.kafka.KafkaBroadcastHandler;
import com.herron.exchange.common.api.common.model.PartitionKey;
import herron.exchange.referencedataservice.server.external.ExternalReferenceDataHandler;
import herron.exchange.referencedataservice.server.repository.ReferenceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceDataServiceBootloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataServiceBootloader.class);
    private static final PartitionKey KEY = new PartitionKey(KafkaTopicEnum.HERRON_REFERENCE_DATA, 0);
    private final ReferenceDataRepository repository;
    private final ExternalReferenceDataHandler externalReferenceDataHandler;
    private final KafkaBroadcastHandler kafkaBroadcastHandler;

    public ReferenceDataServiceBootloader(ReferenceDataRepository repository,
                                          ExternalReferenceDataHandler externalReferenceDataHandler,
                                          KafkaBroadcastHandler kafkaBroadcastHandler) {
        this.repository = repository;
        this.externalReferenceDataHandler = externalReferenceDataHandler;
        this.kafkaBroadcastHandler = kafkaBroadcastHandler;
    }

    public void init() {
        initReferenceDataBroadcasting();
    }


    private void initReferenceDataBroadcasting() {
        LOGGER.info("Init reference data loading");
        broadcastFromRepository();
        broadCastExternalReferenceData();
        kafkaBroadcastHandler.endBroadCast(KEY);
        LOGGER.info("Done reference data loading");
    }

    private void broadcastFromRepository() {
        LOGGER.info("Init broadcasting reference data from repository.");
        repository.getMarkets().forEach(message -> kafkaBroadcastHandler.broadcastMessage(KEY, message));
        repository.getProducts().forEach(message -> kafkaBroadcastHandler.broadcastMessage(KEY, message));
        repository.getInstruments().forEach(message -> kafkaBroadcastHandler.broadcastMessage(KEY, message));
        repository.getOrderbookData().forEach(message -> kafkaBroadcastHandler.broadcastMessage(KEY, message));
        LOGGER.info("Done broadcasting reference data from repository.");
    }

    private void broadCastExternalReferenceData() {
        LOGGER.info("Init broadcasting external reference data.");
        externalReferenceDataHandler.getReferenceData().forEach(referenceDataResult -> {
            referenceDataResult.markets().forEach(message -> kafkaBroadcastHandler.broadcastMessage(KEY, message));
            referenceDataResult.products().forEach(message -> kafkaBroadcastHandler.broadcastMessage(KEY, message));
            referenceDataResult.orderbookData().forEach(message -> kafkaBroadcastHandler.broadcastMessage(KEY, message));
            referenceDataResult.instruments().forEach(message -> kafkaBroadcastHandler.broadcastMessage(KEY, message));
        });
        LOGGER.info("Done broadcasting external reference data.");
    }
}
