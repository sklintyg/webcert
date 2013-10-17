package se.inera.webcert.fkstub;


import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.webcert.fkstub.util.ResultOfCallUtil;
import se.inera.webcert.sendmedicalcertificatequestion.v1.rivtabp20.SendMedicalCertificateQuestionResponderInterface;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionType;

/**
 * @author andreaskaltenbach
 */
public class SendQuestionStub implements SendMedicalCertificateQuestionResponderInterface {

    @Autowired
    private QuestionAnswerStore questionAnswerStore;

    @Override
    public SendMedicalCertificateQuestionResponseType sendMedicalCertificateQuestion(AttributedURIType logicalAddress, SendMedicalCertificateQuestionType parameters) {
        SendMedicalCertificateQuestionResponseType response = new SendMedicalCertificateQuestionResponseType();
        if (parameters.getQuestion().getFraga().getMeddelandeText().equalsIgnoreCase("error")) {
            response.setResult(ResultOfCallUtil.failResult("Du ville ju få ett fel"));
        } else {
            response.setResult(ResultOfCallUtil.okResult());
            questionAnswerStore.addQuestion(parameters.getQuestion());
        }

        
        return response;
    }

}
