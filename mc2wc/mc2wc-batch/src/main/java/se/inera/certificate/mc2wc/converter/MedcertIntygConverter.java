package se.inera.certificate.mc2wc.converter;

import se.inera.certificate.mc2wc.message.CertificateType;
import se.inera.webcert.persistence.legacy.model.MigreratMedcertIntyg;

public interface MedcertIntygConverter {

    public MigreratMedcertIntyg toMigreratMedcertIntyg(CertificateType cert);

}
