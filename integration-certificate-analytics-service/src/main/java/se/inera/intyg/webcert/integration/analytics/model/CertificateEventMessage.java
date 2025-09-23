package se.inera.intyg.webcert.integration.analytics.model;

import java.io.Serializable;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CertificateEventMessage implements Serializable {

    String certificateId;
    CertificateEventMessageType type;

}
