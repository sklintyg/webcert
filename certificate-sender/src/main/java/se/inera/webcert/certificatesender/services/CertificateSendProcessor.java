package se.inera.webcert.certificatesender.services;

import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;
import se.inera.webcert.certificatesender.exception.PermanentException;
import se.inera.webcert.certificatesender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;

import javax.xml.ws.WebServiceException;

/**
 * Created by eriklupander on 2015-05-21.
 */
public class CertificateSendProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateSendProcessor.class);

    private static final java.lang.String INTYGS_ID = "INTYGS_ID";
    private static final java.lang.String PERSON_ID = "PERSON_ID";
    private static final java.lang.String RECIPIENT = "RECIPIENT";
    private static final String LOGICAL_ADDRESS = "LOGICAL_ADDRESS";

    @Autowired
    private SendCertificateToRecipientResponderInterface sendService;


    public void process(Message message) throws Exception {
        LOG.debug("Receiving message: {}", message.getMessageId());

        String intygsId = (String) message.getHeader(INTYGS_ID);
        String personId = (String) message.getHeader(PERSON_ID);
        String recipient = (String) message.getHeader(RECIPIENT);
        String logicalAddress = (String) message.getHeader(LOGICAL_ADDRESS);

        SendCertificateToRecipientType request = new SendCertificateToRecipientType();
        request.setUtlatandeId(intygsId);
        request.setPersonId(personId);
        request.setMottagareId(recipient);

        SendCertificateToRecipientResponseType response = null;
        try {
            response = sendService.sendCertificateToRecipient(logicalAddress, request);
        } catch (WebServiceException e) {
            LOG.error("Call to revoke intyg {} caused an error: {}, ErrorId: {}. Will retry",
                    new Object[]{intygsId, response.getResult().getResultText(), response.getResult().getErrorId()});
            throw new TemporaryException(response.getResult().getResultText());
        }

        // check whether call was successful or not
        if (ResultCodeType.ERROR.equals(response.getResult().getResultCode())) {

            LOG.error("Error occured when trying to send intyg '{}'; {}", intygsId, response.getResult().getResultText());

            switch (response.getResult().getErrorId()) {
                case APPLICATION_ERROR:
                case TECHNICAL_ERROR:
                    throw new TemporaryException(response.getResult().getResultText());
                case REVOKED:
                case VALIDATION_ERROR:
                    throw new PermanentException(response.getResult().getResultText());
            }

            throw new TemporaryException("Error occured when trying to send intyg '"+intygsId+"'; "+response.getResult().getResultText());
        } else {
            if (ResultCodeType.INFO.equals(response.getResult().getResultCode())) {
                LOG.warn("Warning occured when trying to send intyg '{}'; {}. Will not requeue." , intygsId, response.getResult().getResultText());
            }
        }
    }

}
