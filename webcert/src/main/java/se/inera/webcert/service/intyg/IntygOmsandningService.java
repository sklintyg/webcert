package se.inera.webcert.service.intyg;

import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;

public interface IntygOmsandningService {

    /**
     * Trigger a new attempt to store a certificate in the Intygtjanst.
     * 
     * @param omsandning
     * @return
     */
    public abstract IntygServiceResult storeIntyg(Omsandning omsandning);
        
    /**
     * Triggers a new attempt to send a certificate to a recipient.
     * 
     * @param omsandning
     * @return
     */
    public abstract IntygServiceResult sendIntyg(Omsandning omsandning);
    
    /**
     * Triggers a new attempt to issue a revoke for a given certificate.
     * 
     * @param omsandning
     * @return
     */
    public abstract IntygServiceResult revokeIntyg(Omsandning omsandning);
    
}
