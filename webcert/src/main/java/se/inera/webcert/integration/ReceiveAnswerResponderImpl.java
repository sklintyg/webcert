package se.inera.webcert.integration;

import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.webcert.receivemedicalcertificateanswer.v1.rivtabp20.ReceiveMedicalCertificateAnswerResponderInterface;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerResponseType;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType;

/**
 * @author andreaskaltenbach
 */
public class ReceiveAnswerResponderImpl implements ReceiveMedicalCertificateAnswerResponderInterface {
    @Override
    public ReceiveMedicalCertificateAnswerResponseType receiveMedicalCertificateAnswer(AttributedURIType logicalAddress, ReceiveMedicalCertificateAnswerType parameters) {
        ReceiveMedicalCertificateAnswerResponseType response = new ReceiveMedicalCertificateAnswerResponseType();
        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }
}
