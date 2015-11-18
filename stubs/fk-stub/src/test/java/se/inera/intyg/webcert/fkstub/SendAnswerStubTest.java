package se.inera.webcert.fkstub;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.Amnetyp;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.InnehallType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.VardAdresseringsType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType;
import iso.v21090.dt.v1.II;


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
        assertEquals(answer.getResult().getErrorText(), ResultCodeEnum.OK, answer.getResult().getResultCode());
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
        answerType.setFkReferensId("fkReferensId");
        answerType.setAmne(Amnetyp.OVRIGT);
        InnehallType fraga = new InnehallType();
        fraga.setMeddelandeText("fraga");
        fraga.setSigneringsTidpunkt(LocalDateTime.now());
        answerType.setFraga(fraga);
        InnehallType svar = new InnehallType();
        svar.setMeddelandeText(message);
        svar.setSigneringsTidpunkt(LocalDateTime.now());
        answerType.setSvar(svar);
        answerType.setVardReferensId("vardRef");
        answerType.setAvsantTidpunkt(LocalDateTime.now());
        LakarutlatandeEnkelType lakarutlatande = new LakarutlatandeEnkelType();
        lakarutlatande.setLakarutlatandeId("id");
        lakarutlatande.setSigneringsTidpunkt(LocalDateTime.now());
        PatientType patient = new PatientType();
        II id = new II();
        id.setRoot("1.2.752.129.2.1.3.1");
        id.setExtension("19121212-1212");
        patient.setPersonId(id);
        patient.setFullstandigtNamn("namn");
        lakarutlatande.setPatient(patient);
        answerType.setLakarutlatande(lakarutlatande);
        VardAdresseringsType vardAdress = new VardAdresseringsType();
        HosPersonalType hosPersonal = new HosPersonalType();
        II hosId = new II();
        hosId.setRoot("1.2.752.129.2.1.4.1");
        hosId.setExtension("hosId");
        hosPersonal.setPersonalId(hosId);
        hosPersonal.setFullstandigtNamn("hosPersonal");
        EnhetType enhet = new EnhetType();
        II enhetsId = new II();
        enhetsId.setRoot("1.2.752.129.2.1.4.1");
        enhetsId.setExtension("enhetsId");
        enhet.setEnhetsId(enhetsId);
        enhet.setEnhetsnamn("enhetsnamn");
        VardgivareType vardgivare = new VardgivareType();
        II vardgivarId = new II();
        vardgivarId.setRoot("1.2.752.129.2.1.4.1");
        vardgivarId.setExtension("vardgivarId");
        vardgivare.setVardgivareId(vardgivarId);
        vardgivare.setVardgivarnamn("vardgivarnamn");
        enhet.setVardgivare(vardgivare);
        hosPersonal.setEnhet(enhet);
        vardAdress.setHosPersonal(hosPersonal);
        answerType.setAdressVard(vardAdress);
        parameters.setAnswer(answerType);
        return parameters;
    }
}
