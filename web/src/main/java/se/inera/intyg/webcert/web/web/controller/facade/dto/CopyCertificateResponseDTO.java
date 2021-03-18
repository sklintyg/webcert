package se.inera.intyg.webcert.web.web.controller.facade.dto;

public class CopyCertificateResponseDTO {

    private String certificateId;

    public static CopyCertificateResponseDTO create(String certificateId) {
        final CopyCertificateResponseDTO responseDTO = new CopyCertificateResponseDTO();
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
