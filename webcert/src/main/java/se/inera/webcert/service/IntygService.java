package se.inera.webcert.service;

/**
 * @author andreaskaltenbach
 */
public interface IntygService {

    /**
     * Fetches the intyg data from the intygstjanst and returns the intyg content in internal model representation.
     *
     *
     * @throws se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException if there occurs a
     * problem fetching intyg data from the intygstjanst
     * @throws
     */
    String fetchIntygData(String intygId);
}
