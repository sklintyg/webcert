package se.inera.intyg.webcert.web.ppsintegration.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.integration.privatepractitioner.model.HospInformationDTO;

@Builder
@Value
public class HospInformation {
  String personalPrescriptionCode;
  List<String> specialityNames;
  List<String> hsaTitles;

  public static HospInformation convert(HospInformationDTO hospInformationDTO) {
      return HospInformation.builder()
          .personalPrescriptionCode(hospInformationDTO.getPersonalPrescriptionCode())
          .specialityNames(hospInformationDTO.getSpecialityNames())
          .hsaTitles(hospInformationDTO.getHsaTitles())
          .build();
  }
}
