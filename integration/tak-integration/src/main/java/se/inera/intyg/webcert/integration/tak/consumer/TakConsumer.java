package se.inera.intyg.webcert.integration.tak.consumer;

import se.inera.intyg.webcert.integration.tak.model.TakLogicalAddress;

public interface TakConsumer {
    TakLogicalAddress[] doLookup(String njpdId, String hsaId, String contract);

    String getConnectionPointId();

    String getServiceContractId(String contract);
}
