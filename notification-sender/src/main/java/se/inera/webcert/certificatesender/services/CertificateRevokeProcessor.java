package se.inera.webcert.certificatesender.services;

import static com.google.common.base.Preconditions.checkArgument;

import javax.xml.ws.WebServiceException;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.webcert.certificatesender.exception.PermanentException;
import se.inera.webcert.certificatesender.exception.TemporaryException;
import se.inera.webcert.certificatesender.services.converter.RevokeRequestConverter;
import se.inera.webcert.common.Constants;

/**
 * Created by eriklupander on 2015-05-21.
 */
public class CertificateRevokeProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateSendProcessor.class);

    @Autowired
    private RevokeMedicalCertificateResponderInterface revokeService;

    @Autowired
    private RevokeRequestConverter revokeRequestConverter;

    public void process(@Body String xmlBody, @Header(Constants.INTYGS_ID) String intygsId, @Header(Constants.LOGICAL_ADDRESS) String logicalAddress) throws Exception {

        checkArgument(!nullOrEmpty(xmlBody), "Message of type %s with intygsId %s has no body.", Constants.REVOKE_MESSAGE, intygsId);
        checkArgument(!nullOrEmpty(intygsId), "Message of type %s does not have a %s header.", Constants.REVOKE_MESSAGE, Constants.INTYGS_ID);
        checkArgument(!nullOrEmpty(logicalAddress), "Message of type %s with intygsId %s has no %s header.", Constants.REVOKE_MESSAGE, intygsId, Constants.LOGICAL_ADDRESS);

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
                                    intygsId, resultOfCall.getErrorText(), resultOfCall.getErrorId());
                            throw new TemporaryException(resultOfCall.getErrorText());
                        case TRANSFORMATION_ERROR:
                        case VALIDATION_ERROR:
                            LOG.error("Call to revoke intyg {} caused an error: {}, ErrorId: {}. Rethrowing as PermanentException",
                                    intygsId, resultOfCall.getErrorText(), resultOfCall.getErrorId());
                            throw new PermanentException(resultOfCall.getErrorText());
                    }
                    throw new TemporaryException(resultOfCall.getErrorText());
                default:
                    throw new TemporaryException(resultOfCall.getErrorText());
            }

        } catch (WebServiceException e) {
            LOG.error("Call to revoke intyg {} caused an error: {}. Will retry",
                    intygsId, e.getMessage());
            throw new TemporaryException(e.getMessage());
        }
    }

    private boolean nullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

}
