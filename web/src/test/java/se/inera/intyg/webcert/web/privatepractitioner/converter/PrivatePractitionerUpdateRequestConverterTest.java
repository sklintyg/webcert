package se.inera.intyg.webcert.web.privatepractitioner.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_HSA_ID;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_NAME;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_PERSON_ID;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_UPDATE_REQUEST_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_UPDATE_REQUEST_INTEGRATION_DTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerUpdateRequestConverterTest {

    @Mock
    private WebCertUserService webCertUserService;

    private PrivatePractitionerUpdateRequestConverter converter;

    @BeforeEach
    void setUp() {
        converter = new PrivatePractitionerUpdateRequestConverter(webCertUserService);
    }

    private void mockUser() {
        final var user = new WebCertUser();
        user.setPersonId(DR_KRANSTEGE_PERSON_ID);
        user.setNamn(DR_KRANSTEGE_NAME);
        user.setHsaId(DR_KRANSTEGE_HSA_ID);
        when(webCertUserService.getUser()).thenReturn(user);
    }

    @Test
    void shouldConvertUpdateRequest() {
        mockUser();
        final var result = converter.convert(DR_KRANSTEGE_UPDATE_REQUEST_DTO);

        assertEquals(DR_KRANSTEGE_UPDATE_REQUEST_INTEGRATION_DTO, result);
    }

    @Test
    void shouldThrowExceptionWhenInputIsNull() {
        mockUser();

        assertThrows(IllegalArgumentException.class, () -> converter.convert(null));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {
        when(webCertUserService.getUser()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> converter.convert(DR_KRANSTEGE_UPDATE_REQUEST_DTO));
    }
}