package se.inera.webcert.service.intyg;

import java.util.List;

import se.inera.certificate.modules.support.api.dto.PdfResponse;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygPdf;

/**
 * @author andreaskaltenbach
 */
public interface IntygService {

    /**
     * Fetches the intyg data from the intygstjanst and returns the intyg content in internal model representation.
     *
     * @throws se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException if there occurs a problem fetching intyg data from the intygstjanst
     * @throws se.inera.certificate.integration.rest.exception.ModuleCallFailedException        if a call to the module API fails
     */
    IntygContentHolder fetchIntygData(String intygId);

    /**
     * Fetches the intyg data from the intygstjanst and returns the intyg content in external model representation.
     *
     * @throws se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException if there occurs a problem fetching intyg data from the intygstjanst
     * @throws se.inera.certificate.integration.rest.exception.ModuleCallFailedException        if a call to the module API fails
     */
    IntygContentHolder fetchExternalIntygData(String intygId);

    /**
     * Returns all certificates for the given patient within all the given units.
     *
     * @param enhetId      list of HSA IDs for the units
     * @param personnummer the person number
     * @return list of certificates matching the search criteria
     */
    List<IntygItem> listIntyg(List<String> enhetId, String personnummer);

    /**
     * Returns a given certificate as PDF.
     *
     * @param intygId
     * @return
     */
    IntygPdf fetchIntygAsPdf(String intygId);

    boolean storeIntyg(Intyg intyg);

    boolean storeIntyg(Omsandning omsandning);
    
    boolean sendIntyg(Omsandning omsandning);
    
    boolean sendIntyg(String intygId, String recipient);
}
