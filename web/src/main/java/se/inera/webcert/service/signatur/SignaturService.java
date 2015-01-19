package se.inera.webcert.service.signatur;

import se.inera.webcert.service.signatur.dto.SignaturTicket;

public interface SignaturService {

    /**
     * This method is used when signing using other methods than NetId.
     *
     * @param intygId intygid
     * @return SignatureTicket
     */
    SignaturTicket serverSignature(String intygId);

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
     * @return
     */
    SignaturTicket createDraftHash(String intygId);

}
