package se.inera.intyg.webcert.web.service.mail;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

import javax.mail.Address;
import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.infrastructure.directory.privatepractitioner.v1.EnhetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;

public class MailNotificationServiceImplTest {

    @InjectMocks
    private MailNotificationServiceImpl mailNotificationService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private HSAWebServiceCalls hsaClient;

    @Mock
    private MonitoringLogService monitoringService;

    @Mock
    private PPService ppService;

    @Mock
    private WebCertUserService webCertUserService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(mailNotificationService, "adminMailAddress", "AdminMail");
        ReflectionTestUtils.setField(mailNotificationService, "fromAddress", "FromAddress");
        ReflectionTestUtils.setField(mailNotificationService, "webCertHostUrl", "WebCertHostUrl");
        ReflectionTestUtils.setField(mailNotificationService, "ppLogicalAddress", "PpLogicalAddress");
        MimeMessage mimeMessage = new MimeMessage(Mockito.mock(MimeMessage.class));
        Mockito.doReturn(mimeMessage).when(mailSender).createMimeMessage();
    }

    @Captor
    private ArgumentCaptor<MimeMessage> mimeCaptor;

    @Test
    public void testSendMailForIncomingQuestionHsaIsCalledIfNotPrivatePractitioner() throws Exception {
        //Given
        FragaSvar fragaSvar = new FragaSvar();
        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId("ThisIsNotPp" + MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");
        fragaSvar.setVardperson(vardperson);

        //When
        try {
            mailNotificationService.sendMailForIncomingQuestion(fragaSvar);
        } catch (IllegalArgumentException e) {
            //Expected
        }

        //Then
        Mockito.verify(hsaClient, times(1)).callGetHsaunit(anyString());
    }

    @Test
    public void testSendMailForIncomingQuestionHsaIsNotCalledIfPrivatePractitioner() throws Exception {
        //Given
        HoSPersonType hoSPersonType = new HoSPersonType();
        EnhetType enhet = new EnhetType();
        enhet.setEpost("test@test.se");
        enhet.setEnhetsnamn("TestEnhet");
        hoSPersonType.setEnhet(enhet);
        Mockito.doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), anyString(), anyString());

        FragaSvar fragaSvar = new FragaSvar();
        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId(MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");
        fragaSvar.setVardperson(vardperson);
        fragaSvar.setIntygsReferens(new IntygsReferens());

        //When
        mailNotificationService.sendMailForIncomingQuestion(fragaSvar);

        //Then
        Mockito.verify(hsaClient, times(0)).callGetHsaunit(anyString());
    }

    @Test
    public void testSendMailForIncomingQuestionMailIsSentToPrivatePractitioner() throws Exception {
        //Given
        HoSPersonType hoSPersonType = new HoSPersonType();
        EnhetType enhet = new EnhetType();
        String epost = "test@test.se";
        enhet.setEpost(epost);
        enhet.setEnhetsnamn("TestEnhet");
        hoSPersonType.setEnhet(enhet);
        Mockito.doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), anyString(), anyString());

        FragaSvar fragaSvar = new FragaSvar();
        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId(MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");
        fragaSvar.setVardperson(vardperson);
        fragaSvar.setIntygsReferens(new IntygsReferens());

        //When
        mailNotificationService.sendMailForIncomingQuestion(fragaSvar);

        //Then
        Mockito.verify(mailSender, times(1)).send(mimeCaptor.capture());
        MimeMessage mimeMessage = mimeCaptor.getValue();
        Address[] allRecipients = mimeMessage.getAllRecipients();
        assertEquals(1, allRecipients.length);
        assertEquals(epost, allRecipients[0].toString());
    }

    @Test
    public void testSendMailForIncomingAnswerHsaIsCalledIfNotPrivatePractitioner() throws Exception {
        //Given
        FragaSvar fragaSvar = new FragaSvar();
        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId("ThisIsNotPp" + MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");
        fragaSvar.setVardperson(vardperson);

        //When
        try {
            mailNotificationService.sendMailForIncomingAnswer(fragaSvar);
        } catch (IllegalArgumentException e) {
            //Expected
        }

        //Then
        Mockito.verify(hsaClient, times(1)).callGetHsaunit(anyString());
    }

    @Test
    public void testSendMailForIncomingAnswerHsaIsNotCalledIfPrivatePractitioner() throws Exception {
        //Given
        HoSPersonType hoSPersonType = new HoSPersonType();
        EnhetType enhet = new EnhetType();
        enhet.setEpost("test@test.se");
        enhet.setEnhetsnamn("TestEnhet");
        hoSPersonType.setEnhet(enhet);
        Mockito.doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), anyString(), anyString());

        FragaSvar fragaSvar = new FragaSvar();
        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId(MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");
        fragaSvar.setVardperson(vardperson);
        fragaSvar.setIntygsReferens(new IntygsReferens());

        //When
        mailNotificationService.sendMailForIncomingAnswer(fragaSvar);

        //Then
        Mockito.verify(hsaClient, times(0)).callGetHsaunit(anyString());
    }

    @Test
    public void testSendMailForIncomingAnswerMailIsSentToPrivatePractitioner() throws Exception {
        //Given
        HoSPersonType hoSPersonType = new HoSPersonType();
        EnhetType enhet = new EnhetType();
        String epost = "test@test.se";
        enhet.setEpost(epost);
        enhet.setEnhetsnamn("TestEnhet");
        hoSPersonType.setEnhet(enhet);
        Mockito.doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), anyString(), anyString());

        FragaSvar fragaSvar = new FragaSvar();
        Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId(MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");
        fragaSvar.setVardperson(vardperson);
        fragaSvar.setIntygsReferens(new IntygsReferens());

        //When
        mailNotificationService.sendMailForIncomingAnswer(fragaSvar);

        //Then
        Mockito.verify(mailSender, times(1)).send(mimeCaptor.capture());
        MimeMessage mimeMessage = mimeCaptor.getValue();
        Address[] allRecipients = mimeMessage.getAllRecipients();
        assertEquals(1, allRecipients.length);
        assertEquals(epost, allRecipients[0].toString());
    }

    @Test
    public void testIntygsUrlUthopp() throws Exception {
        //Given
        final WebCertUser user = Mockito.mock(WebCertUser.class);
        Mockito.when(user.isRoleUthopp()).thenReturn(true);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);
        final FragaSvar fragaSvar = new FragaSvar();
        fragaSvar.setVardperson(new Vardperson());
        fragaSvar.setIntygsReferens(new IntygsReferens());

        //When
        final String url = mailNotificationService.intygsUrl(fragaSvar);

        //Then
        assertEquals("WebCertHostUrl/webcert/web/user/certificate/null/questions", url);
    }

    @Test
    public void testIntygsUrlLandsting() throws Exception {
        //Given
        final WebCertUser user = Mockito.mock(WebCertUser.class);
        Mockito.when(user.isRoleUthopp()).thenReturn(false);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);
        final FragaSvar fragaSvar = new FragaSvar();
        fragaSvar.setVardperson(new Vardperson());
        fragaSvar.setIntygsReferens(new IntygsReferens());

        //When
        final String url = mailNotificationService.intygsUrl(fragaSvar);

        //Then
        assertEquals("WebCertHostUrl/webcert/web/user/basic-certificate/null/questions", url);
    }

    @Test
    public void testIntygsUrlPp() throws Exception {
        //Given
        final WebCertUser user = Mockito.mock(WebCertUser.class);
        Mockito.when(user.isRoleUthopp()).thenReturn(false);
        Mockito.when(webCertUserService.getUser()).thenReturn(user);
        final FragaSvar fragaSvar = new FragaSvar();
        final Vardperson vardperson = new Vardperson();
        vardperson.setEnhetsId(MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "AndSomeOtherText");
        fragaSvar.setVardperson(vardperson);
        fragaSvar.setIntygsReferens(new IntygsReferens());

        //When
        final String url = mailNotificationService.intygsUrl(fragaSvar);

        //Then
        assertEquals("WebCertHostUrl/webcert/web/user/pp-certificate/null/questions", url);
    }

}
