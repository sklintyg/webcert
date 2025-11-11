package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.privatepractitioner.PrivatePractitionerService;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HealthCareServiceTypeDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HospInformationResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PositionDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConsentDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerRegistrationRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.TypeOfCareDTO;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerApiControllerTest {

    public static final String PERSON_ID = "191212121212";
    @Mock
    PrivatePractitionerService service;
    @InjectMocks
    PrivatePractitionerApiController controller;

    @Test
    void shouldRegisterPractitioner() {
        final var request =
            PrivatePractitionerRegistrationRequest.builder()
                .position("Doctor")
                .careUnitName("Care Unit")
                .typeOfCare("Type of Care")
                .healthcareServiceType("Healthcare Service Type")
                .workplaceCode("Workplace Code")
                .phoneNumber("123456789")
                .email("email")
                .address("Street 1")
                .zipCode("12345")
                .city("City")
                .municipality("Municipality")
                .county("County")
                .consentFormVersion(1L)
                .build();

        final var privatePractitioner = PrivatePractitionerDTO.builder()
            .personId(PERSON_ID)
            .name("Name")
            .position("Doctor")
            .careUnitName("Care Unit")
            .ownershipType("Type of Ownership")
            .typeOfCare("Type of Care")
            .healthcareServiceType("Healthcare Service Type")
            .workplaceCode("Workplace Code")
            .phoneNumber("123456789")
            .email("email")
            .address("Street 1")
            .zipCode("12345")
            .city("City")
            .municipality("Municipality")
            .county("County")
            .build();

        when(service.registerPrivatePractitioner(request)).thenReturn(privatePractitioner);

        final var response = controller.registerPractitioner(request);

        assertAll(
            () -> assertEquals(PERSON_ID, response.privatePractitioner().getPersonId()),
            () -> assertEquals("Name", response.privatePractitioner().getName()),
            () -> assertEquals("Doctor", response.privatePractitioner().getPosition()),
            () -> assertEquals("Care Unit", response.privatePractitioner().getCareUnitName()),
            () -> assertEquals("Type of Care", response.privatePractitioner().getTypeOfCare()),
            () -> assertEquals("Type of Ownership",
                response.privatePractitioner().getOwnershipType()),
            () -> assertEquals("Healthcare Service Type",
                response.privatePractitioner().getHealthcareServiceType()),
            () -> assertEquals("Workplace Code",
                response.privatePractitioner().getWorkplaceCode()),
            () -> assertEquals("123456789", response.privatePractitioner().getPhoneNumber()),
            () -> assertEquals("email", response.privatePractitioner().getEmail()),
            () -> assertEquals("Street 1", response.privatePractitioner().getAddress()),
            () -> assertEquals("12345", response.privatePractitioner().getZipCode()),
            () -> assertEquals("City", response.privatePractitioner().getCity()),
            () -> assertEquals("Municipality",
                response.privatePractitioner().getMunicipality()),
            () -> assertEquals("County", response.privatePractitioner().getCounty())
        );
    }

    @Test
    void shouldGetPrivatePractitioner() {

        final var privatePractitioner = PrivatePractitionerDTO.builder()
            .personId(PERSON_ID)
            .name("Name")
            .position("Doctor")
            .careUnitName("Care Unit")
            .ownershipType("Type of Ownership")
            .typeOfCare("Type of Care")
            .healthcareServiceType("Healthcare Service Type")
            .workplaceCode("Workplace Code")
            .phoneNumber("123456789")
            .email("email")
            .address("Street 1")
            .zipCode("12345")
            .city("City")
            .municipality("Municipality")
            .county("County")
            .consentFormVersion(1L)
            .build();
        when(service.getPrivatePractitioner()).thenReturn(privatePractitioner);

        final var response = controller.getPrivatePractitioner();

        assertAll(
            () -> assertEquals(PERSON_ID, response.privatePractitioner().getPersonId()),
            () -> assertEquals("Name", response.privatePractitioner().getName()),
            () -> assertEquals("Doctor", response.privatePractitioner().getPosition()),
            () -> assertEquals("Care Unit", response.privatePractitioner().getCareUnitName()),
            () -> assertEquals("Type of Care", response.privatePractitioner().getTypeOfCare()),
            () -> assertEquals("Type of Ownership",
                response.privatePractitioner().getOwnershipType()),
            () -> assertEquals("Healthcare Service Type",
                response.privatePractitioner().getHealthcareServiceType()),
            () -> assertEquals("Workplace Code",
                response.privatePractitioner().getWorkplaceCode()),
            () -> assertEquals("123456789", response.privatePractitioner().getPhoneNumber()),
            () -> assertEquals("email", response.privatePractitioner().getEmail()),
            () -> assertEquals("Street 1", response.privatePractitioner().getAddress()),
            () -> assertEquals("12345", response.privatePractitioner().getZipCode()),
            () -> assertEquals("City", response.privatePractitioner().getCity()),
            () -> assertEquals("Municipality",
                response.privatePractitioner().getMunicipality()),
            () -> assertEquals("County", response.privatePractitioner().getCounty()),
            () -> assertEquals(1L, response.privatePractitioner().getConsentFormVersion())
        );
    }

    @Test
    void shouldUpdatePrivatePractitioner() {
        final var privatePractitionerDTO = PrivatePractitionerDTO.builder()
            .personId(PERSON_ID)
            .name("Name")
            .position("Doctor")
            .careUnitName("Care Unit")
            .ownershipType("Type of Ownership")
            .typeOfCare("Type of Care")
            .healthcareServiceType("Healthcare Service Type")
            .workplaceCode("Workplace Code")
            .phoneNumber("123456789")
            .email("email")
            .address("Street 1")
            .zipCode("12345")
            .city("City")
            .municipality("Municipality")
            .county("County")
            .build();

        final var response = controller.updatePrivatePractitioner(privatePractitionerDTO);

        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldGetPrivatePractitionerConfig() {
        final var config = PrivatePractitionerConfigResponse.builder()
            .consent(new PrivatePractitionerConsentDTO("Consent Text", 1L))
            .positions(List.of(new PositionDTO("Daoc", "Doctor")))
            .healthcareServiceType(List.of(new HealthCareServiceTypeDTO("Spec", "Specialty")))
            .typeOfCare(List.of(new TypeOfCareDTO("Care", "Type of Care")))
            .build();
        when(service.getPrivatePractitionerConfig()).thenReturn(config);

        final var response = controller.getPrivatePractitionerConfig();
        assertEquals(config, response);
    }

    @Test
    void shouldGetHospInformation() {
        final var hospInformation = HospInformationResponse.builder()
            .hsaTitles(List.of("Title"))
            .personalPrescriptionCode("Code")
            .specialityNames(List.of("Speciality"))
            .build();
        when(service.getHospInformation()).thenReturn(hospInformation);

        final var response = controller.getHospInformation();

        assertEquals(hospInformation, response);

    }
}

