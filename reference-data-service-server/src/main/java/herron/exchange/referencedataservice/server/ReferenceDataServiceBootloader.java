package herron.exchange.referencedataservice.server;

import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import com.herron.exchange.common.api.common.logging.EventLogger;
import com.herron.exchange.common.api.common.messages.KafkaBroadCastProducer;
import com.herron.exchange.common.api.common.model.PartitionKey;
import herron.exchange.referencedataservice.server.external.ExternalReferenceDataHandler;
import herron.exchange.referencedataservice.server.repository.ReferenceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class ReferenceDataServiceBootloader extends KafkaBroadCastProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataServiceBootloader.class);
    private final ReferenceDataRepository repository;
    private final ExternalReferenceDataHandler externalReferenceDataHandler;

    public ReferenceDataServiceBootloader(ReferenceDataRepository repository,
                                          KafkaTemplate<String, Object> kafkaTemplate,
                                          ExternalReferenceDataHandler externalReferenceDataHandler) {
        super(new PartitionKey(KafkaTopicEnum.HERRON_REFERENCE_DATA, 0), kafkaTemplate, new EventLogger(100));
        this.repository = repository;
        this.externalReferenceDataHandler = externalReferenceDataHandler;
    }

    public void init() {
        initReferenceDataBroadcasting();
    }


    private void initReferenceDataBroadcasting() {
        LOGGER.info("Init reference data loading");
        startBroadcasting();
        broadcastFromRepository();
        broadCastExternalReferenceData();
        endBroadcasting();
        LOGGER.info("Done reference data loading");
    }

    private void broadcastFromRepository() {
        LOGGER.info("Init broadcasting reference data from repository.");
        repository.getMarkets().forEach(this::broadcastMessage);
        repository.getProducts().forEach(this::broadcastMessage);
        repository.getInstruments().forEach(this::broadcastMessage);
        repository.getOrderbookData().forEach(this::broadcastMessage);
        LOGGER.info("Done broadcasting reference data from repository.");
    }

    private void broadCastExternalReferenceData() {
        LOGGER.info("Init broadcasting external reference data.");
        externalReferenceDataHandler.getReferenceData().forEach(referenceDataResult -> {
            referenceDataResult.markets().forEach(this::broadcastMessage);
            referenceDataResult.products().forEach(this::broadcastMessage);
            referenceDataResult.orderbookData().forEach(this::broadcastMessage);
            referenceDataResult.instruments().forEach(this::broadcastMessage);
        });
        LOGGER.info("Done broadcasting external reference data.");
    }
}
