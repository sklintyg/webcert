package se.inera.intyg.webcert.web.service.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.common.enumerations.Diagnoskodverk;
import se.inera.certificate.modules.service.WebcertModuleService;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponseType;

/**
 * Exposes services to be used by modules.
 *
 * @author npet
 */
@Component
public class WebcertModuleServiceImpl implements WebcertModuleService {

    private static final Logger LOG = LoggerFactory.getLogger(WebcertModuleService.class);

    @Autowired
    private DiagnosService diagnosService;

    /*
     * (non-Javadoc)
     *
     * @see se.inera.certificate.modules.service.WebcertModuleService#validateDiagnosisCode(java.lang.String, int)
     */
    @Override
    public boolean validateDiagnosisCode(String codeFragment, String codeSystemStr) {

        DiagnosResponse response = diagnosService.getDiagnosisByCode(codeFragment, codeSystemStr);

        LOG.debug("Validation result for diagnosis code '{}' is {}", codeFragment, response);

        return (DiagnosResponseType.OK.equals(response.getResultat()));
    }

    @Override
    public boolean validateDiagnosisCode(String codeFragment, Diagnoskodverk codeSystem) {

        DiagnosResponse response = diagnosService.getDiagnosisByCode(codeFragment, codeSystem);

        LOG.debug("Validation result for diagnosis code '{}' is {}", codeFragment, response);

        return (DiagnosResponseType.OK.equals(response.getResultat()));
    }
}
