package se.inera.intyg.webcert.web.service.signatur;

import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;

public interface SignaturService {

    /**
     * This method is used when signing using other methods than NetId.
     *
     * @param intygId intygid
     * @param version version
     * @return SignatureTicket
     */
    SignaturTicket serverSignature(String intygId, long version);

    /**
     * This method is used when signing using NetId.
     */
    SignaturTicket clientSignature(String biljettId, String rawSignatur);

    /**
     * This method is used by the GRP collect mechanism implemented in {se.inera.intyg.webcert.web.service.signatur.grp.GrpPoller}
     * to finalize signing operations after GRP collect has completed with success status.
     *
     * Since this doesn't run in the context of a HTTP request there is no Principal to fetch from the executing
     * ThreadLocal, hence the webCertUser parameter.
     */
    SignaturTicket clientGrpSignature(String biljettId, String rawSignatur, WebCertUser webCertUser);

    /**
     * Checks the status of the signing ticket.
     *
     * @return The signing ticket corresponding to the supplied biljettId or a blank ticket no ticket was found.
     */
    SignaturTicket ticketStatus(String biljettId);

    /**
     * This method is used to generate a signing ticket based on the payload of an Intyg.
     *
     * @param intygId The id of the draft to generate signing ticket for
     * @param version version
     */
    SignaturTicket createDraftHash(String intygId, long version);

}
