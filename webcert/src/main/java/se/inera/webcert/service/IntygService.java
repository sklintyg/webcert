package se.inera.webcert.service;

import se.inera.certificate.integration.rest.dto.CertificateContentHolder;
import se.inera.certificate.model.Utlatande;
import se.inera.webcert.service.dto.UtlatandeCommonModelHolder;

/**
 * @author andreaskaltenbach
 */
public interface IntygService {

    /**
     * Fetches the intyg data from the intygstjanst and returns the intyg content in internal model representation.
     * 
     * @throws se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException
     *             if there occurs a problem fetching intyg data from the intygstjanst
     * @throws se.inera.certificate.integration.rest.exception.ModuleCallFailedException
     *             if a call to the module API fails
     */
    CertificateContentHolder fetchIntygData(String intygId);

    /**
     * Fetches the intyg data from the intygstjanst and returns the intyg content in external model representation.
     * 
     * @throws se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException
     *             if there occurs a problem fetching intyg data from the intygstjanst
     * @throws se.inera.certificate.integration.rest.exception.ModuleCallFailedException
     *             if a call to the module API fails
     */
    CertificateContentHolder fetchExternalIntygData(String intygId);

    /**
     * Fetches the {@link Utlatande} with id IntygsId from the Intygstjanst
     * 
     * @throws se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException
     */
    UtlatandeCommonModelHolder fetchIntygCommonModel(String intygId);
}
