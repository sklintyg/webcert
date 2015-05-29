package se.inera.webcert.certificatesender.services;

import javax.xml.ws.WebServiceException;

import com.google.common.base.Preconditions;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;
import se.inera.webcert.certificatesender.exception.PermanentException;
import se.inera.webcert.certificatesender.exception.TemporaryException;
import se.inera.webcert.common.Constants;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by eriklupander on 2015-05-21.
 */
public class CertificateSendProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateSendProcessor.class);

    @Autowired
    private SendCertificateToRecipientResponderInterface sendService;

    public void process(@Header(Constants.INTYGS_ID) String intygsId,
            @Header(Constants.PERSON_ID) String personId,
            @Header(Constants.RECIPIENT) String recipient,
            @Header(Constants.LOGICAL_ADDRESS) String logicalAddress) throws Exception {

        checkArgument(!nullOrEmpty(intygsId));
        checkArgument(!nullOrEmpty(personId));
        checkArgument(!nullOrEmpty(recipient));
        checkArgument(!nullOrEmpty(logicalAddress));

        SendCertificateToRecipientType request = new SendCertificateToRecipientType();
        request.setUtlatandeId(intygsId);
        request.setPersonId(personId);
        request.setMottagareId(recipient);

        SendCertificateToRecipientResponseType response;
        try {
            response = sendService.sendCertificateToRecipient(logicalAddress, request);
        } catch (WebServiceException e) {
            LOG.warn("Call to revoke intyg {} caused an error: {}. Will retry", intygsId, e.getMessage());
            throw new TemporaryException(e.getMessage());
        } catch (Exception e) {
            throw new PermanentException(e.getMessage());
        }

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
    }

    private boolean nullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

}
