# Data Flow

This document describes the data flow of the application.

## Table of Content

* [Visual Flow Chart](#visual-flow-chart): Visual Flow Chart.

## Visual Flow Chart

```mermaid
flowchart LR;
BSC{Server Starts} --->INT


subgraph INT[INTERNAL]
IL[Load Mock Data from Repository]
end


subgraph EXT[EXTERNAL]
EU[Fetch Eurex Referens Data]
end

INT -->|KAFKA|KP
INT --> |TRIGGER| EXT
EXT -->|KAFKA|KP
subgraph KP[Broadcast Reference Data]
end