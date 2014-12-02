package se.inera.webcert.notifications.service;

import se.inera.webcert.persistence.intyg.model.Intyg;

public interface WebcertRepositoryService {

    Intyg getIntygsUtkast(String intygsId);
    
    String getIntygsUtkastModel(String intygsId);
    
    boolean isIntygsUtkastPresent(String intygsId);
    
    boolean isVardenhetIntegrerad(String vardenhetHsaId);

}
