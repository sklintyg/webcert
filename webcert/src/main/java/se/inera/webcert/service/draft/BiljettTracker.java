package se.inera.webcert.service.draft;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.inera.webcert.service.draft.dto.SigneringsBiljett;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class BiljettTracker {
    private static final int BILJETT_TIMEOUT = 300;

    private static final Logger LOG = LoggerFactory.getLogger(BiljettTracker.class);

    private Map<String, SigneringsBiljett> biljettMap = new HashMap<>();

    public synchronized SigneringsBiljett getBiljett(String biljettId) {
        prune();
        return biljettMap.get(biljettId);
    }

    public synchronized void trackBiljett(SigneringsBiljett biljett) {
        LOG.info("Tracking {}", biljett);
        if (!biljettMap.containsKey(biljett.getId())) {
            biljettMap.put(biljett.getId(), biljett);
        } else {
            throw new IllegalArgumentException("Duplicate biljett " + biljett.getId());
        }
    }

    private void prune() {
        LocalDateTime cutoff = new LocalDateTime().minusSeconds(BILJETT_TIMEOUT);
        Iterator<Map.Entry<String, SigneringsBiljett>> iterator = biljettMap.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().getTimestamp().isBefore(cutoff)) {
                iterator.remove();
            }
        }
    }

    public synchronized SigneringsBiljett updateStatusBiljett(String id, SigneringsBiljett.Status status) {
        SigneringsBiljett biljett = getBiljett(id);
        if (biljett != null) {
            LOG.info("Updating status {}", biljett);
            biljett = biljett.withStatus(status);
            biljettMap.put(biljett.getId(), biljett);
        } else {
            LOG.info("Updating status failed, no biljett {}", id);
        }
        return biljett;
    }
}
