package se.inera.intyg.webcert.web.service.intyg;

import java.util.List;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygItemListResponse;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;

/**
 * @author andreaskaltenbach
 */
public interface IntygService {

    /**
     * Fetches the intyg data from the Intygstjanst and returns the intyg content in internal model representation.
     *
     * If the Intygstjanst couldn't find the intyg or the Intygstjanst was not available,
     * an attempt to find an utkast stored in Webcert will be performed.
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
    IntygItemListResponse listIntyg(List<String> enhetId, Personnummer personnummer);

    /**
     * Returns a given certificate as PDF.
     *
     * @param isEmployer
     *            Indicates if the certificate should be for the employer.
     */
    IntygPdf fetchIntygAsPdf(String intygId, String typ, boolean isEmployer);

    /**
     * Registers a given certificate in the Intygstjanst.
     */
    IntygServiceResult storeIntyg(Utkast utkast);

    /**
     * Instructs Intygstjanst to deliver the given certifiate to an external recipient.
     */
    IntygServiceResult sendIntyg(String intygId, String typ, String mottagare, boolean hasPatientConsent);

    /**
     * Instructs Intygstjanst to revoke the given certificate.
     */
    IntygServiceResult revokeIntyg(String intygId, String intygTyp, String revokeMessage);

}
