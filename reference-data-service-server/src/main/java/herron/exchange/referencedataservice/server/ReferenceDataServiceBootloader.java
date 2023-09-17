package herron.exchange.referencedataservice.server;

import com.herron.exchange.common.api.common.enums.DataLoadingStateEnum;
import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import com.herron.exchange.common.api.common.logging.EventLogger;
import com.herron.exchange.common.api.common.messages.KafkaBroadCastProducer;
import com.herron.exchange.common.api.common.messages.common.HerronDataLoading;
import com.herron.exchange.common.api.common.model.PartitionKey;
import herron.exchange.referencedataservice.server.repository.ReferenceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

public class ReferenceDataServiceBootloader extends KafkaBroadCastProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataServiceBootloader.class);
    private final ReferenceDataRepository repository;

    public ReferenceDataServiceBootloader(ReferenceDataRepository repository, KafkaTemplate<String, Object> kafkaTemplate) {
        super(new PartitionKey(KafkaTopicEnum.HERRON_REFERENCE_DATA, 1), kafkaTemplate, new EventLogger(1));
        this.repository = repository;
    }

    public void init() {
        HerronDataLoading start = new HerronDataLoading(Instant.now().toEpochMilli(), DataLoadingStateEnum.START);
        broadcastMessage(start);
        repository.getMarkets().forEach(this::broadcastMessage);
        repository.getInstruments().forEach(this::broadcastMessage);
        repository.getOrderbookData().forEach(this::broadcastMessage);
        HerronDataLoading done = new HerronDataLoading(Instant.now().toEpochMilli(), DataLoadingStateEnum.DONE);
        broadcastMessage(done);
    }


}
