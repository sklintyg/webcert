package se.inera.intyg.common.specifications.spec

import static groovyx.net.http.ContentType.JSON
import se.inera.intyg.common.specifications.spec.util.RestClientFixture

/**
 * Läs upp ett intyg via intygsId, för att verifiera vårdEnhet, vårdGivare och wireTap-status.
 *
 */
public class KontrolleraIntyg extends RestClientFixture {

    String intyg
    private def certificate
    
    public void execute() {
        def restClient = createRestClient()
        def response = restClient.get(
                path: "certificate/${intyg}",
                requestContentType: JSON
                )
        certificate = response.data
    }
    
    public String personNr() {
        certificate.civicRegistrationNumber
    }

    public String vårdEnhet() {
        certificate.careUnitId
    }

    public String vårdGivare() {
        certificate.careGiverId
    }

    public boolean wiretappat() {
        certificate.wireTapped
    }
}
