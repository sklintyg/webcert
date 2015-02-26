package se.inera.webcert.notifications.service;

import se.inera.certificate.common.enumerations.Diagnoskodverk;
import se.inera.certificate.modules.service.WebcertModuleService;

public class MockWebcertModuleServiceImpl implements WebcertModuleService {

    @Override
    public boolean validateDiagnosisCode(String codeFragment, String codeSystem) {
        return "S47".equals(codeFragment);
    }

    @Override
    public boolean validateDiagnosisCode(String codeFragment, Diagnoskodverk codeSystem) {
        return "S47".equals(codeFragment);
    }
}
