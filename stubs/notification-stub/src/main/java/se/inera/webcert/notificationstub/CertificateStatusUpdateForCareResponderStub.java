package se.inera.webcert.notificationstub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;

public class CertificateStatusUpdateForCareResponderStub implements CertificateStatusUpdateForCareResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateForCareResponderStub.class);

    @Autowired
    NotificationStore notificationStore;

    @Override
    public CertificateStatusUpdateForCareResponseType certificateStatusUpdateForCare(String logicalAddress,
            CertificateStatusUpdateForCareType request) {
        String handelseKod = request.getUtlatande().getHandelse().getHandelsekod().getCode();
        String utlatandeId = request.getUtlatande().getUtlatandeId().getExtension();
        LOG.info("\n*********************************************************************************\n"
                + " Request to address '{}' recieved for intyg: {} handelse: {}.\n"
                + "*********************************************************************************", logicalAddress, utlatandeId, handelseKod);

        notificationStore.put(utlatandeId, request);

        CertificateStatusUpdateForCareResponseType response = new CertificateStatusUpdateForCareResponseType();
        response.setResult(ResultTypeUtil.okResult());
        LOG.debug("Request set to 'OK'");
        return response;
    }

}
