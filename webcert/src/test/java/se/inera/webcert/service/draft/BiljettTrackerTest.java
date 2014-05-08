package se.inera.webcert.service.draft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static se.inera.webcert.service.draft.dto.SigneringsBiljett.Status.BEARBETAR;
import static se.inera.webcert.service.draft.dto.SigneringsBiljett.Status.SIGNERAD;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import se.inera.webcert.service.draft.dto.SigneringsBiljett;

public class BiljettTrackerTest {

    private BiljettTracker tracker = new BiljettTracker();

    @Test
    public void prune() {
        for (int i = 1; i < 100; i++) {
            tracker.trackBiljett(new SigneringsBiljett("old-" + i, BEARBETAR, "intygid", "1234", new LocalDateTime().minusMinutes(6)));
            tracker.trackBiljett(new SigneringsBiljett(String.valueOf(i), BEARBETAR, "intygid", "1234", new LocalDateTime()));
        }
        SigneringsBiljett tracked = tracker.getBiljett("1");
        assertEquals(BEARBETAR, tracked.getStatus());
        assertNull(tracker.getBiljett("old-1"));
    }

    @Test
    public void updateStatus() {
        tracker.trackBiljett(new SigneringsBiljett("id", BEARBETAR, "intygid", "1234", new LocalDateTime()));
        tracker.updateStatusBiljett("id", SIGNERAD);
        SigneringsBiljett tracked = tracker.getBiljett("id");
        assertEquals(SIGNERAD, tracked.getStatus());
    }
}
