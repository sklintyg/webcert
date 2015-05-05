package se.inera.webcert.service.signatur;

import se.inera.webcert.service.signatur.dto.SignaturTicket;

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
     * This method is used when signing using NetId
     * 
     * @param biljettId
     * @param rawSignatur
     * @return
     */
    SignaturTicket clientSignature(String biljettId, String rawSignatur);

    /**
     * Checks the status of the signing ticket.
     * 
     * @param biljettId
     * @return The signing ticket corresponding to the supplied biljettId or a blank ticket no ticket was found.
     */
    SignaturTicket ticketStatus(String biljettId);

    /**
     * This method is used to generate a signing ticket based on the payload of an Intyg.
     * 
     * @param intygId The id of the draft to generate signing ticket for
     * @param version version
     * @return
     */
    SignaturTicket createDraftHash(String intygId, long version);

}
