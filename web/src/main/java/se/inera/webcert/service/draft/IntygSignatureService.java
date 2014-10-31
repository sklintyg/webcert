package se.inera.webcert.service.draft;

import se.inera.webcert.service.draft.dto.SignatureTicket;

public interface IntygSignatureService {

    /**
     * Signera intyg.
     *
     * @param intygId intygid
     * @return SignatureTicket
     */
    SignatureTicket serverSignature(String intygId);

    SignatureTicket clientSignature(String biljettId, String rawSignatur);

    SignatureTicket ticketStatus(String biljettId);

    SignatureTicket createDraftHash(String intygId);

}
