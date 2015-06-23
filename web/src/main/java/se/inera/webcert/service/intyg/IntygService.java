package se.inera.webcert.service.intyg;

import java.util.List;

import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.service.intyg.dto.*;

/**
 * @author andreaskaltenbach
 */
public interface IntygService {

    /**
     * Fetches the intyg data from the Intygstjanst and returns the intyg content in internal model representation.
     *
     * If the Intygstjanst couldn't find the intyg or the Intygstjanst was not available,
     * an attempt to find an utkast stored in Webcert will be performed.
     *
     * @param intygId
     * @param typ
     * @return
     */
    IntygContentHolder fetchIntygData(String intygId, String typ);

    /**
     * Returns all certificates for the given patient within all the given units.
     *
     * @param enhetId
     *            list of HSA IDs for the units
     * @param personnummer
     *            the person number
     * @return list of certificates matching the search criteria wrapped in a response container also indicating whether
     *         the data was fetched from intygstjansten ("online") or from webcert ("offline").
     */
    IntygItemListResponse listIntyg(List<String> enhetId, String personnummer);

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
     * @param utkast
     * @return
     */
    IntygServiceResult storeIntyg(Utkast utkast);

    /**
     * Instructs Intygstjanst to deliver the given certifiate to an external recipient.
     *
     * @param intygId
     * @param typ
     * @param mottagare
     * @param hasPatientConsent
     * @return
     */
    IntygServiceResult sendIntyg(String intygId, String typ, String mottagare, boolean hasPatientConsent);

    /**
     * Instructs Intygstjanst to revoke the given certificate.
     *
     * @param intygId
     * @param intygTyp
     * @param revokeMessage
     * @return
     */
    IntygServiceResult revokeIntyg(String intygId, String intygTyp, String revokeMessage);

}
