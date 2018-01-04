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
package se.inera.intyg.webcert.web.service.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.integration.hsa.stub.HsaServiceStub;
import se.inera.intyg.webcert.mailstub.MailStore;
import se.inera.intyg.webcert.mailstub.OutgoingMail;

import javax.annotation.PostConstruct;
import java.io.IOException;

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

    @PostConstruct
    public void setupTestlandVardgivare() throws IOException {
        Vardgivare vardgivare = new CustomObjectMapper().readValue(new ClassPathResource(
                "MailNotificationServiceTest/landstinget-testland.json").getFile(), Vardgivare.class);
        hsaStub.getVardgivare().add(vardgivare);
    }

    @Test
    public void testMailDeliveryForIncomingQuestion() throws Exception {

        mailNotificationService.sendMailForIncomingQuestion(mailNotification(CITYAKUTEN));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("cityakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har ställt en fråga angående ett intyg", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-cityakuten.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswer() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(mailNotification(CITYAKUTEN));
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

        mailNotificationService.sendMailForIncomingQuestion(mailNotification(VANSTERAKUTEN));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("vansterakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har ställt en fråga angående ett intyg", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-vansterakuten.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswerToMottagning() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(mailNotification(VANSTERAKUTEN));
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

        mailNotificationService.sendMailForIncomingQuestion(mailNotification(LANDCENTRALEN));
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

        mailNotificationService.sendMailForIncomingAnswer(mailNotification(LANDCENTRALEN));
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

        mailNotificationService.sendMailForIncomingQuestion(mailNotification(KUSTAKUTEN));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("sjocentralen@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har ställt en fråga angående ett intyg", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-kustakuten.html"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswerToMottagningWithoutMailButParentEnhetWithMail() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(mailNotification(KUSTAKUTEN));

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
        mailNotificationService.sendMailForIncomingQuestion(mailNotification(CITYAKUTEN));
        mailStore.setWait(false);
        mailStore.waitForMails(1);
        long endTimestamp = System.currentTimeMillis();
        assertTrue((endTimestamp - startTimestamp) < THRESHOLD);
        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("cityakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har ställt en fråga angående ett intyg", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-cityakuten.html"), mail.getBody());
    }

    @Test
    public void linkHasCorrectId() {
        String url = mailNotificationService.intygsUrl(mailNotification("enhet"));

        assertEquals("https://www.webcert.se/webcert/web/user/certificate/1/questions?enhet=enhet", url);
    }

    @Test
    public void testMailDeliveryForIncomingQuestionNotFk7263() throws Exception {

        mailNotificationService.sendMailForIncomingQuestion(mailNotification(CITYAKUTEN, LuseEntryPoint.MODULE_ID));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("cityakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har ställt en fråga angående ett intyg", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-cityakuten.html", "luse"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswerNotFk7263() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(mailNotification(CITYAKUTEN, LuseEntryPoint.MODULE_ID));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("cityakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har svarat på en fråga", mail.getSubject());
        assertEquals(expectationFromFile("incoming-answer-body-cityakuten.html", "luse"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingQuestionToMottagningNotFk7263() throws Exception {

        mailNotificationService.sendMailForIncomingQuestion(mailNotification(VANSTERAKUTEN, LuseEntryPoint.MODULE_ID));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("vansterakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har ställt en fråga angående ett intyg", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-vansterakuten.html", "luse"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswerToMottagningNotFk7263() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(mailNotification(VANSTERAKUTEN, LuseEntryPoint.MODULE_ID));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("vansterakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har svarat på en fråga", mail.getSubject());
        assertEquals(expectationFromFile("incoming-answer-body-vansterakuten.html", "luse"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingQuestionForEnhetWithoutMailNotFk7263() throws Exception {

        mailNotificationService.sendMailForIncomingQuestion(mailNotification(LANDCENTRALEN, LuseEntryPoint.MODULE_ID));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("admin@sverige.se", mail.getRecipients().get(0));
        assertEquals("Fråga/svar Webcert: Enhet utan mailadress eller koppling", mail.getSubject());
        assertEquals(expectationFromFile("admin-body.html", "luse"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswerForEnhetWithoutMailNotFk7263() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(mailNotification(LANDCENTRALEN, LuseEntryPoint.MODULE_ID));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("admin@sverige.se", mail.getRecipients().get(0));
        assertEquals("Fråga/svar Webcert: Enhet utan mailadress eller koppling", mail.getSubject());
        assertEquals(expectationFromFile("admin-body.html", "luse"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingQuestionToMottagningWithoutMailButParentEnhetWithMailNotFk7263() throws Exception {

        mailNotificationService.sendMailForIncomingQuestion(mailNotification(KUSTAKUTEN, LuseEntryPoint.MODULE_ID));
        mailStore.waitForMails(1);

        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("sjocentralen@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har ställt en fråga angående ett intyg", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-kustakuten.html", "luse"), mail.getBody());
    }

    @Test
    public void testMailDeliveryForIncomingAnswerToMottagningWithoutMailButParentEnhetWithMailNotFk7263() throws Exception {

        mailNotificationService.sendMailForIncomingAnswer(mailNotification(KUSTAKUTEN, LuseEntryPoint.MODULE_ID));

        mailStore.waitForMails(1);
        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("sjocentralen@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har svarat på en fråga", mail.getSubject());
        assertEquals(expectationFromFile("incoming-answer-body-kustakuten.html", "luse"), mail.getBody());
    }

    @Test
    public void testAsyncMailDeliveryForIncomingQuestionNotFk7263() throws Exception {

        long startTimestamp = System.currentTimeMillis();
        mailStore.setWait(true);
        mailNotificationService.sendMailForIncomingQuestion(mailNotification(CITYAKUTEN, LuseEntryPoint.MODULE_ID));
        mailStore.setWait(false);
        mailStore.waitForMails(1);
        long endTimestamp = System.currentTimeMillis();
        assertTrue((endTimestamp - startTimestamp) < THRESHOLD);
        assertEquals(1, mailStore.getMails().size());
        OutgoingMail mail = mailStore.getMails().get(0);

        assertEquals(1, mail.getRecipients().size());
        assertEquals("no-reply@webcert.intygstjanster.se", mail.getFrom());
        assertEquals("cityakuten@testland.se", mail.getRecipients().get(0));
        assertEquals("Försäkringskassan har ställt en fråga angående ett intyg", mail.getSubject());
        assertEquals(expectationFromFile("incoming-question-body-cityakuten.html", "luse"), mail.getBody());
    }

    @Test
    public void linkHasCorrectIdNotFk7263() {
        String url = mailNotificationService.intygsUrl(mailNotification("enhet", LuseEntryPoint.MODULE_ID));

        assertEquals("https://www.webcert.se/webcert/web/user/certificate/luse/1/questions?enhet=enhet", url);
    }

    @After
    public void cleanMailStore() {
        mailStore.getMails().clear();
    }

    private MailNotification mailNotification(String enhetsId) {
        return mailNotification(enhetsId, Fk7263EntryPoint.MODULE_ID);
    }

    private MailNotification mailNotification(String enhetsId, String intygsTyp) {
        return new MailNotification("2L", "1", intygsTyp, enhetsId, null, null);
    }

    private String expectationFromFile(String filename, String folder) throws Exception {
        return FileUtils.readFileToString(new ClassPathResource("MailNotificationServiceTest/" + folder + "/" + filename).getFile());
    }

    private String expectationFromFile(String filename) throws Exception {
        return FileUtils.readFileToString(new ClassPathResource("MailNotificationServiceTest/" + filename).getFile());
    }

}
