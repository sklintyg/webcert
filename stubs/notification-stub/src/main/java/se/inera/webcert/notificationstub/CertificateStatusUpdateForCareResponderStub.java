package se.inera.webcert.notificationstub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.Arbetsformaga;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.FragorOchSvar;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;

public class CertificateStatusUpdateForCareResponderStub implements CertificateStatusUpdateForCareResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateForCareResponderStub.class);

    @Autowired
    private NotificationStore notificationStore;

    @Override
    public CertificateStatusUpdateForCareResponseType certificateStatusUpdateForCare(String logicalAddress,
            CertificateStatusUpdateForCareType request) {
        UtlatandeType utlatande = request.getUtlatande();

        String handelseKod = utlatande.getHandelse().getHandelsekod().getCode();
        String utlatandeId = utlatande.getUtlatandeId().getExtension();

        StringBuilder sb = new StringBuilder();
        
        if (utlatande.getSigneringsdatum() != null) {
            sb.append(" Signeringsdatum: " + utlatande.getSigneringsdatum());
            sb.append("\n");
        }
        
        if (utlatande.getPatient() != null) {
            sb.append(" Patient: " + utlatande.getPatient().getPersonId().getExtension());
            sb.append("\n");
        }

        if (utlatande.getDiagnos() != null) {
            sb.append(" Diagnoskod: " + utlatande.getDiagnos().getCode());
            sb.append("\n");
        }

        if (!utlatande.getArbetsformaga().isEmpty()) {
            sb.append(" Arbetsformagor: ");
            for (Arbetsformaga arbFormaga : utlatande.getArbetsformaga()) {
                sb.append("[" + arbFormaga.getVarde().getValue() + "% ");
                sb.append(arbFormaga.getPeriod().getFrom() + "->");
                sb.append(arbFormaga.getPeriod().getTom() + "] ");
            }
            sb.append("\n");
        }

        FragorOchSvar fs = utlatande.getFragorOchSvar();
        sb.append(" Fragor: " + fs.getAntalFragor());
        sb.append(", Hant. fragor: " + fs.getAntalHanteradeFragor());
        sb.append(", Svar: " + fs.getAntalSvar());
        sb.append(", Hant. svar: " + fs.getAntalHanteradeSvar());
        sb.append("\n");

        LOG.info("\n*********************************************************************************\n"
                + " Request to address '{}' recieved for intyg: {} handelse: {}.\n"
                + "{}"
                + "*********************************************************************************", logicalAddress, utlatandeId, handelseKod, sb.toString());

        notificationStore.put(utlatandeId, request);

        CertificateStatusUpdateForCareResponseType response = new CertificateStatusUpdateForCareResponseType();
        response.setResult(ResultTypeUtil.okResult());
        LOG.debug("Request set to 'OK'");
        return response;
    }

}
