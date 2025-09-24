package se.inera.intyg.webcert.integration.analytics.model;

import java.io.Serializable;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CertificateEventMessage implements Serializable {

    /**
     * Unique identifier for the event.
     */
    String eventId = UUID.randomUUID().toString();
    /**
     * Type of the message, used for routing and processing.
     */
    String type = "certificate.event";
    /**
     * Version of the message schema.
     */
    String schemaVersion = "v1";

    String certificateId;
    CertificateEventMessageType messageType;

}
