package se.inera.intyg.webcert.web.web.controller.api.dto;

public class KlientSignaturRequest {

    private byte[] signatur;
    private String certifikat;

    public byte[] getSignatur() {
        return signatur;
    }

    public void setSignatur(byte[] signatur) {
        this.signatur = signatur;
    }

    public String getCertifikat() {
        return certifikat;
    }

    public void setCertifikat(String certifikat) {
        this.certifikat = certifikat;
    }
}
