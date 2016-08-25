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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket.Status.BEARBETAR;
import static se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket.Status.SIGNERAD;

import java.time.LocalDateTime;
import org.junit.Test;

import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;

public class SignaturTicketTrackerTest {

    private SignaturTicketTracker tracker = new SignaturTicketTracker();

    @Test
    public void prune() {
        for (int i = 1; i < 100; i++) {
            tracker.trackTicket(new SignaturTicket("old-" + i, BEARBETAR, "intygid", 1, LocalDateTime.now(), "1234", LocalDateTime.now().minusMinutes(6)));
            tracker.trackTicket(new SignaturTicket(String.valueOf(i), BEARBETAR, "intygid", 1, LocalDateTime.now(), "1234", LocalDateTime.now()));
        }
        SignaturTicket tracked = tracker.getTicket("1");
        assertEquals(BEARBETAR, tracked.getStatus());
        assertNull(tracker.getTicket("old-1"));
    }

    @Test
    public void updateStatus() {
        tracker.trackTicket(new SignaturTicket("id", BEARBETAR, "intygid", 1, LocalDateTime.now(), "1234", LocalDateTime.now()));
        tracker.updateStatus("id", SIGNERAD);
        SignaturTicket tracked = tracker.getTicket("id");
        assertEquals(SIGNERAD, tracked.getStatus());
    }
}
