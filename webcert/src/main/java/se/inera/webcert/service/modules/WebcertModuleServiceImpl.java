package se.inera.webcert.service.modules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.modules.service.WebcertModuleService;
import se.inera.webcert.service.diagnos.DiagnosService;

/**
 * Exposes services to be used by modules.
 * 
 * @author npet
 *
 */
@Component
public class WebcertModuleServiceImpl implements WebcertModuleService {

    @Autowired
    private DiagnosService diagnosService;

    /* (non-Javadoc)
     * @see se.inera.certificate.modules.service.WebcertModuleService#validateDiagnosisCode(java.lang.String, int)
     */
    public boolean validateDiagnosisCode(String codeFragment, int minimumLength) {
        return diagnosService.validateDiagnosisCode(codeFragment, minimumLength);
    }
}
