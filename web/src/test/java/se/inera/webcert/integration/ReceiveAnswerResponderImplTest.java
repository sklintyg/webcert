package se.inera.webcert.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;

import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.webcert.integration.registry.IntegreradeEnheterRegistry;
import se.inera.webcert.notifications.message.v1.HandelseType;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.Id;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.AnswerFromFkType;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerResponseType;
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType;
import se.inera.webcert.service.fragasvar.FragaSvarService;
import se.inera.webcert.service.mail.MailNotificationService;
import se.inera.webcert.service.notification.NotificationService;

@RunWith(MockitoJUnitRunner.class)
public class ReceiveAnswerResponderImplTest {

    private static final String INTEGRERAD_ENHET = "SE4815162344-1A02";

    private static final String EJ_INTEGRERAD_ENHET = "SE4815162344-1A03";

    private static final Long QUESTION_ID = 1234L;

    @Mock
    private MailNotificationService mockMailNotificationService;

    @Mock
    private FragaSvarService mockFragaSvarService;

    @Mock
    private NotificationService mockNotificationService;

    @Mock
    private IntegreradeEnheterRegistry mockIntegreradeEnheterRegistry;

    @InjectMocks
    private ReceiveAnswerResponderImpl receiveAnswerResponder;

    @Before
    public void integreradeEnheterExpectations() {
        when(mockIntegreradeEnheterRegistry.isEnhetIntegrerad(INTEGRERAD_ENHET)).thenReturn(Boolean.TRUE);
        when(mockIntegreradeEnheterRegistry.isEnhetIntegrerad(EJ_INTEGRERAD_ENHET)).thenReturn(Boolean.FALSE);
    }

    @Test
    public void testReceiveAnswer() {

        ArgumentCaptor<NotificationRequestType> notCaptor = ArgumentCaptor.forClass(NotificationRequestType.class);

        FragaSvar fragaSvar = buildFraga(QUESTION_ID, "mahana", Amne.ARBETSTIDSFORLAGGNING, LocalDateTime.now());
        when(mockFragaSvarService.processIncomingAnswer(anyLong(), anyString(), any(LocalDateTime.class))).thenReturn(fragaSvar);

        ReceiveMedicalCertificateAnswerType request = createRequest("RecieveQuestionAnswerResponders/answer-from-fk.xml");
        ReceiveMedicalCertificateAnswerResponseType response = receiveAnswerResponder.receiveMedicalCertificateAnswer(null, request);

        // should place notification on queue
        verify(mockNotificationService).notify(notCaptor.capture());

        assertNotNull(response);
        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
        assertEquals(HandelseType.SVAR_FRAN_FK, notCaptor.getValue().getHandelse());
    }

    private ReceiveMedicalCertificateAnswerType createRequest(String answerFile) {
        ReceiveMedicalCertificateAnswerType request = new ReceiveMedicalCertificateAnswerType();
        AnswerFromFkType answer = inflateAnswer(answerFile);
        request.setAnswer(answer);
        return request;
    }

    private AnswerFromFkType inflateAnswer(String filePath) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(AnswerFromFkType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            AnswerFromFkType answer = unmarshaller
                    .unmarshal(new StreamSource(new ClassPathResource(filePath).getInputStream()),
                            AnswerFromFkType.class).getValue();
            return answer;
        } catch (Exception e) {
            return null;
        }
    }

    private FragaSvar buildFraga(Long id, String frageText, Amne amne, LocalDateTime fragaSkickadDatum) {
        FragaSvar f = new FragaSvar();
        f.setStatus(Status.PENDING_INTERNAL_ACTION);
        f.setAmne(amne);
        f.setExternReferens("<fk-extern-referens>");
        f.setInternReferens(id);
        f.setFrageSkickadDatum(fragaSkickadDatum);
        f.setFrageText(frageText);

        IntygsReferens intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsId("<intygsId>");
        intygsReferens.setIntygsTyp("fk7263");
        intygsReferens.setPatientId(new Id("patiend-id-root", "19121212-1212"));
        f.setIntygsReferens(intygsReferens);
        f.setKompletteringar(new HashSet<Komplettering>());
        f.setVardperson(new Vardperson());
        f.getVardperson().setEnhetsId("SE4815162344-1A02");
        f.getVardperson().setEnhetsnamn("WebCert-Integration Enhet 1");
        return f;
    }

}
