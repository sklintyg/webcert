package se.inera.webcert.service.draft;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.inera.webcert.service.draft.dto.SignatureTicket;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class TicketTracker {
    private static final int TICKET_TIMEOUT = 300;

    private static final Logger LOG = LoggerFactory.getLogger(TicketTracker.class);

    private Map<String, SignatureTicket> ticketMap = new HashMap<>();

    public synchronized SignatureTicket getTicket(String ticketId) {
        prune();
        return ticketMap.get(ticketId);
    }

    public synchronized void trackTicket(SignatureTicket ticket) {
        LOG.info("Tracking {}", ticket);
        if (!ticketMap.containsKey(ticket.getId())) {
            ticketMap.put(ticket.getId(), ticket);
        } else {
            throw new IllegalArgumentException("Duplicate ticket " + ticket.getId());
        }
    }

    private void prune() {
        LocalDateTime cutoff = new LocalDateTime().minusSeconds(TICKET_TIMEOUT);
        Iterator<Map.Entry<String, SignatureTicket>> iterator = ticketMap.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().getTimestamp().isBefore(cutoff)) {
                iterator.remove();
            }
        }
    }

    public synchronized SignatureTicket updateStatus(String id, SignatureTicket.Status status) {
        SignatureTicket ticket = getTicket(id);
        if (ticket != null) {
            LOG.info("Updating status {}", ticket);
            ticket = ticket.withStatus(status);
            ticketMap.put(ticket.getId(), ticket);
        } else {
            LOG.info("Updating status failed, no ticket {}", id);
        }
        return ticket;
    }
}
