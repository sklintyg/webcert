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
import se.inera.intyg.webcert.integration.privatepractitioner.model.GetPrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitionerConsent;
import se.inera.intyg.webcert.web.ppsintegration.dto.HospInformation;
import se.inera.intyg.webcert.web.ppsintegration.PrivatePractitionerService;
import se.inera.intyg.webcert.web.ppsintegration.dto.PrivatePractitioner;
import se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerRegisterRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerRegistrationDTO;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class PrivatePractitionerApiControllerTest {

  public static final String PERSON_ID = "123456789";
  @Mock
  PrivatePractitionerService service;
  @InjectMocks
  PrivatePractitionerApiController controller;

  @Test
  void shouldRegisterPractitioner() {
    final var request = PrivatePractitionerRegisterRequest.builder()
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

  @Test
  void shouldGetPrivatePractitioner() {
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

    when(service.getPrivatePractitioner()).thenReturn(privatePractitioner);

    final var response = controller.getPrivatePractitioner();

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

  @Test
  void shouldUpdatePrivatePractitioner() {
    final var privatePractitionerDTO = se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerDTO.builder()
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
        .municipality("Municipality")
        .county("County")
        .build();

    final var response = controller.updatePrivatePractitioner(privatePractitionerDTO);

    assertEquals(200, response.getStatus());
  }

  @Test
  void shouldGetPrivatePractitionerConfig() {
    final var config = GetPrivatePractitionerConfigResponse.builder()
        .consent(new PrivatePractitionerConsent("Consent Text", 1L))
        .positions(Map.of("Doc", "Doctor"))
        .specialties(Map.of("Spec", "Specialty"))
        .typeOfCare(Map.of("Care", "Type of Care"))
        .build();
    when(service.getPrivatePractitionerConfig()).thenReturn(config);

    final var response = controller.getPrivatePractitionerConfig();

    assertAll(
        () -> assertEquals(config, response.getGetPrivatePractitionerConfig()),
        () -> assertEquals("Consent Text",
            response.getGetPrivatePractitionerConfig().getConsent().content()),
        () -> assertEquals(1L,
            response.getGetPrivatePractitionerConfig().getConsent().version()),
        () -> assertEquals(Map.of("Doc", "Doctor"),
            response.getGetPrivatePractitionerConfig().getPositions()),
        () -> assertEquals(Map.of("Spec", "Specialty"),
            response.getGetPrivatePractitionerConfig().getSpecialties()),
        () -> assertEquals(Map.of("Care", "Type of Care"),
            response.getGetPrivatePractitionerConfig().getTypeOfCare())
    );
  }

  @Test
  void shouldGetHospInformation() {
    final var hospInformation = HospInformation.builder()
        .hsaTitles(List.of("Title"))
        .personalPrescriptionCode("Code")
        .specialityNames(List.of("Speciality"))
        .build();
    when(service.getHospInformation()).thenReturn(hospInformation);

    final var response = controller.getHospInformation();

    assertAll(
        () -> assertEquals(hospInformation, response.getHospInformation()),
        () -> assertEquals(List.of("Title"), response.getHospInformation().getHsaTitles()),
        () -> assertEquals("Code", response.getHospInformation().getPersonalPrescriptionCode()),
        () -> assertEquals(List.of("Speciality"),
            response.getHospInformation().getSpecialityNames())
    );

  }
}

