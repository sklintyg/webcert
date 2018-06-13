package se.inera.intyg.webcert.web.service.underskrift.tracker;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

@Service
@Profile(value = "!dev")
public class RedisTicketTrackerImpl implements RedisTicketTracker {


    @Override
    public void trackBiljett(SignaturBiljett signaturBiljett) {

    }

    @Override
    public SignaturBiljett findBiljett(String ticketId) {
        return null;
    }

    @Override
    public SignaturBiljett updateBiljett(SignaturBiljett biljett) {
        return null;
    }
}
