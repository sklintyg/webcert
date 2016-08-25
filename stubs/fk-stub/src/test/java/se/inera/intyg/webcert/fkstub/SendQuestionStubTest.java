/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.fkstub;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;

import iso.v21090.dt.v1.II;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.*;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.*;
import se.inera.ifv.insuranceprocess.healthreporting.v2.*;

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
        assertEquals(answer.getResult().getErrorText(), ResultCodeEnum.OK, answer.getResult().getResultCode());
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
        questionType.setAmne(Amnetyp.OVRIGT);
        InnehallType svar = new InnehallType();
        svar.setMeddelandeText(message);
        svar.setSigneringsTidpunkt(LocalDateTime.now());
        questionType.setFraga(svar);
        questionType.setVardReferensId("vardRef");
        questionType.setAvsantTidpunkt(LocalDateTime.now());
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
        questionType.setLakarutlatande(lakarutlatande);
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
        questionType.setAdressVard(vardAdress);
        parameters.setQuestion(questionType);
        return parameters;
    }
}
