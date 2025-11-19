package se.inera.intyg.webcert.web.csintegration.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DeleteStaleDraftsProfileActiveTest {

    @Test
    void shouldReturnFalseIfActivationDateIsAfterToday() {
        final var draftsProfileActive = new DeleteStaleDraftsProfileActive();
        ReflectionTestUtils.setField(draftsProfileActive, "activationDate", LocalDate.now().plusDays(1));
        assertFalse(draftsProfileActive.active());
    }

    @Test
    void shouldReturnTrueIfActivationDateEqualsToday() {
        final var draftsProfileActive = new DeleteStaleDraftsProfileActive();
        ReflectionTestUtils.setField(draftsProfileActive, "activationDate", LocalDate.now());
        assertTrue(draftsProfileActive.active());
    }

}