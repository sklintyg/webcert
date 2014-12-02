package se.inera.webcert.notifications.service;

import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.notifications.routes.RouteHeaders;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.webcert.persistence.integreradenhet.repository.IntegreradEnhetRepository;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;

/**
 * Simple facade for the Intyg respository so header values from Camel
 * routes can be used as parameters.
 * 
 * @author npet
 *
 */
public class WebcertRepositoryServiceImpl implements WebcertRepositoryService {

    private static final Logger LOG = LoggerFactory.getLogger(WebcertRepositoryService.class);
    
    @Autowired
    private IntygRepository intygRepository;
    
    @Autowired
    private FragaSvarRepository fragaSvarRepository;
    
    @Autowired
    private IntegreradEnhetRepository integreradEnhetRepository;
    
    public Long countNbrOfQuestionsForIntyg(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        return fragaSvarRepository.countByIntyg(intygsId);
    }
    
    public Long countNbrOfAnsweredQuestionsForIntyg(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        return fragaSvarRepository.countAnsweredByIntyg(intygsId);
    }
    
    public Long countNbrOfHandledQuestionsForIntyg(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        return fragaSvarRepository.countHandledByIntyg(intygsId);
    }
    
    public Long countNbrOfHandledAndAnsweredQuestionsForIntyg(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        return fragaSvarRepository.countHandledAndAnsweredByIntyg(intygsId);
    }
    
    /* (non-Javadoc)
     * @see se.inera.webcert.notifications.service.IntygRepositoryService#getIntygsUtkast(java.lang.String)
     */
    @Override
    public Intyg getIntygsUtkast(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        
        LOG.debug("Retrieveing Intygsutkast using param '{}'", intygsId);
        
        return intygRepository.findOne(intygsId);
    }
    
    @Override
    public String getIntygsUtkastModel(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        
        LOG.debug("Retrieveing Intygsutkast model using param '{}'", intygsId);
        
        Intyg intygsUtkast = intygRepository.findOne(intygsId);
        
        if (intygsUtkast != null) {
            return intygsUtkast.getModel();
        }
                
        return null;
    }

    public boolean isIntygsUtkastPresent(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        
        if (intygRepository.exists(intygsId)) {
            return true;
        }
        
        LOG.debug("Intyg '{}' is not present in IntygRepository", intygsId);
        return false;
    }
    
    public boolean isVardenhetIntegrerad(@Header(RouteHeaders.VARDENHET_HSA_ID) String vardenhetHsaId) {
        
        if (integreradEnhetRepository.exists(vardenhetHsaId)) {
            return true;
        };
        
        LOG.debug("Vardenhet '{}' is not present in IntegreradEnhetRepository", vardenhetHsaId);
        return false;
    }
}
