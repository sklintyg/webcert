package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataDTO.DR_KRANSTEGE_REGISTREATION_REQUEST_DTO;

import jakarta.ws.rs.core.Response.Status;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.privatepractitioner.PrivatePractitionerService;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.CodeDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HospInformationResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConfigResponse;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerApiControllerTest {

    @Mock
    PrivatePractitionerService service;
    @InjectMocks
    PrivatePractitionerApiController controller;

    @Test
    void shouldRegisterPractitioner() {
        var result = controller.registerPractitioner(DR_KRANSTEGE_REGISTREATION_REQUEST_DTO);

        verify(service).registerPrivatePractitioner(DR_KRANSTEGE_REGISTREATION_REQUEST_DTO);
        assertEquals(Status.OK, result.getStatusInfo().toEnum());
    }

    @Test
    void shouldGetPrivatePractitionerConfig() {
        final var config = PrivatePractitionerConfigResponse.builder()
            .positions(List.of(new CodeDTO("Överläkare", "Frida Kranstege")))
            .healthcareServiceTypes(List.of(new CodeDTO("11", "Medicinsk verksamhet")))
            .typeOfCare(List.of(new CodeDTO("01", "Type of Care")))
            .build();
        when(service.getPrivatePractitionerConfig()).thenReturn(config);

        final var response = controller.getPrivatePractitionerConfig();
        assertEquals(config, response);
    }

    @Test
    void shouldGetHospInformation() {
        final var hospInformation = HospInformationResponse.builder()
            .licensedHealthcareProfessions(List.of(
                new CodeDTO("1", "Läkare"),
                new CodeDTO("ATLK", "AT-läkare")))
            .specialities(List.of(
                new CodeDTO("32", "Klinisk fysiologi"),
                new CodeDTO("74", "Nukleärmedicin")))
            .personalPrescriptionCode("12345")
            .build();
        when(service.getHospInformation()).thenReturn(hospInformation);

        final var response = controller.getHospInformation();

        assertEquals(hospInformation, response);

    }
}

