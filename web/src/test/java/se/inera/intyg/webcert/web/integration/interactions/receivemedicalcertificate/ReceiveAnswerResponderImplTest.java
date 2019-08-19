/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import javax.xml.bind.JAXBElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.AnswerFromFkType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

@RunWith(MockitoJUnitRunner.class)
public class ReceiveAnswerResponderImplTest {

    private static final Long QUESTION_ID = 1234L;

    private static final String INTEGRERAD_ENHET = "SE4815162344-1A02";

    private static final Personnummer PATIENT_ID = Personnummer.createPersonnummer("19121212-1212").get();

    @Mock
    private FragaSvarService mockFragaSvarService;

    @Mock
    private NotificationService mockNotificationService;

    @InjectMocks
    private ReceiveAnswerResponderImpl receiveAnswerResponder;

    @Test
    public void testReceiveAnswerOK() throws Exception {
        FragaSvar fragaSvar = buildFraga(QUESTION_ID, "That is the question", Amne.ARBETSTIDSFORLAGGNING, LocalDateTime.now(),
            INTEGRERAD_ENHET, Status.PENDING_INTERNAL_ACTION);

        when(mockFragaSvarService.processIncomingAnswer(anyLong(), anyString(), any(LocalDateTime.class))).thenReturn(fragaSvar);

        ReceiveMedicalCertificateAnswerType request = createRequest("RecieveQuestionAnswerResponders/answer-from-fk-integrated.xml");
        ReceiveMedicalCertificateAnswerResponseType response = receiveAnswerResponder.receiveMedicalCertificateAnswer(null, request);

        // should place notification on queue
        verify(mockNotificationService).sendNotificationForAnswerRecieved(any(FragaSvar.class));

        assertNotNull(response);
        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
    }

    @Test
    public void testReceiveAnswerValidationError() throws Exception {
        ReceiveMedicalCertificateAnswerType request = createRequest("RecieveQuestionAnswerResponders/answer-from-fk-integrated.xml");
        request.getAnswer().setSvar(null); // invalid
        ReceiveMedicalCertificateAnswerResponseType response = receiveAnswerResponder.receiveMedicalCertificateAnswer(null, request);

        verifyZeroInteractions(mockNotificationService);
        verifyZeroInteractions(mockFragaSvarService);

        assertNotNull(response);
        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Missing svar element.", response.getResult().getErrorText());
    }

    private ReceiveMedicalCertificateAnswerType createRequest(String answerFile) throws IOException {
        ClassPathResource resource = new ClassPathResource(answerFile);
        JAXBElement<AnswerFromFkType> jaxbElement = XmlMarshallerHelper.unmarshal(resource.getInputStream());

        ReceiveMedicalCertificateAnswerType request = new ReceiveMedicalCertificateAnswerType();
        AnswerFromFkType answer = jaxbElement.getValue();

        if (answer == null) {
            throw new RuntimeException("Could not unmarshal file");
        }

        request.setAnswer(answer);
        return request;
    }

    private FragaSvar buildFraga(Long id, String frageText, Amne amne, LocalDateTime fragaSkickadDatum, String vardpersonEnhetsId, Status status) {
        FragaSvar f = new FragaSvar();
        f.setStatus(status);
        f.setAmne(amne);
        f.setExternReferens("<fk-extern-referens>");
        f.setInternReferens(id);
        f.setFrageSkickadDatum(fragaSkickadDatum);
        f.setFrageText(frageText);

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
