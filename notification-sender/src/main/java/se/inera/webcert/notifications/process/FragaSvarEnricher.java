package se.inera.webcert.notifications.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.FragorOchSvarType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;

public class FragaSvarEnricher {

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarEnricher.class);

    public CertificateStatusUpdateForCareType enrichWithNbrOfQuestionsForIntyg(CertificateStatusUpdateForCareType statusUpdateType,
            Long nbrOfQuestionsForIntyg) {
        LOG.debug("Adding nbr of questions to CertificateStatusUpdateForCareType: {}", nbrOfQuestionsForIntyg);
        FragorOchSvarType fragaSvarType = getFragorOchSvarTypeFromUtlatandeType(statusUpdateType);
        fragaSvarType.setAntalFragor(longToInt(nbrOfQuestionsForIntyg));

        return statusUpdateType;
    }

    public CertificateStatusUpdateForCareType enrichWithNbrOfAnsweredQuestionsForIntyg(CertificateStatusUpdateForCareType statusUpdateType,
            Long nbrOfAnsweredQuestionsForIntyg) {
        LOG.debug("Adding nbr of answered questions to CertificateStatusUpdateForCareType: {}", nbrOfAnsweredQuestionsForIntyg);
        FragorOchSvarType fragaSvarType = getFragorOchSvarTypeFromUtlatandeType(statusUpdateType);
        fragaSvarType.setAntalSvar(longToInt(nbrOfAnsweredQuestionsForIntyg));

        return statusUpdateType;
    }

    public CertificateStatusUpdateForCareType enrichWithNbrOfHandledQuestionsForIntyg(CertificateStatusUpdateForCareType statusUpdateType,
            Long nbrOfHandledQuestionsForIntyg) {
        LOG.debug("Adding nbr of handled questions to CertificateStatusUpdateForCareType: {}", nbrOfHandledQuestionsForIntyg);
        FragorOchSvarType fragaSvarType = getFragorOchSvarTypeFromUtlatandeType(statusUpdateType);
        fragaSvarType.setAntalHanteradeFragor(longToInt(nbrOfHandledQuestionsForIntyg));

        return statusUpdateType;
    }

    public CertificateStatusUpdateForCareType enrichWithNbrOfHandledAndAnsweredQuestionsForIntyg(CertificateStatusUpdateForCareType statusUpdateType,
            Long nbrOfHandledAndAnsweredQuestionsForIntyg) {
        LOG.debug("Adding nbr of handled and answered questions to CertificateStatusUpdateForCareType: {}", nbrOfHandledAndAnsweredQuestionsForIntyg);
        FragorOchSvarType fragaSvarType = getFragorOchSvarTypeFromUtlatandeType(statusUpdateType);
        fragaSvarType.setAntalHanteradeSvar(longToInt(nbrOfHandledAndAnsweredQuestionsForIntyg));

        return statusUpdateType;
    }

    private int longToInt(Long value) {
        return (value != null) ? value.intValue() : -1;
    }

    private FragorOchSvarType getFragorOchSvarTypeFromUtlatandeType(CertificateStatusUpdateForCareType statusUpdateType) {

        UtlatandeType utlatandeType = statusUpdateType.getUtlatande();

        if (utlatandeType.getFragorOchSvar() == null) {
            LOG.debug("Creating FragorOchSvarType since none was found in UtlatandeType");
            FragorOchSvarType fragaSvarType = new FragorOchSvarType();
            utlatandeType.setFragorOchSvar(fragaSvarType);
        }

        return utlatandeType.getFragorOchSvar();
    }
}
