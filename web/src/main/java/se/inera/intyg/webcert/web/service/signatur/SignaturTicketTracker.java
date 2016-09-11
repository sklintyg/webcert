/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.web.service.signatur;

import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;

@Component
public class SignaturTicketTracker {
    private static final int TICKET_TIMEOUT = 300;

    private static final Logger LOG = LoggerFactory.getLogger(SignaturTicketTracker.class);

    private final Map<String, SignaturTicket> ticketMap = new HashMap<>();

    public synchronized SignaturTicket getTicket(String ticketId) {
        prune();
        return ticketMap.get(ticketId);
    }

    public synchronized void trackTicket(SignaturTicket ticket) {
        LOG.info("Tracking {}", ticket);
        if (!ticketMap.containsKey(ticket.getId())) {
            ticketMap.put(ticket.getId(), ticket);
        } else {
            throw new IllegalArgumentException("Duplicate ticket " + ticket.getId());
        }
    }

    private void prune() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(TICKET_TIMEOUT);
        Iterator<Map.Entry<String, SignaturTicket>> iterator = ticketMap.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().getTimestamp().isBefore(cutoff)) {
                iterator.remove();
            }
        }
    }

    public synchronized SignaturTicket updateStatus(String id, SignaturTicket.Status status) {
        SignaturTicket ticket = getTicket(id);
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
