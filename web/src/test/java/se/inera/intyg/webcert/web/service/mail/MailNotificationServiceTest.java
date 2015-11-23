package se.inera.intyg.webcert.web.service.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.stub.HsaServiceStub;
import se.inera.intyg.webcert.mailstub.MailStore;
import se.inera.intyg.webcert.mailstub.OutgoingMail;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * @author andreaskaltenbach
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:MailNotificationServiceTest/test-context.xml")
public class MailNotificationServiceTest {

    private static final int THRESHOLD = 2000;

    private static final String CITYAKUTEN = "cityakuten";

    private static final String LANDCENTRALEN = "landcentralen";
    private static final String VANSTERAKUTEN = "vansterakuten";
    private static final String KUSTAKUTEN = "kustakuten";

    @Autowired
    private MailNotificationService mailNotificationService;

    @Autowired
    private MailStore mailStore;

    @Autowired
    private HsaServiceStub hsaStub;

    @Autowired
    private WebCertUserService webCertUserServiceMock;

    @PostConstruct
    public void setupTestlandVardgivare() throws IOException {
        Vardgivare vardgivare = new CustomObjectMapper().readValue(new ClassPathResource(
                "MailNotificationServiceTest/landstinget-testland.json").getFile(), Vardgivare.class);
        hsaStub.getVardgivare().add(vardgivare);
        final WebCertUser user = Mockito.mock(WebCertUser.class);
        Mockito.when(user.isRoleUthopp()).thenReturn(true);
        Mockito.when(webCertUserServiceMock.getUser()).thenReturn(user);
    }

    @Test
    public void testMailDeliveryForIncomingQuestion() throws Exception {

        mailNotificationService.sendMailForIncomingQuestion(fragaSvar(CITYAKUTEN));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("cityakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Inkommen fråga från Försäkringskassan", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-cityakuten.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswer() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(fragaSvar(CITYAKUTEN));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("cityakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har svarat på en fråga", mail.getSubject());
        assertEquals(expectationFromFile("incoming-answer-body-cityakuten.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingQuestionToMottagning() throws Exception {

        mailNotificationService.sendMailForIncomingQuestion(fragaSvar(VANSTERAKUTEN));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("vansterakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Inkommen fråga från Försäkringskassan", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-vansterakuten.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswerToMottagning() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(fragaSvar(VANSTERAKUTEN));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("vansterakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har svarat på en fråga", mail.getSubject());
        assertEquals(expectationFromFile("incoming-answer-body-vansterakuten.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingQuestionForEnhetWithoutMail() throws Exception {

        mailNotificationService.sendMailForIncomingQuestion(fragaSvar(LANDCENTRALEN));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("admin@sverige.se", mail.getRecipients().get(0));
        assertEquals("Fråga/svar Webcert: Enhet utan mailadress eller koppling", mail.getSubject());
        assertEquals(expectationFromFile("admin-body.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswerForEnhetWithoutMail() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(fragaSvar(LANDCENTRALEN));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("admin@sverige.se", mail.getRecipients().get(0));
        assertEquals("Fråga/svar Webcert: Enhet utan mailadress eller koppling", mail.getSubject());
        assertEquals(expectationFromFile("admin-body.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingQuestionToMottagningWithoutMailButParentEnhetWithMail() throws Exception {

        mailNotificationService.sendMailForIncomingQuestion(fragaSvar(KUSTAKUTEN));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("sjocentralen@testland.se", mail.getRecipients().get(0));
        assertEquals("Inkommen fråga från Försäkringskassan", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-kustakuten.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswerToMottagningWithoutMailButParentEnhetWithMail() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(fragaSvar(KUSTAKUTEN));

        mailStore.waitForMails(1);
        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("sjocentralen@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har svarat på en fråga", mail.getSubject());
        assertEquals(expectationFromFile("incoming-answer-body-kustakuten.html"), mail.getBody());
    }

    @Test
    public void testAsyncMailDeliveryForIncomingQuestion() throws Exception {

        long startTimestamp = System.currentTimeMillis();
        mailStore.setWait(true);
        mailNotificationService.sendMailForIncomingQuestion(fragaSvar(CITYAKUTEN));
        mailStore.setWait(false);
        mailStore.waitForMails(1);
        long endTimestamp = System.currentTimeMillis();
        assertTrue((endTimestamp - startTimestamp) < THRESHOLD);
        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("cityakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Inkommen fråga från Försäkringskassan", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-cityakuten.html"), mail.getBody());
    }

    @Test
    public void linkHasCorrectId() {
        String url = mailNotificationService.intygsUrl(fragaSvar("enhet"));

        assertEquals("https://www.webcert.se/webcert/web/user/certificate/1/questions", url);
    }

    @After
    public void cleanMailStore() {
        mailStore.getMails().clear();
    }

    private FragaSvar fragaSvar(String enhetsId) {
        FragaSvar fragaSvar = new FragaSvar();
        fragaSvar.setVardperson(new Vardperson());
        fragaSvar.getVardperson().setEnhetsId(enhetsId);
        fragaSvar.setInternReferens(2L);
        fragaSvar.setIntygsReferens(new IntygsReferens());
        fragaSvar.getIntygsReferens().setIntygsId("1");
        fragaSvar.getIntygsReferens().setIntygsTyp("FK7263");
        return fragaSvar;
    }

    private String expectationFromFile(String filename) throws Exception {
        return FileUtils.readFileToString(new ClassPathResource("MailNotificationServiceTest/" + filename).getFile());
    }

}
