package se.inera.intyg.webcert.web.web.controller.facade.dto;

import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;

public class CertificateEventResponseDTO {

    private CertificateEventDTO[] certificateEvents;

    public static CertificateEventResponseDTO create(CertificateEventDTO[] certificateEvents) {
        final var responseDTO = new CertificateEventResponseDTO();
        responseDTO.certificateEvents = certificateEvents;
        return responseDTO;
    }

    public CertificateEventDTO[] getCertificateEvents() {
        return certificateEvents;
    }

    public void setCertificateEvents(CertificateEventDTO[] certificateEvents) {
        this.certificateEvents = certificateEvents;
    }
}
