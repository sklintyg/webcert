package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import javax.xml.ws.WebServiceException;

import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.webcert.common.client.SendCertificateServiceClient;
import se.inera.intyg.webcert.common.common.Constants;
import se.inera.intyg.webcert.notification_sender.exception.PermanentException;
import se.inera.intyg.webcert.notification_sender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

/**
 * Created by eriklupander on 2015-05-21.
 */
public class CertificateSendProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateSendProcessor.class);

    @Autowired
    private SendCertificateServiceClient sendServiceClient;

    public void process(@Header(Constants.INTYGS_ID) String intygsId,
            @Header(Constants.PERSON_ID) String personId,
            @Header(Constants.RECIPIENT) String recipient,
            @Header(Constants.LOGICAL_ADDRESS) String logicalAddress) throws Exception {



        SendCertificateToRecipientResponseType response;
        try {
            response = sendServiceClient.sendCertificate(intygsId, personId, recipient, logicalAddress);

            final ResultType result = response.getResult();
            final String resultText = result.getResultText();
            if (ResultCodeType.ERROR == result.getResultCode()) {
                LOG.warn("Error occured when trying to send intyg '{}'; {}", intygsId, resultText);

                switch (result.getErrorId()) {
                    case APPLICATION_ERROR:
                    case TECHNICAL_ERROR:
                        throw new TemporaryException(resultText);
                    case REVOKED:
                    case VALIDATION_ERROR:
                        throw new PermanentException(resultText);
                    default:
                        throw new PermanentException("Unhandled error type: " + result.getErrorId() + " - " + resultText);
                }

            } else {
                if (ResultCodeType.INFO.equals(result.getResultCode())) {
                    LOG.warn("Warning occured when trying to send intyg '{}'; {}. Will not requeue.", intygsId, resultText);
                }
            }

        } catch (WebServiceException e) {
            LOG.warn("Call to send intyg {} caused an error: {}. Will retry", intygsId, e.getMessage());
            throw new TemporaryException(e.getMessage());
        }
    }
}
