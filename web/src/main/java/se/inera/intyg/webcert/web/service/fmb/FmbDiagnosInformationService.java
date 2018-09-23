package se.inera.intyg.webcert.web.service.fmb;

import java.util.Optional;
import se.inera.intyg.webcert.web.web.controller.api.dto.FmbResponse;

public interface FmbDiagnosInformationService {

    Optional<FmbResponse> findFmbDiagnosInformationByIcd10Kod(String icd10Kod);
}
