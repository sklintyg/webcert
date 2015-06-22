package se.inera.webcert.fkstub;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.webcert.fkstub.validation.SendMedicalCertificateQuestionValidator;
import se.inera.webcert.fkstub.validation.ValidationException;


/**
 * @author andreaskaltenbach
 */
public class SendQuestionStub implements
        SendMedicalCertificateQuestionResponderInterface {

    private static final String LOGICAL_ADDRESS = "SendQuestionStub";

    @Autowired
    private QuestionAnswerStore questionAnswerStore;

    @Override
    public SendMedicalCertificateQuestionResponseType sendMedicalCertificateQuestion(
            AttributedURIType logicalAddress,
            SendMedicalCertificateQuestionType parameters) {
        SendMedicalCertificateQuestionResponseType response = new SendMedicalCertificateQuestionResponseType();

        if (logicalAddress == null) {
            response.setResult(ResultOfCallUtil
                    .failResult("Ingen LogicalAddress är satt"));
        } else if (!LOGICAL_ADDRESS.equals(logicalAddress.getValue())) {
            response.setResult(ResultOfCallUtil.failResult("LogicalAddress '"
                    + logicalAddress.getValue() + "' är inte samma som '"
                    + LOGICAL_ADDRESS + "'"));
        } else if (parameters.getQuestion().getFraga().getMeddelandeText()
                .equalsIgnoreCase("error")) {
            response.setResult(ResultOfCallUtil
                    .failResult("Du ville ju få ett fel"));
        } else {
            QuestionToFkType questionType = parameters.getQuestion();
            SendMedicalCertificateQuestionValidator validator = new SendMedicalCertificateQuestionValidator(
                    questionType);
            try {
                validator.validateAndCorrect();
                response.setResult(ResultOfCallUtil.okResult());
            } catch (ValidationException e) {
                response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
            }
            questionAnswerStore.addQuestion(parameters.getQuestion());
        }

        return response;
    }
}
