package se.inera.webcert.service.dto;

import se.inera.certificate.model.Utlatande;

/**
 * Wrapper class for holding a common model {@link Utlatande} as well as the
 * status history of the certificate.
 *
 * @author marced
 */
public class UtlatandeCommonModelHolder {

    private Utlatande utlatande;

    private IntygMetadata metaData;

    public UtlatandeCommonModelHolder(Utlatande utlatande, IntygMetadata metaData) {
        this.utlatande = utlatande;
        this.metaData = metaData;
    }

    public IntygMetadata getMetaData() {
        return metaData;
    }

    public void setMetaData(IntygMetadata metaData) {
        this.metaData = metaData;
    }

    public Utlatande getUtlatande() {
        return utlatande;
    }

    public void setCertificateContent(Utlatande utlatande) {
        this.utlatande = utlatande;
    }

}
