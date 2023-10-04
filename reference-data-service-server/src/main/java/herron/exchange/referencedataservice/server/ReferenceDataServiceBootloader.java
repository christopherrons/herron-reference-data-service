package herron.exchange.referencedataservice.server;

import com.herron.exchange.common.api.common.enums.DataLoadingStateEnum;
import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import com.herron.exchange.common.api.common.logging.EventLogger;
import com.herron.exchange.common.api.common.messages.KafkaBroadCastProducer;
import com.herron.exchange.common.api.common.messages.common.HerronDataLoading;
import com.herron.exchange.common.api.common.model.PartitionKey;
import herron.exchange.referencedataservice.server.external.EurexReferenceDataApiClient;
import herron.exchange.referencedataservice.server.repository.ReferenceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

public class ReferenceDataServiceBootloader extends KafkaBroadCastProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataServiceBootloader.class);
    private final ReferenceDataRepository repository;
    private final EurexReferenceDataApiClient eurexReferenceDataApiClient;

    public ReferenceDataServiceBootloader(ReferenceDataRepository repository,
                                          KafkaTemplate<String, Object> kafkaTemplate,
                                          EurexReferenceDataApiClient eurexReferenceDataApiClient) {
        super(new PartitionKey(KafkaTopicEnum.HERRON_REFERENCE_DATA, 0), kafkaTemplate, new EventLogger(1));
        this.repository = repository;
        this.eurexReferenceDataApiClient = eurexReferenceDataApiClient;
    }

    public void init() {
        initReferenceDataBroadcasting();
    }


    private void initReferenceDataBroadcasting() {
        HerronDataLoading start = new HerronDataLoading(Instant.now().toEpochMilli(), DataLoadingStateEnum.START);
        LOGGER.info("Init reference data loading");
        broadcastMessage(start);
        broadcastFromRepository();
        broadCastExternalReferenceData();
        HerronDataLoading done = new HerronDataLoading(Instant.now().toEpochMilli(), DataLoadingStateEnum.DONE);
        broadcastMessage(done);
        LOGGER.info("Done reference data loading");
    }

    private void broadCastExternalReferenceData() {
        eurexReferenceDataApiClient.getEurexInstruments().forEach(this::broadcastMessage);
    }

    private void broadcastFromRepository() {
        LOGGER.info("Init broadcasting reference data from repository.");
        repository.getMarkets().forEach(this::broadcastMessage);
        repository.getInstruments().forEach(this::broadcastMessage);
        repository.getOrderbookData().forEach(this::broadcastMessage);
        LOGGER.info("Done broadcasting reference data from repository.");
    }
}
