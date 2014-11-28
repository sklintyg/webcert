package se.inera.webcert.notifications.service;

import java.util.Arrays;
import java.util.List;

import org.apache.camel.Header;

import se.inera.webcert.notifications.TestIntygProducer;
import se.inera.webcert.notifications.routes.RouteHeaders;
import se.inera.webcert.persistence.intyg.model.Intyg;

public class MockWebcertRepositoryServiceImpl implements WebcertRepositoryService {
    
    private List<String> VALID_INTYGS_ID = Arrays.asList("intyg-1", "intyg-3");
    
    private List<String> INTEGRATED_UNITS = Arrays.asList("vardenhet-1","vardenhet-3");
    
    private TestIntygProducer intygProducer = new TestIntygProducer();
    
    @Override
    public Intyg getIntygsUtkast(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        String pathToIntygJson = "intyg/" + intygsId + ".json";
        return intygProducer.buildIntyg(pathToIntygJson);
    }

    @Override
    public boolean isIntygsUtkastPresent(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        return (VALID_INTYGS_ID.contains(intygsId));
    }

    @Override
    public boolean isVardenhetIntegrerad(@Header(RouteHeaders.VARDENHET_HSA_ID) String vardenhetHsaId) {
        return INTEGRATED_UNITS.contains(vardenhetHsaId);
    }   
}
