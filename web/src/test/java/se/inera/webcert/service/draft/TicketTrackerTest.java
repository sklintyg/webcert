package se.inera.webcert.service.draft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static se.inera.webcert.service.draft.dto.SignatureTicket.Status.BEARBETAR;
import static se.inera.webcert.service.draft.dto.SignatureTicket.Status.SIGNERAD;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import se.inera.webcert.service.draft.dto.SignatureTicket;

public class TicketTrackerTest {

    private TicketTracker tracker = new TicketTracker();

    @Test
    public void prune() {
        for (int i = 1; i < 100; i++) {
            tracker.trackTicket(new SignatureTicket("old-" + i, BEARBETAR, "intygid", "1234", new LocalDateTime().minusMinutes(6)));
            tracker.trackTicket(new SignatureTicket(String.valueOf(i), BEARBETAR, "intygid", "1234", new LocalDateTime()));
        }
        SignatureTicket tracked = tracker.getTicket("1");
        assertEquals(BEARBETAR, tracked.getStatus());
        assertNull(tracker.getTicket("old-1"));
    }

    @Test
    public void updateStatus() {
        tracker.trackTicket(new SignatureTicket("id", BEARBETAR, "intygid", "1234", new LocalDateTime()));
        tracker.updateStatus("id", SIGNERAD);
        SignatureTicket tracked = tracker.getTicket("id");
        assertEquals(SIGNERAD, tracked.getStatus());
    }
}
