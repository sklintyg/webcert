package se.inera.certificate.mc2wc.converter;

import org.joda.time.LocalDateTime;
import se.inera.certificate.mc2wc.message.CertificateType;
import se.inera.certificate.mc2wc.message.PatientType;
import se.inera.webcert.persistence.legacy.model.MigreratMedcertIntyg;

public class MedcertIntygConverterImpl implements MedcertIntygConverter {

    @Override
    public MigreratMedcertIntyg toMigreratMedcertIntyg(CertificateType cert) {

        MigreratMedcertIntyg mmCert = new MigreratMedcertIntyg();

        mmCert.setIntygsId(cert.getCertificateId());
        mmCert.setEnhetsId(cert.getCareUnitId());
        mmCert.setIntygsTyp(cert.getCertificateType());

        PatientType patient = cert.getPatient();
        mmCert.setPatientNamn(patient.getFullName());
        mmCert.setPatientPersonnummer(patient.getPersonId());

        mmCert.setUrsprung(cert.getOrigin());
        mmCert.setMigreradFran(cert.getMigratedFrom());

        mmCert.setSkapad(cert.getCreated());
        mmCert.setSkickad(cert.getSent());

        mmCert.setIntygsData(cert.getContents());
        mmCert.setStatus(cert.getStatus().value());
        mmCert.setMigrerad(new LocalDateTime());

        return mmCert;
    }

}
