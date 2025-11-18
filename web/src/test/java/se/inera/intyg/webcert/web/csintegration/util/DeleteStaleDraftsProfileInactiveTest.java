package se.inera.intyg.webcert.web.csintegration.util;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class DeleteStaleDraftsProfileInactiveTest {

    @Test
    void shouldReturnFalse() {
        final var draftsProfileInactive = new DeleteStaleDraftsProfileInactive();
        assertFalse(draftsProfileInactive.active());
    }
}