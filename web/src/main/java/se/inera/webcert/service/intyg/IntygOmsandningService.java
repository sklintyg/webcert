package se.inera.webcert.service.intyg;

import se.inera.webcert.persistence.utkast.model.Omsandning;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;

/**
 * @deprecated
 *      Remove ASAP
 */
public interface IntygOmsandningService {

    /**
     * Trigger a new attempt to store a certificate in the Intygtjanst.
     *
     * @param omsandning
     * @return
     */
    IntygServiceResult storeIntyg(Omsandning omsandning);

    /**
     * Triggers a new attempt to send a certificate to a recipient.
     *
     * @param omsandning
     * @return
     */
    IntygServiceResult sendIntyg(Omsandning omsandning);
}
