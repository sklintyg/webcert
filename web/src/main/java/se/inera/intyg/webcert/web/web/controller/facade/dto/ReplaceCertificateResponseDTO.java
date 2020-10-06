package se.inera.intyg.webcert.web.web.controller.facade.dto;

public class ReplaceCertificateResponseDTO {

    private String certificateId;

    public static ReplaceCertificateResponseDTO create(String certificateId) {
        final ReplaceCertificateResponseDTO responseDTO = new ReplaceCertificateResponseDTO();
        responseDTO.certificateId = certificateId;
        return responseDTO;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }
}
