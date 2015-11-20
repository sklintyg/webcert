package se.inera.intyg.webcert.web.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;

import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.intyg.webcert.web.converter.FragaSvarConverter;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.mail.MailNotificationService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

@RunWith(MockitoJUnitRunner.class)
public class ReceiveQuestionResponderImplTest {

    private static final String INTEGRERAD_ENHET = "SE4815162344-1A02";

    private static final String EJ_INTEGRERAD_ENHET = "SE4815162344-1A03";

    @Mock
    private MailNotificationService mockMailNotificationService;

    @Spy
    private FragaSvarConverter converter = new FragaSvarConverter();

    @Mock
    private FragaSvarService mockFragaSvarService;

    @Mock
    private NotificationService mockNotificationService;

    @Mock
    private IntegreradeEnheterRegistry mockIntegreradeEnheterRegistry;

    @InjectMocks
    private ReceiveQuestionResponderImpl receiveQuestionResponder;

    @Before
    public void integreradeEnheterExpectations() {
        when(mockIntegreradeEnheterRegistry.isEnhetIntegrerad(INTEGRERAD_ENHET)).thenReturn(Boolean.TRUE);
        when(mockIntegreradeEnheterRegistry.isEnhetIntegrerad(EJ_INTEGRERAD_ENHET)).thenReturn(Boolean.FALSE);
    }

    @Before
    public void processIncomingExpectation() {
        // return the fragasvar supplied
        when(mockFragaSvarService.processIncomingQuestion(any(FragaSvar.class))).thenAnswer(new Answer<FragaSvar>() {
            @Override
            public FragaSvar answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                FragaSvar fragaSvar = (FragaSvar) args[0];
                fragaSvar.setInternReferens(1L);
                return fragaSvar;
            }
        });
    }

    @Test
    public void testReceiveWithIntegratedUnit() {

        ReceiveMedicalCertificateQuestionType request = createRequest("RecieveQuestionAnswerResponders/question-from-fk-integrated.xml");
        ReceiveMedicalCertificateQuestionResponseType response = receiveQuestionResponder.receiveMedicalCertificateQuestion(null, request);

        // should place notification on queue
        verify(mockNotificationService).sendNotificationForQuestionReceived(any(FragaSvar.class));

        assertNotNull(response);
        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
    }

    @Test
    public void testReceive() {

        ReceiveMedicalCertificateQuestionType request = createRequest("RecieveQuestionAnswerResponders/question-from-fk.xml");
        ReceiveMedicalCertificateQuestionResponseType response = receiveQuestionResponder.receiveMedicalCertificateQuestion(null, request);

        // should mail notification
        verify(mockMailNotificationService).sendMailForIncomingQuestion(any(FragaSvar.class));

        assertNotNull(response);
        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
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

}
