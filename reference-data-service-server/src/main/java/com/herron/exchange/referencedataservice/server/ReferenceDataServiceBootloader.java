package com.herron.exchange.referencedataservice.server;

import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import com.herron.exchange.common.api.common.kafka.KafkaBroadcastHandler;
import com.herron.exchange.common.api.common.messages.common.PartitionKey;
import com.herron.exchange.referencedataservice.server.external.ExternalReferenceDataHandler;
import com.herron.exchange.referencedataservice.server.repository.ReferenceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceDataServiceBootloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataServiceBootloader.class);
    private static final PartitionKey REFERENCE_DATA_PARTITION_KEY = new PartitionKey(KafkaTopicEnum.REFERENCE_DATA, 0);
    private final ReferenceDataRepository repository;
    private final ExternalReferenceDataHandler externalReferenceDataHandler;
    private final KafkaBroadcastHandler kafkaBroadcastHandler;
    private final Thread bootloaderThread;

    public ReferenceDataServiceBootloader(ReferenceDataRepository repository,
                                          ExternalReferenceDataHandler externalReferenceDataHandler,
                                          KafkaBroadcastHandler kafkaBroadcastHandler) {
        this.repository = repository;
        this.externalReferenceDataHandler = externalReferenceDataHandler;
        this.kafkaBroadcastHandler = kafkaBroadcastHandler;
        this.bootloaderThread = new Thread(this::initReferenceDataBroadcasting);
    }

    public void init() {
        bootloaderThread.start();
    }


    private void initReferenceDataBroadcasting() {
        LOGGER.info("Init reference data loading");
        broadcastFromRepository();
        broadCastExternalReferenceData();
        kafkaBroadcastHandler.endBroadCast(REFERENCE_DATA_PARTITION_KEY);
        LOGGER.info("Done reference data loading");
        bootloaderThread.interrupt();
    }

    private void broadcastFromRepository() {
        LOGGER.info("Init broadcasting reference data from repository.");
        repository.getMarkets().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
        repository.getProducts().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
        repository.getInstruments().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
        repository.getOrderbookData().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
        LOGGER.info("Done broadcasting reference data from repository.");
    }

    private void broadCastExternalReferenceData() {
        LOGGER.info("Init broadcasting external reference data.");
        externalReferenceDataHandler.getReferenceData().forEach(referenceDataResult -> {
            referenceDataResult.markets().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
            referenceDataResult.products().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
            referenceDataResult.orderbookData().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
            referenceDataResult.instruments().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
        });
        LOGGER.info("Done broadcasting external reference data.");
    }
}
