package se.inera.webcert.fkstub;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.webcert.medcertqa.v1.InnehallType;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SendAnswerStubTest {

    public static final String SEND_ANSWER_STUB_ADDRESS = "SendAnswerStub";

    @Mock
    private QuestionAnswerStore store;

    @InjectMocks
    private SendAnswerStub stub = new SendAnswerStub();

    @Test
    public void answerRequestWithoutAddressIsRejected() {
        SendMedicalCertificateAnswerResponseType answer = stub.sendMedicalCertificateAnswer(null, null);
        assertEquals(ResultCodeEnum.ERROR, answer.getResult().getResultCode());
    }

    @Test
    public void answerRequestWrongAddressIsRejected() {
        AttributedURIType address = new AttributedURIType();
        address.setValue("WrongAddress");
        SendMedicalCertificateAnswerResponseType answer = stub.sendMedicalCertificateAnswer(address, null);
        assertEquals(ResultCodeEnum.ERROR, answer.getResult().getResultCode());
    }

    @Test
    public void answerIsAccepted() {
        AttributedURIType address = new AttributedURIType();
        address.setValue(SEND_ANSWER_STUB_ADDRESS);
        SendMedicalCertificateAnswerType parameters = createAnswer("Message");
        SendMedicalCertificateAnswerResponseType answer = stub.sendMedicalCertificateAnswer(address, parameters);
        assertEquals(ResultCodeEnum.OK, answer.getResult().getResultCode());
    }

    @Test
    public void answerWithMessageErrorGeneratesError() {
        AttributedURIType address = new AttributedURIType();
        address.setValue(SEND_ANSWER_STUB_ADDRESS);
        SendMedicalCertificateAnswerType parameters = createAnswer("Error");
        SendMedicalCertificateAnswerResponseType answer = stub.sendMedicalCertificateAnswer(address, parameters);
        assertEquals(ResultCodeEnum.ERROR, answer.getResult().getResultCode());
    }

    private SendMedicalCertificateAnswerType createAnswer(String message) {
        SendMedicalCertificateAnswerType parameters = new SendMedicalCertificateAnswerType();
        AnswerToFkType answerType = new AnswerToFkType();
        InnehallType svar = new InnehallType();
        svar.setMeddelandeText(message);
        answerType.setSvar(svar);
        parameters.setAnswer(answerType);
        return parameters;
    }
}
