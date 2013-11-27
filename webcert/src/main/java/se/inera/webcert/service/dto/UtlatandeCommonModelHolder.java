package se.inera.webcert.service.dto;

import se.inera.certificate.integration.rest.dto.CertificateContentMeta;
import se.inera.certificate.model.Utlatande;


/**
 * Wrapper class for holding a common model {@link Utlatande} as well as the status history of the certificate.
 * @author marced
 */
public class UtlatandeCommonModelHolder {
    
    
    private Utlatande utlatande;
    
    private CertificateContentMeta certificateContentMeta;


    public UtlatandeCommonModelHolder(Utlatande utlatande, CertificateContentMeta metaData) {
        this.utlatande = utlatande;
        this.certificateContentMeta = metaData;
    }

    public UtlatandeCommonModelHolder() {}

    public CertificateContentMeta getCertificateContentMeta() {
        return certificateContentMeta;
    }

    public void setCertificateContentMeta(CertificateContentMeta certificateContentMeta) {
        this.certificateContentMeta = certificateContentMeta;
    }

    public Utlatande getUtlatande() {
        return utlatande;
    }

    public void setCertificateContent(Utlatande utlatande) {
        this.utlatande = utlatande;
    }



}
