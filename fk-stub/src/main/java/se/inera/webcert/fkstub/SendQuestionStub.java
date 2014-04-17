package se.inera.webcert.fkstub;


import static se.inera.certificate.integration.util.ResultOfCallUtil.failResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.webcert.integration.validator.SendMedicalCertificateQuestionValidator;
import se.inera.webcert.integration.validator.ValidationException;
import se.inera.webcert.sendmedicalcertificatequestion.v1.rivtabp20.SendMedicalCertificateQuestionResponderInterface;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.QuestionToFkType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionType;

/**
 * @author andreaskaltenbach
 */
public class SendQuestionStub implements SendMedicalCertificateQuestionResponderInterface {

    private static final String LOGICAL_ADDRESS = "SendQuestionStub";

    @Autowired
    private QuestionAnswerStore questionAnswerStore;

    @Override
    public SendMedicalCertificateQuestionResponseType sendMedicalCertificateQuestion(AttributedURIType logicalAddress, SendMedicalCertificateQuestionType parameters) {
        SendMedicalCertificateQuestionResponseType response = new SendMedicalCertificateQuestionResponseType();

        if (logicalAddress == null) {
            response.setResult(ResultOfCallUtil.failResult("Ingen LogicalAddress är satt"));
        } else if (!LOGICAL_ADDRESS.equals(logicalAddress.getValue())) {
            response.setResult(ResultOfCallUtil.failResult("LogicalAddress '" + logicalAddress.getValue() + "' är inte samma som '" + LOGICAL_ADDRESS + "'"));
        } else if (parameters.getQuestion().getFraga().getMeddelandeText().equalsIgnoreCase("error")) {
            response.setResult(ResultOfCallUtil.failResult("Du ville ju få ett fel"));
        } else {
            QuestionToFkType questionType = parameters.getQuestion();
            SendMedicalCertificateQuestionValidator validator = new SendMedicalCertificateQuestionValidator(questionType);
            try {
                validator.validateAndCorrect();
                response.setResult(ResultOfCallUtil.okResult());
            } catch (ValidationException e) {
                response.setResult(failResult(e.getMessage()));
            }
            questionAnswerStore.addQuestion(parameters.getQuestion());
        }
        
        return response;
    }

}
