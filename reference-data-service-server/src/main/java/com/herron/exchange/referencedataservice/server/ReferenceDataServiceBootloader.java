package com.herron.exchange.referencedataservice.server;

import com.herron.exchange.common.api.common.bootloader.Bootloader;
import com.herron.exchange.common.api.common.enums.KafkaTopicEnum;
import com.herron.exchange.common.api.common.kafka.KafkaBroadcastHandler;
import com.herron.exchange.common.api.common.messages.common.PartitionKey;
import com.herron.exchange.referencedataservice.server.external.ExternalReferenceDataHandler;
import com.herron.exchange.referencedataservice.server.repository.ReferenceDataRepository;

public class ReferenceDataServiceBootloader extends Bootloader {

    private static final PartitionKey REFERENCE_DATA_PARTITION_KEY = new PartitionKey(KafkaTopicEnum.REFERENCE_DATA, 0);
    private final ReferenceDataRepository repository;
    private final ExternalReferenceDataHandler externalReferenceDataHandler;
    private final KafkaBroadcastHandler kafkaBroadcastHandler;

    public ReferenceDataServiceBootloader(ReferenceDataRepository repository,
                                          ExternalReferenceDataHandler externalReferenceDataHandler,
                                          KafkaBroadcastHandler kafkaBroadcastHandler) {
        super("Reference-Data-Service");
        this.repository = repository;
        this.externalReferenceDataHandler = externalReferenceDataHandler;
        this.kafkaBroadcastHandler = kafkaBroadcastHandler;
    }

    @Override
    protected void bootloaderInit() {
        initReferenceDataBroadcasting();
        bootloaderComplete();
    }

    private void initReferenceDataBroadcasting() {
        logger.info("Init reference data loading");
        broadcastFromRepository();
      //  broadCastExternalReferenceData();
        kafkaBroadcastHandler.endBroadCast(REFERENCE_DATA_PARTITION_KEY);
        logger.info("Done reference data loading");
    }

    private void broadcastFromRepository() {
        logger.info("Init broadcasting reference data from repository.");
        repository.getMarkets().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
        repository.getProducts().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
        repository.getInstruments().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
        repository.getOrderbookData().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
        logger.info("Done broadcasting reference data from repository.");
    }

    private void broadCastExternalReferenceData() {
        logger.info("Init broadcasting external reference data.");
        externalReferenceDataHandler.getReferenceData().forEach(referenceDataResult -> {
            referenceDataResult.markets().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
            referenceDataResult.products().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
            referenceDataResult.orderbookData().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
            referenceDataResult.instruments().forEach(message -> kafkaBroadcastHandler.broadcastMessage(REFERENCE_DATA_PARTITION_KEY, message));
        });
        logger.info("Done broadcasting external reference data.");
    }
}
