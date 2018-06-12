package se.inera.intyg.webcert.web.service.underskrift.tracker.stub;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile(value = "dev")
public class InMemoryTicketTracker implements RedisTicketTracker {

    private Map<String, SignaturBiljett> map = new ConcurrentHashMap<>();

    @Override
    public void trackBiljett(SignaturBiljett signaturBiljett) {
        map.putIfAbsent(signaturBiljett.getTicketId(), signaturBiljett);
    }

    @Override
    public SignaturBiljett findBiljett(String ticketId) {
        return map.get(ticketId);
    }
}
