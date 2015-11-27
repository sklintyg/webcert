package se.inera.intyg.webcert.notification_sender.notifications.services;

import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.notification_sender.exception.PermanentException;
import se.inera.intyg.webcert.notification_sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.notifications.routes.RouteHeaders;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

public class NotificationWSClient {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationWSClient.class);

    @Autowired
    private CertificateStatusUpdateForCareResponderInterface statusUpdateForCareClient;

    public void sendStatusUpdate(CertificateStatusUpdateForCareType request,
            @Header(RouteHeaders.LOGISK_ADRESS) String logicalAddress) throws Exception {

        LOG.debug("Sending status update to '{}' for intyg '{}'", logicalAddress, request.getUtlatande().getUtlatandeId().getExtension());

        CertificateStatusUpdateForCareResponseType response = null;

        try {
            response = statusUpdateForCareClient.certificateStatusUpdateForCare(logicalAddress, request);
        } catch (Exception e) {
            LOG.warn("Exception occured when sending status update: {}", e.getMessage());
            throw new TemporaryException(e);
        }

        ResultType result = response.getResult();
        switch (result.getResultCode()) {
        case ERROR:
            if (result.getErrorId().equals(ErrorIdType.TECHNICAL_ERROR)) {
                throw new TemporaryException(String.format("NotificationWSClient failed with error code: %s and message %s",
                        result.getErrorId(),
                        result.getResultText()));
            } else {
                throw new PermanentException(String.format("NotificationWSClient failed with non-recoverable error code: %s and message %s",
                        result.getErrorId(),
                        result.getResultText()));
            }
        case INFO:
            LOG.info("NotificationWSClient got message:" + result.getResultText());
            break;
        case OK:
            break;
        default:
            throw new PermanentException("Unhandled result code type: " + result.getResultCode());
        }

    }
}
