package se.inera.webcert.notifications.service;

import java.util.Arrays;
import java.util.List;

import org.apache.camel.Header;

import se.inera.certificate.modules.service.WebcertModuleService;
import se.inera.webcert.notifications.TestDataUtil;
import se.inera.webcert.notifications.TestUtkastProducer;
import se.inera.webcert.notifications.routes.RouteHeaders;
import se.inera.webcert.persistence.utkast.model.Utkast;

public class MockWebcertModuleServiceImpl implements WebcertModuleService {

    @Override
    public boolean validateDiagnosisCode(String codeFragment, String codeSystem) {
        return "S47".equals(codeFragment);
    }
}
