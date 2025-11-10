package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.ppsintegration.PrivatePractitionerService;
import se.inera.intyg.webcert.web.ppsintegration.dto.PrivatePractitioner;
import se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerRegistrationDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.RegisterPrivatePractitionerRequest;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerApiControllerTest {

  public static final String PERSON_ID = "123456789";
  @Mock
  PrivatePractitionerService service;
  @InjectMocks
  PrivatePractitionerApiController controller;

  @Test
  void shouldRegisterPractitioner() {
    final var request = RegisterPrivatePractitionerRequest.builder()
        .privatePractitionerRegistration(
            PrivatePractitionerRegistrationDTO.builder()
                .position("Doctor")
                .careUnitName("Care Unit")
                .typeOfCare("Type of Care")
                .healthcareServiceType("Healthcare Service Type")
                .workplaceCode("Workplace Code")
                .phoneNumber("123456789")
                .email("email")
                .postalAddress("Street 1")
                .zipCode("12345")
                .city("City")
                .municipality("Municipality")
                .county("County")
                .consentFormVersion(1L)
                .build()
        )
        .build();

    final var privatePractitioner = PrivatePractitioner.builder()
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
        .postalAddress("Street 1")
        .zipCode("12345")
        .city("City")
        .municipalityCode("Municipality")
        .countyCode("County")
        .build();

    when(service.registerPrivatePractitioner(request)).thenReturn(privatePractitioner);

    final var response = controller.registerPractitioner(request);

    assertAll(
        () -> assertEquals(PERSON_ID, response.getPrivatePractitioner().getPersonId()),
        () -> assertEquals("Name", response.getPrivatePractitioner().getName()),
        () -> assertEquals("Doctor", response.getPrivatePractitioner().getPosition()),
        () -> assertEquals("Care Unit", response.getPrivatePractitioner().getCareUnitName()),
        () -> assertEquals("Type of Care", response.getPrivatePractitioner().getTypeOfCare()),
        () -> assertEquals("Type of Ownership",
            response.getPrivatePractitioner().getOwnershipType()),
        () -> assertEquals("Healthcare Service Type",
            response.getPrivatePractitioner().getHealthcareServiceType()),
        () -> assertEquals("Workplace Code",
            response.getPrivatePractitioner().getWorkplaceCode()),
        () -> assertEquals("123456789", response.getPrivatePractitioner().getPhoneNumber()),
        () -> assertEquals("email", response.getPrivatePractitioner().getEmail()),
        () -> assertEquals("Street 1", response.getPrivatePractitioner().getPostalAddress()),
        () -> assertEquals("12345", response.getPrivatePractitioner().getZipCode()),
        () -> assertEquals("City", response.getPrivatePractitioner().getCity()),
        () -> assertEquals("Municipality",
            response.getPrivatePractitioner().getMunicipality()),
        () -> assertEquals("County", response.getPrivatePractitioner().getCounty())
    );
  }
}

