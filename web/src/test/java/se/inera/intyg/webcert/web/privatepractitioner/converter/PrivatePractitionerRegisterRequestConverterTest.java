package se.inera.intyg.webcert.web.privatepractitioner.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_HSA_ID;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_NAME;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_PERSON_ID;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_REGISTRATION_REQUEST_DTO;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_REGISTRATION_REQUEST_INTEGRATION_DTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerRegisterRequestConverterTest {

    @Mock
    private WebCertUserService webCertUserService;

    private final PrivatePractitionerRegisterRequestConverter converter = new PrivatePractitionerRegisterRequestConverter();

    private void mockUser() {
        final var user = new WebCertUser();
        user.setPersonId(DR_KRANSTEGE_PERSON_ID);
        user.setNamn(DR_KRANSTEGE_NAME);
        user.setHsaId(DR_KRANSTEGE_HSA_ID);
        when(webCertUserService.getUser()).thenReturn(user);
    }

    @Test
    void shouldConvertRegisterRequest() {
        mockUser();

        final var result = converter.convert(DR_KRANSTEGE_REGISTRATION_REQUEST_DTO, webCertUserService);

        assertEquals(DR_KRANSTEGE_REGISTRATION_REQUEST_INTEGRATION_DTO, result);
    }

    @Test
    void shouldThrowExceptionWhenWebCertUserIsNull() {
        when(webCertUserService.getUser()).thenReturn(null);

        assertThrows(IllegalStateException.class, () ->
            converter.convert(DR_KRANSTEGE_REGISTRATION_REQUEST_DTO, webCertUserService)
        );
    }
}