package se.inera.webcert.service;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.stub.HsaServiceStub;
import se.inera.webcert.mailstub.MailStore;
import se.inera.webcert.mailstub.OutgoingMail;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;

/**
 * @author andreaskaltenbach
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:MailNotificationServiceTest/test-context.xml")
public class MailNotificationServiceTest {

    private static final String CITYAKUTEN = "cityakuten";

    private static final String LANDCENTRALEN = "landcentralen";
    private static final String VANSTERAKUTEN = "vansterakuten";
    private static final String HOGERAKUTEN = "hogerakuten";
    private static final String KUSTAKUTEN = "kustakuten";

    @Autowired
    private MailNotificationServiceImpl mailNotificationService;

    @Autowired
    private MailStore mailStore;

    @Autowired
    private HsaServiceStub hsaStub;

    @PostConstruct
    public void setupTestlandVardgivare() throws IOException {
        Vardgivare vardgivare = new ObjectMapper().readValue(new ClassPathResource(
                "MailNotificationServiceTest/landstinget-testland.json").getFile(), Vardgivare.class);
        hsaStub.getVardgivare().add(vardgivare);
    }

    @PostConstruct
    public void setupMailNotificationService() {
        mailNotificationService.webCertHostUrl = "https://www.webcert.se";
        mailNotificationService.adminMailAddress = "admin@sverige.se";
    }

    @Test
    public void testMailDeliveryForIncomingQuestion() throws Exception {

        mailNotificationService.sendMailForIncomingQuestion(fragaSvar(CITYAKUTEN));

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("cityakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Inkommen fråga från Försäkringskassan", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-cityakuten.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswer() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(fragaSvar(CITYAKUTEN));

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("cityakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har svarat på en fråga", mail.getSubject());
        assertEquals(expectationFromFile("incoming-answer-body-cityakuten.html"), mail.getBody());
    }

    @Test
        public void testMailDeliveryForIncomingQuestionToMottagning() throws Exception {

            mailNotificationService.sendMailForIncomingQuestion(fragaSvar(VANSTERAKUTEN));

            assertEquals(1, mailStore.getMails().size());
            OutgoingMail mail = mailStore.getMails().get(0);

            assertEquals(1, mail.getRecipients().size());
            assertEquals("vansterakuten@testland.se", mail.getRecipients().get(0));
            assertEquals("Inkommen fråga från Försäkringskassan", mail.getSubject());
            assertEquals(expectationFromFile("incoming-question-body-vansterakuten.html"), mail.getBody());
        }

        @Test
        public void testMailDeliveryForIncomingAnswerToMottagning() throws Exception {

            mailNotificationService.sendMailForIncomingAnswer(fragaSvar(VANSTERAKUTEN));

            assertEquals(1, mailStore.getMails().size());
            OutgoingMail mail = mailStore.getMails().get(0);

            assertEquals(1, mail.getRecipients().size());
            assertEquals("vansterakuten@testland.se", mail.getRecipients().get(0));
            assertEquals("Försäkringskassan har svarat på en fråga", mail.getSubject());
            assertEquals(expectationFromFile("incoming-answer-body-vansterakuten.html"), mail.getBody());
        }

    @Test
    public void testMailDeliveryForIncomingQuestionForEnhetWithoutMail() throws Exception {

        mailNotificationService.sendMailForIncomingQuestion(fragaSvar(LANDCENTRALEN));

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("admin@sverige.se", mail.getRecipients().get(0));
        assertEquals("Fråga/svar Webcert: Enhet utan mailadress eller koppling", mail.getSubject());
        assertEquals(expectationFromFile("admin-body.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswerForEnhetWithoutMail() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(fragaSvar(LANDCENTRALEN));

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("admin@sverige.se", mail.getRecipients().get(0));
        assertEquals("Fråga/svar Webcert: Enhet utan mailadress eller koppling", mail.getSubject());
        assertEquals(expectationFromFile("admin-body.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingQuestionToMottagningWithoutMailButParentEnhetWithMail() throws Exception {

        mailNotificationService.sendMailForIncomingQuestion(fragaSvar(KUSTAKUTEN));

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("sjocentralen@testland.se", mail.getRecipients().get(0));
        assertEquals("Inkommen fråga från Försäkringskassan", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-kustakuten.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswerToMottagningWithoutMailButParentEnhetWithMail() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(fragaSvar(KUSTAKUTEN));

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("sjocentralen@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har svarat på en fråga", mail.getSubject());
        assertEquals(expectationFromFile("incoming-answer-body-kustakuten.html"), mail.getBody());
    }

    @After
    public void cleanMailStore() {
        mailStore.getMails().clear();
    }

    private FragaSvar fragaSvar(String enhetsId) {
        FragaSvar fragaSvar = new FragaSvar();
        fragaSvar.setVardperson(new Vardperson());
        fragaSvar.getVardperson().setEnhetsId(enhetsId);
        fragaSvar.setInternReferens(1L);
        fragaSvar.setIntygsReferens(new IntygsReferens());
        fragaSvar.getIntygsReferens().setIntygsId("1L");
        fragaSvar.getIntygsReferens().setIntygsTyp("FK7263");
        return fragaSvar;
    }

    private String expectationFromFile(String filename) throws Exception {
        return FileUtils.readFileToString(new ClassPathResource("MailNotificationServiceTest/" + filename).getFile());
    }

}
