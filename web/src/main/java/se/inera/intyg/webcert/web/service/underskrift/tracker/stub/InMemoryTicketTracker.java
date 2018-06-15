/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.underskrift.tracker.stub;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile(value = "dev")
public class InMemoryTicketTracker implements RedisTicketTracker {

    private static final int TICKET_TIMEOUT_SECONDS = 300;

    private Map<String, SignaturBiljett> map = new ConcurrentHashMap<>();

    @Override
    public void trackBiljett(SignaturBiljett signaturBiljett) {
        map.putIfAbsent(signaturBiljett.getTicketId(), signaturBiljett);
    }

    @Override
    public SignaturBiljett findBiljett(String ticketId) {
        prune();
        return map.get(ticketId);
    }

    @Override
    public SignaturBiljett updateBiljett(SignaturBiljett biljett) {
        return map.replace(biljett.getTicketId(), biljett);
    }

    @Override
    public void updateStatus(String ticketId, SignaturStatus status) {
        if (map.containsKey(ticketId)) {
            SignaturBiljett signaturBiljett = map.get(ticketId);
            signaturBiljett.setStatus(status);
        }
    }

    private void prune() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(TICKET_TIMEOUT_SECONDS);
        map.entrySet().removeIf(stringSignaturBiljettEntry -> stringSignaturBiljettEntry.getValue().getSkapad().isBefore(cutoff));
    }
}
