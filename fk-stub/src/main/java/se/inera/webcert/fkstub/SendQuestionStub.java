package se.inera.webcert.fkstub;


import org.w3.wsaddressing10.AttributedURIType;
import se.inera.webcert.sendmedicalcertificatequestion.v1.rivtabp20.SendMedicalCertificateQuestionResponderInterface;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionType;

/**
 * @author andreaskaltenbach
 */
public class SendQuestionStub implements SendMedicalCertificateQuestionResponderInterface {

    @Override
    public SendMedicalCertificateQuestionResponseType sendMedicalCertificateQuestion(AttributedURIType logicalAddress, SendMedicalCertificateQuestionType parameters) {
        SendMedicalCertificateQuestionResponseType response = new SendMedicalCertificateQuestionResponseType();
        return response;
    }
}
