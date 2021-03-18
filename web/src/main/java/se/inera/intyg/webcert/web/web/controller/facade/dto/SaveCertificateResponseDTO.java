package se.inera.intyg.webcert.web.web.controller.facade.dto;

public class SaveCertificateResponseDTO {

    private long version;

    public static SaveCertificateResponseDTO create(long version) {
        final SaveCertificateResponseDTO responseDTO = new SaveCertificateResponseDTO();
        responseDTO.version = version;
        return responseDTO;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
