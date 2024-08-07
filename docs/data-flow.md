# Data Flow

This document describes the data flow of the application.

## Table of Content

* [Visual Flow Chart](#visual-flow-chart): Visual Flow Chart.

## Visual Flow Chart

```mermaid
flowchart LR;

subgraph INTERNAL_REFERENCE_DATA_REPOSITORY[INTERNAL]
LOAD_INTERNAL_MOCK_DATA[Load Mock Data from Repository]
end


subgraph EXTERNAL_REFERENCE_DATA[EXTERNAL]
FETCH_EUREX_REFERENCE_DATA[Fetch Eurex Referens Data]
end

INTERNAL_REFERENCE_DATA_REPOSITORY -->KAFKA_REFERENCE_DATA_TOPIC
INTERNAL_REFERENCE_DATA_REPOSITORY --> FETCH_EUREX_REFERENCE_DATA
FETCH_EUREX_REFERENCE_DATA --> KAFKA_REFERENCE_DATA_TOPIC


subgraph KAFKA_REFERENCE_DATA_TOPIC[Reference Data]
KAFKA_REFERENCE_DATA_TOPIC_PARTITION_1[Partition 1] -.- KAFKA_REFERENCE_DATA_TOPIC_PARTITION_N[Partition... N]
end

KAFKA_REFERENCE_DATA_TOPIC --> KAFKA_PRODUCER
subgraph KAFKA_PRODUCER[Kafka Producer]
KAFKA_PRODUCER_PARTITION_1[Partition 1] -.- KAFKA_PRODUCER_PARTITION_N[Partition... N]
end
```