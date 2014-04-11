package se.inera.webcert.fkstub;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.webcert.medcertqa.v1.InnehallType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.QuestionToFkType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionType;

@RunWith(MockitoJUnitRunner.class)
public class SendQuestionStubTest {

    private static final String SEND_QUESTION_STUB_ADDRESS = "SendQuestionStub";

    @Mock
    private QuestionAnswerStore store;

    @InjectMocks
    private SendQuestionStub stub = new SendQuestionStub();

    @Test
    public void answerRequestWithoutAddressIsRejected() {
        SendMedicalCertificateQuestionResponseType answer = stub.sendMedicalCertificateQuestion(null, null);
        assertEquals(ResultCodeEnum.ERROR, answer.getResult().getResultCode());
    }

    @Test
    public void answerRequestWrongAddressIsRejected() {
        AttributedURIType address = new AttributedURIType();
        address.setValue("WrongAddress");
        SendMedicalCertificateQuestionResponseType answer = stub.sendMedicalCertificateQuestion(address, null);
        assertEquals(ResultCodeEnum.ERROR, answer.getResult().getResultCode());
    }

    @Test
    public void answerIsAccepted() {
        AttributedURIType address = new AttributedURIType();
        address.setValue(SEND_QUESTION_STUB_ADDRESS);
        SendMedicalCertificateQuestionType parameters = createQuestion("Message");
        SendMedicalCertificateQuestionResponseType answer = stub.sendMedicalCertificateQuestion(address, parameters);
        assertEquals(ResultCodeEnum.OK, answer.getResult().getResultCode());
    }

    @Test
    public void answerWithMessageErrorGeneratesError() {
        AttributedURIType address = new AttributedURIType();
        address.setValue(SEND_QUESTION_STUB_ADDRESS);
        SendMedicalCertificateQuestionType parameters = createQuestion("Error");
        SendMedicalCertificateQuestionResponseType answer = stub.sendMedicalCertificateQuestion(address, parameters);
        assertEquals(ResultCodeEnum.ERROR, answer.getResult().getResultCode());
    }

    private SendMedicalCertificateQuestionType createQuestion(String message) {
        SendMedicalCertificateQuestionType parameters = new SendMedicalCertificateQuestionType();
        QuestionToFkType questionType = new QuestionToFkType();
        InnehallType svar = new InnehallType();
        svar.setMeddelandeText(message);
        questionType.setFraga(svar);
        parameters.setQuestion(questionType);
        return parameters;
    }
}
