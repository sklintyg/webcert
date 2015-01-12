package se.inera.webcert.notifications.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.EnhetType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.HosPersonalType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.HsaId;
import se.inera.webcert.persistence.intyg.model.Utkast;
import se.inera.webcert.persistence.intyg.model.UtkastStatus;
import se.inera.webcert.persistence.intyg.model.Signatur;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;

public class EnrichWithIntygDataStrategy {

    private static final String HSAID_ROOT = "1.2.752.129.2.1.4.1";

    private static final Logger LOG = LoggerFactory.getLogger(EnrichWithIntygDataStrategy.class);

    public CertificateStatusUpdateForCareType enrichWithIntygProperties(CertificateStatusUpdateForCareType statusUpdateType, Utkast intygsUtkast) {

        LOG.debug("Enriching CertificateStatusUpdateForCareType with data from intygsutkast {}", intygsUtkast.getIntygsId());

        UtlatandeType utlatandeType = statusUpdateType.getUtlatande();

        decorateWithHoSPerson(utlatandeType, intygsUtkast);
        decorateWithSignDate(utlatandeType, intygsUtkast);

        return statusUpdateType;
    }

    private void decorateWithSignDate(UtlatandeType utlatandeType, Utkast intygsUtkast) {
        if (UtkastStatus.SIGNED.equals(intygsUtkast.getStatus())) {
            LOG.debug("Status is SIGNED, getting signed date from signature");
            Signatur signatur = intygsUtkast.getSignatur();
            utlatandeType.setSigneringsdatum(signatur.getSigneringsDatum());
        }
    }

    private void decorateWithHoSPerson(UtlatandeType utlatandeType, Utkast intygsUtkast) {

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
        hsaId.setRoot(HSAID_ROOT);
        hsaId.setExtension(id);
        return hsaId;
    }
}
