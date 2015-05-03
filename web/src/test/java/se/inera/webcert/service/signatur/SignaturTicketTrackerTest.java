package se.inera.webcert.service.signatur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static se.inera.webcert.service.signatur.dto.SignaturTicket.Status.BEARBETAR;
import static se.inera.webcert.service.signatur.dto.SignaturTicket.Status.SIGNERAD;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import se.inera.webcert.service.signatur.SignaturTicketTracker;
import se.inera.webcert.service.signatur.dto.SignaturTicket;

public class SignaturTicketTrackerTest {

    private SignaturTicketTracker tracker = new SignaturTicketTracker();

    @Test
    public void prune() {
        for (int i = 1; i < 100; i++) {
            tracker.trackTicket(new SignaturTicket("old-" + i, BEARBETAR, "intygid", 1, LocalDateTime.now(), "1234", new LocalDateTime().minusMinutes(6)));
            tracker.trackTicket(new SignaturTicket(String.valueOf(i), BEARBETAR, "intygid", 1, LocalDateTime.now(), "1234", new LocalDateTime()));
        }
        SignaturTicket tracked = tracker.getTicket("1");
        assertEquals(BEARBETAR, tracked.getStatus());
        assertNull(tracker.getTicket("old-1"));
    }

    @Test
    public void updateStatus() {
        tracker.trackTicket(new SignaturTicket("id", BEARBETAR, "intygid", 1, LocalDateTime.now(), "1234", new LocalDateTime()));
        tracker.updateStatus("id", SIGNERAD);
        SignaturTicket tracked = tracker.getTicket("id");
        assertEquals(SIGNERAD, tracked.getStatus());
    }
}
