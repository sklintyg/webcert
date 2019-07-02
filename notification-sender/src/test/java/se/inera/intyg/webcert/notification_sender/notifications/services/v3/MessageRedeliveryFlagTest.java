package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class MessageRedeliveryFlagTest {

    @Test
    public void lowerAndoutdatedTest() {
        long t0 = System.currentTimeMillis() - 1L;
        MessageRedeliveryFlag.StatusFlag sf = new MessageRedeliveryFlag.StatusFlag();

        sf.lower();

        assertEquals(true, sf.isSuccess());
        assertEquals(true, sf.getSuccessTimestamp() > t0);
        assertEquals(true, sf.isOutdated(t0));
    }

    @Test
    public void raisedTest() {
        MessageRedeliveryFlag.StatusFlag sf = new MessageRedeliveryFlag.StatusFlag();

        sf.raise();

        assertEquals(false, sf.isSuccess());
        assertEquals(0L, sf.getSuccessTimestamp());
    }
}
