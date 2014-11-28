package se.inera.webcert.notifications.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.EnhetType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.HosPersonalType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.HsaId;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;

public class EnrichWithIntygDataStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(EnrichWithIntygDataStrategy.class);

    public CertificateStatusUpdateForCareType enrichCertificateStatusUpdate(CertificateStatusUpdateForCareType statusUpdateType, Intyg intygsUtkast) {

        LOG.info("Enriching CertificateStatusUpdateForCareType with data from intygsutkast {}", intygsUtkast.getIntygsId());

        UtlatandeType utlatandeType = statusUpdateType.getUtlatande();

        decorateWithSignDate(utlatandeType, intygsUtkast);
        decorateWithHoSPerson(utlatandeType, intygsUtkast);

        // content that has to be extracted from the certificate model
        // utlatandeType.setDiagnos(value);
        // utlatandeType.getArbetsformaga()

        return statusUpdateType;
    }

    private void decorateWithSignDate(UtlatandeType utlatandeType, Intyg intygsUtkast) {
        if (IntygsStatus.SIGNED.equals(intygsUtkast.getStatus())) {

            utlatandeType.setSigneringsdatum(null);
        }
    }

    private void decorateWithHoSPerson(UtlatandeType utlatandeType, Intyg intygsUtkast) {

        VardpersonReferens vardpersonReferens = intygsUtkast.getSkapadAv();

        HosPersonalType hoSPerson = new HosPersonalType();
        hoSPerson.setFullstandigtNamn(vardpersonReferens.getNamn());

        HsaId personHsaId = createHsaId(vardpersonReferens.getHsaId());
        hoSPerson.setPersonalId(personHsaId);

        EnhetType vardEnhet = new EnhetType();
        vardEnhet.setEnhetsnamn(intygsUtkast.getEnhetsNamn());

        HsaId vardEnhetHsaId = createHsaId(vardpersonReferens.getHsaId());
        vardEnhet.setEnhetsId(vardEnhetHsaId);

        hoSPerson.setEnhet(vardEnhet);

        utlatandeType.setSkapadAv(hoSPerson);
    }

    private HsaId createHsaId(String id) {
        HsaId hsaId = new HsaId();
        hsaId.setRoot("adf");
        hsaId.setExtension(id);
        return hsaId;
    }
}
