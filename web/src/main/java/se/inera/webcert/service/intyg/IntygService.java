package se.inera.webcert.service.intyg;

import java.util.List;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.intyg.dto.IntygPdf;
import se.inera.webcert.service.intyg.dto.IntygRecipient;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;

/**
 * @author andreaskaltenbach
 */
public interface IntygService {

    /**
     * Fetches the intyg data from the Intygstjanst and returns the intyg content in internal model representation.
     *
     * @throws se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException
     *             if there occurs a problem fetching intyg data from the intygstjanst
     * @throws se.inera.certificate.integration.rest.exception.ModuleCallFailedException
     *             if a call to the module API fails
     */
    IntygContentHolder fetchIntygData(String intygId, String typ);

    /**
     * Returns all certificates for the given patient within all the given units.
     *
     * @param enhetId
     *            list of HSA IDs for the units
     * @param personnummer
     *            the person number
     * @return list of certificates matching the search criteria
     */
    List<IntygItem> listIntyg(List<String> enhetId, String personnummer);

    /**
     * Returns a given certificate as PDF.
     *
     * @param intygId
     * @return
     */
    IntygPdf fetchIntygAsPdf(String intygId, String typ);

    /**
     * Registers a given certificate in the Intygstjanst.
     *
     * @param intyg
     * @return
     */
    IntygServiceResult storeIntyg(Intyg intyg);

    /**
     * Instructs Intygstjanst to deliver the given certifiate to an external recipient.
     *
     * @param intygId
     * @param typ
     * @param recipient
     * @param hasPatientConsent
     * @return
     */
    IntygServiceResult sendIntyg(String intygId, String typ, String recipient, boolean hasPatientConsent);
    
    /**
     * Retrieves a list over available recipients for a particular type of certificate.
     *
     * @param intygType
     * @return
     */
    List<IntygRecipient> fetchListOfRecipientsForIntyg(String intygType);

    /**
     * Instructs Intygstjanst to revoke the given certificate.
     *
     * @param intygId
     * @param intygType
     * @param revokeMessage
     * @return
     */
    IntygServiceResult revokeIntyg(String intygId, String intygType, String revokeMessage);
    
}
