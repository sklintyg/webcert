package se.inera.webcert.certificatesender.services;

import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.webcert.certificatesender.exception.PermanentException;
import se.inera.webcert.certificatesender.exception.TemporaryException;
import se.inera.webcert.certificatesender.services.converter.RevokeRequestConverter;
import se.inera.webcert.certificatesender.services.validator.CertificateMessageValidator;
import se.inera.webcert.common.Constants;

import javax.xml.ws.WebServiceException;

/**
 * Created by eriklupander on 2015-05-21.
 */
public class CertificateRevokeProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateSendProcessor.class);

    @Autowired
    private RevokeMedicalCertificateResponderInterface revokeService;

    @Autowired
    private RevokeRequestConverter revokeRequestConverter;

    @Autowired
    @Qualifier("certificateRevokeMessageValidator")
    private CertificateMessageValidator certificateRevokeMessageValidator;

    public void process(Message message) throws Exception {
        LOG.debug("Receiving message: {}", message.getMessageId());

        certificateRevokeMessageValidator.validate(message);

        String intygsId = (String) message.getHeader(Constants.INTYGS_ID);
        String logicalAddress = (String) message.getHeader(Constants.LOGICAL_ADDRESS);
        String xmlBody = (String) message.getBody();

        RevokeMedicalCertificateRequestType request = revokeRequestConverter.fromXml(xmlBody);

        AttributedURIType uri = new AttributedURIType();
        uri.setValue(logicalAddress);
        ResultOfCall resultOfCall = null;

        try {
            // Revoke the certificate
            RevokeMedicalCertificateResponseType response = revokeService.revokeMedicalCertificate(uri, request);

            // Take care of the response
            resultOfCall = response.getResult();

            switch (resultOfCall.getResultCode()) {
                case OK:

                case INFO:
                    return;
                case ERROR:
                    switch (resultOfCall.getErrorId()) {
                        case APPLICATION_ERROR:
                        case TECHNICAL_ERROR:
                            LOG.error("Call to revoke intyg {} caused an error: {}, ErrorId: {}. Rethrowing as TemporaryException",
                                    new Object[] { intygsId, resultOfCall.getErrorText(), resultOfCall.getErrorId() });
                            throw new TemporaryException(resultOfCall.getErrorText());
                        case TRANSFORMATION_ERROR:
                        case VALIDATION_ERROR:
                            LOG.error("Call to revoke intyg {} caused an error: {}, ErrorId: {}. Rethrowing as PermanentException",
                                    new Object[] { intygsId, resultOfCall.getErrorText(), resultOfCall.getErrorId() });
                            throw new PermanentException(resultOfCall.getErrorText());
                    }
                    throw new TemporaryException(resultOfCall.getErrorText());
                default:
                    throw new TemporaryException(resultOfCall.getErrorText());
            }

        } catch (WebServiceException e) {
            LOG.error("Call to revoke intyg {} caused an error: {}. Will retry",
                    new Object[]{intygsId, e.getMessage()});
            throw new TemporaryException(e.getMessage());
        }
    }
}
