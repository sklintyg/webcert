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
package se.inera.intyg.webcert.web.service.underskrift.tracker;

import org.junit.Test;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.tracker.stub.InMemoryTicketTracker;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus.BEARBETAR;
import static se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus.SIGNERAD;

public class RedisTicketTrackerTest {

    private static final String TICKET_ID = "ticket-1";

    private InMemoryTicketTracker tracker = new InMemoryTicketTracker();

    @Test
    public void prune() {
        for (int i = 1; i < 100; i++) {
            tracker.trackBiljett(buildSignaturBiljett(i, LocalDateTime.now().minusMinutes(6)));
            tracker.trackBiljett(SignaturBiljett.SignaturBiljettBuilder.aSignaturBiljett()
                    .withTicketId(String.valueOf(i))
                    .withStatus(BEARBETAR)
                    .withSkapad(LocalDateTime.now()).build());
        }
        SignaturBiljett tracked = tracker.findBiljett("1");
        assertEquals(BEARBETAR, tracked.getStatus());
        assertNull(tracker.findBiljett("old-1"));
    }

    @Test
    public void updateStatus() {
        tracker.trackBiljett(buildSignaturBiljett(1, LocalDateTime.now()));
        tracker.updateStatus(TICKET_ID + 1, SIGNERAD);
        SignaturBiljett tracked = tracker.findBiljett(TICKET_ID + 1);
        assertEquals(SIGNERAD, tracked.getStatus());
    }

    private SignaturBiljett buildSignaturBiljett(int i, LocalDateTime created) {
        return SignaturBiljett.SignaturBiljettBuilder.aSignaturBiljett()
                .withTicketId(TICKET_ID + i)
                .withIntygsId("old" + i)
                .withSkapad(created)
                .withStatus(BEARBETAR)
                .build();
    }
}
