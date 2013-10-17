package se.inera.webcert.fkstub;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.webcert.fkstub.util.ResultOfCallUtil;
import se.inera.webcert.sendmedicalcertificateanswer.v1.rivtabp20.SendMedicalCertificateAnswerResponderInterface;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;

/**
 * @author andreaskaltenbach
 */
public class SendAnswerStub implements SendMedicalCertificateAnswerResponderInterface {

    @Autowired
    private QuestionAnswerStore questionAnswerStore;

    @Override
    public SendMedicalCertificateAnswerResponseType sendMedicalCertificateAnswer(AttributedURIType logicalAddress,
            SendMedicalCertificateAnswerType parameters) {
        SendMedicalCertificateAnswerResponseType response = new SendMedicalCertificateAnswerResponseType();

        if (parameters.getAnswer().getSvar().getMeddelandeText().equalsIgnoreCase("error")) {
            response.setResult(ResultOfCallUtil.failResult("Du ville ju få ett fel"));
        } else {
            response.setResult(ResultOfCallUtil.okResult());
            questionAnswerStore.addAnswer(parameters.getAnswer());
        }

        return response;
    }
}
