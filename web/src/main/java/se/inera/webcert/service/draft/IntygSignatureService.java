package se.inera.webcert.service.draft;

import se.inera.webcert.service.draft.dto.SignatureTicket;

public interface IntygSignatureService {

    /**
     * This method is used when signing using other methods than NetId.
     *
     * @param intygId intygid
     * @return SignatureTicket
     */
    SignatureTicket serverSignature(String intygId);

    /**
     * This method is used when signing using NetId
     * 
     * @param biljettId
     * @param rawSignatur
     * @return
     */
    SignatureTicket clientSignature(String biljettId, String rawSignatur);

    /**
     * Checks the status of the signing ticket.
     * 
     * @param biljettId
     * @return The signing ticket corresponding to the supplied biljettId or a blank ticket no ticket was found.
     */
    SignatureTicket ticketStatus(String biljettId);

    /**
     * This method is used to generate a signing ticket based on the payload of an Intyg.
     * 
     * @param intygId The id of the draft to generate signing ticket for
     * @return
     */
    SignatureTicket createDraftHash(String intygId);

}
