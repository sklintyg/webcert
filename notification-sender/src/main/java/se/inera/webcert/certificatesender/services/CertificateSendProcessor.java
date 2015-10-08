package se.inera.webcert.certificatesender.services;

import javax.xml.ws.WebServiceException;

import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.webcert.client.SendCertificateServiceClient;
import se.inera.webcert.common.Constants;
import se.inera.webcert.exception.PermanentException;
import se.inera.webcert.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;

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

            if (ResultCodeType.ERROR == response.getResult().getResultCode()) {
                LOG.warn("Error occured when trying to send intyg '{}'; {}", intygsId, response.getResult().getResultText());

                switch (response.getResult().getErrorId()) {
                    case APPLICATION_ERROR:
                    case TECHNICAL_ERROR:
                        throw new TemporaryException(response.getResult().getResultText());
                    case REVOKED:
                    case VALIDATION_ERROR:
                        throw new PermanentException(response.getResult().getResultText());
                }

            } else {
                if (ResultCodeType.INFO.equals(response.getResult().getResultCode())) {
                    LOG.warn("Warning occured when trying to send intyg '{}'; {}. Will not requeue.", intygsId, response.getResult().getResultText());
                }
            }

        } catch (WebServiceException e) {
            LOG.warn("Call to send intyg {} caused an error: {}. Will retry", intygsId, e.getMessage());
            throw new TemporaryException(e.getMessage());
        }
    }
}
