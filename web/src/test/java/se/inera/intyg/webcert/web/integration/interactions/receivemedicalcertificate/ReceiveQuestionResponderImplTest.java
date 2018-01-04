/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.interactions.receivemedicalcertificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.converter.FragaSvarConverter;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

@RunWith(MockitoJUnitRunner.class)
public class ReceiveQuestionResponderImplTest {

    private static final Long QUESTION_ID = 1234L;

    private static final String INTEGRERAD_ENHET = "SE4815162344-1A02";

    private static final Personnummer PATIENT_ID = new Personnummer("19121212-1212");

    @Spy
    private FragaSvarConverter converter = new FragaSvarConverter();

    @Mock
    private FragaSvarService mockFragaSvarService;

    @Mock
    private NotificationService mockNotificationService;

    @InjectMocks
    private ReceiveQuestionResponderImpl receiveQuestionResponder;

    @Test
    public void testReceiveQuestionOK() {
        FragaSvar fraga = buildFraga(INTEGRERAD_ENHET, Status.PENDING_INTERNAL_ACTION);
        when(mockFragaSvarService.processIncomingQuestion(any(FragaSvar.class))).thenReturn(fraga);

        ReceiveMedicalCertificateQuestionType request = createRequest("RecieveQuestionAnswerResponders/question-from-fk-integrated.xml");
        ReceiveMedicalCertificateQuestionResponseType response = receiveQuestionResponder.receiveMedicalCertificateQuestion(null, request);

        // should place notification on queue
        verify(mockNotificationService).sendNotificationForQuestionReceived(any(FragaSvar.class));

        assertNotNull(response);
        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
    }

    @Test
    public void testReceiveQuestionValidationError() {
        ReceiveMedicalCertificateQuestionType request = createRequest("RecieveQuestionAnswerResponders/question-from-fk-integrated.xml");
        request.getQuestion().setAmne(null); // invalid
        ReceiveMedicalCertificateQuestionResponseType response = receiveQuestionResponder.receiveMedicalCertificateQuestion(null, request);

        verifyZeroInteractions(mockNotificationService);
        verifyZeroInteractions(mockFragaSvarService);

        assertNotNull(response);
        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Amne är felaktigt", response.getResult().getErrorText());
    }

    private ReceiveMedicalCertificateQuestionType createRequest(String questionFile) {
        ReceiveMedicalCertificateQuestionType request = new ReceiveMedicalCertificateQuestionType();
        QuestionFromFkType question = inflateQuestion(questionFile);

        if (question == null) {
            throw new RuntimeException("Could not inflate file");
        }

        request.setQuestion(question);
        return request;
    }

    private QuestionFromFkType inflateQuestion(String filePath) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(QuestionFromFkType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            QuestionFromFkType question = unmarshaller
                    .unmarshal(new StreamSource(new ClassPathResource(filePath).getInputStream()),
                            QuestionFromFkType.class)
                    .getValue();
            return question;
        } catch (Exception e) {
            return null;
        }
    }

    private FragaSvar buildFraga(String vardpersonEnhetsId, Status status) {
        FragaSvar f = new FragaSvar();
        f.setStatus(status);
        f.setAmne(Amne.OVRIGT);
        f.setExternReferens("<fk-extern-referens>");
        f.setInternReferens(QUESTION_ID);
        f.setFrageSkickadDatum(LocalDateTime.now());
        f.setSvarSigneringsDatum(LocalDateTime.now());
        f.setSvarsText("Ett svar");

        IntygsReferens intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsId("<intygsId>");
        intygsReferens.setIntygsTyp("fk7263");
        intygsReferens.setPatientId(PATIENT_ID);
        f.setIntygsReferens(intygsReferens);
        f.setKompletteringar(new HashSet<Komplettering>());
        f.setVardperson(new Vardperson());
        f.getVardperson().setEnhetsId(vardpersonEnhetsId);
        f.getVardperson().setEnhetsnamn("WebCert-Integration Enhet 1");
        return f;
    }

}
