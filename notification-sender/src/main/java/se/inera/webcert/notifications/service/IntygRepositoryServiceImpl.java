package se.inera.webcert.notifications.service;

import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.notifications.routes.ProcessNotificationRequestRouteHeaders;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;

/**
 * Simple facade for the Intyg respository so header values from Camel
 * routes can be used as parameters.
 * 
 * @author npet
 *
 */
public class IntygRepositoryServiceImpl implements IntygRepositoryService {

    private static final Logger LOG = LoggerFactory.getLogger(IntygRepositoryServiceImpl.class);
    
    //Autowired
    private IntygRepository intygRepository;
    
    /* (non-Javadoc)
     * @see se.inera.webcert.notifications.service.IntygRepositoryService#getIntygsUtkast(java.lang.String)
     */
    @Override
    public Intyg getIntygsUtkast(@Header(ProcessNotificationRequestRouteHeaders.INTYGS_ID) String intygsId) {
        
        LOG.debug("Retrieveing Intygsutkast using param '{}'", intygsId);
        
        //Intyg intygsUtkast = intygRepository.findOne(intygsId);
        
        return new Intyg();
    }

    public IntygRepository getIntygRepository() {
        return intygRepository;
    }

    public void setIntygRepository(IntygRepository intygRepository) {
        this.intygRepository = intygRepository;
    }
}
