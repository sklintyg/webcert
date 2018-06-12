package se.inera.intyg.webcert.web.service.underskrift.tracker;

import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

public interface RedisTicketTracker {
    void trackBiljett(SignaturBiljett signaturBiljett);
    SignaturBiljett findBiljett(String ticketId);
}
