package se.inera.intyg.webcert.web.privatepractitioner.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_UPDATE_REQUEST_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_UPDATE_REQUEST_INTEGRATION_DTO;

import org.junit.jupiter.api.Test;

class PrivatePractitionerUpdateRequestConverterTest {

    private final PrivatePractitionerUpdateRequestConverter converter =
        new PrivatePractitionerUpdateRequestConverter();

    @Test
    void shouldConvertUpdateRequest() {
        final var result = converter.convert(DR_KRANSTEGE_UPDATE_REQUEST_DTO);

        assertEquals(DR_KRANSTEGE_UPDATE_REQUEST_INTEGRATION_DTO, result);
    }

    @Test
    void shouldConvertUpdateRequestWithNulls() {
        final var result = converter.convert(null);

        assertEquals(null, result);
    }
}