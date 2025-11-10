package se.inera.intyg.webcert.web.web.controller.api.dto;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class PrivatePractitionerRegistrationDTO {

  String position;
  String careUnitName;

  String typeOfCare;
  String healthcareServiceType;
  String workplaceCode;

  String phoneNumber;
  String email;
  String postalAddress;
  String zipCode;
  String city;
  String municipality;
  String county;

  Long consentFormVersion;

}
