package se.inera.intyg.webcert.web.csintegration.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DeleteStaleDraftsProfileActiveTest {

    @Test
    void shouldReturnTrue() {
        final var draftsProfileActive = new DeleteStaleDraftsProfileActive();
        assertTrue(draftsProfileActive.active());
    }
}