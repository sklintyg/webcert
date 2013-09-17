package se.inera.webcert.integration;

import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.webcert.receivemedicalcertificatequestion.v1.rivtabp20.ReceiveMedicalCertificateQuestionResponderInterface;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionResponseType;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;

/**
 * @author andreaskaltenbach
 */
public class ReceiveQuestionResponderImpl implements ReceiveMedicalCertificateQuestionResponderInterface {
    @Override
    public ReceiveMedicalCertificateQuestionResponseType receiveMedicalCertificateQuestion(AttributedURIType logicalAddress, ReceiveMedicalCertificateQuestionType parameters) {
        ReceiveMedicalCertificateQuestionResponseType response = new ReceiveMedicalCertificateQuestionResponseType();
        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }
}
