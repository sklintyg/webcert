package se.inera.webcert.service.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.modules.service.WebcertModuleService;
import se.inera.webcert.service.diagnos.DiagnosService;
import se.inera.webcert.service.diagnos.dto.DiagnosResponse;
import se.inera.webcert.service.diagnos.dto.DiagnosResponseType;

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
    public boolean validateDiagnosisCode(String codeFragment) {

        DiagnosResponse response = diagnosService.searchDiagnosisByCode(codeFragment);

        LOG.debug("Validation result for diagnosis code '{}' is {}", codeFragment, response.getResultat());

        return (DiagnosResponseType.OK.equals(response.getResultat()));
    }
}
