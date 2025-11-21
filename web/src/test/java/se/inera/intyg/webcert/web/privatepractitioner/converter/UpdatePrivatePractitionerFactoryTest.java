package se.inera.intyg.webcert.web.privatepractitioner.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_UPDATE_REQUEST_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_UPDATE_REQUEST_INTEGRATION_DTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdatePrivatePractitionerFactoryTest {

    private final UpdatePrivatePractitionerFactory factory = new UpdatePrivatePractitionerFactory();

    @Test
    void shouldCreateUpdateRequest() {

        final var result = factory.create(DR_KRANSTEGE_UPDATE_REQUEST_DTO);

        assertEquals(DR_KRANSTEGE_UPDATE_REQUEST_INTEGRATION_DTO, result);
    }
}