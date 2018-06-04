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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.riv.infrastructure.directory.privatepractitioner.v1.EnhetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;

import javax.mail.Address;
import javax.mail.internet.MimeMessage;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MailNotificationServiceImplTest {

    @InjectMocks
    private MailNotificationServiceImpl mailNotificationService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private HsaOrganizationsService hsaOrganizationUnitService;

    @Mock
    private MonitoringLogService monitoringService;

    @Mock
    private PPService ppService;

    @Mock
    private UtkastRepository utkastRepository;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(mailNotificationService, "adminMailAddress", "AdminMail");
        ReflectionTestUtils.setField(mailNotificationService, "fromAddress", "FromAddress");
        ReflectionTestUtils.setField(mailNotificationService, "webCertHostUrl", "WebCertHostUrl");
        ReflectionTestUtils.setField(mailNotificationService, "ppLogicalAddress", "PpLogicalAddress");
        MimeMessage mimeMessage = new MimeMessage(mock(MimeMessage.class));
        doReturn(mimeMessage).when(mailSender).createMimeMessage();
        Vardenhet vardenhet = new Vardenhet("aflkjdsalkjjlk", "dsaflkj", null, null, "adsflkjasdflkjadfsjlk");
        vardenhet.setEpost("epost@mockadress.net");
        doReturn(vardenhet).when(hsaOrganizationUnitService).getVardenhet(anyString());
    }

    @Captor
    private ArgumentCaptor<MimeMessage> mimeCaptor;

    @Test
    public void testSendMailForIncomingQuestionHsaIsCalledIfNotPrivatePractitioner() throws Exception {
        // Given
        MailNotification mailNotification = mailNotification("intygsId",
                "ThisIsNotPp" + MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");

        // When
        try {
            mailNotificationService.sendMailForIncomingQuestion(mailNotification);
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Then
        verify(hsaOrganizationUnitService, times(1)).getVardenhet(anyString());
    }

    @Test
    public void testSendMailForIncomingQuestionHsaIsNotCalledIfPrivatePractitioner() throws Exception {
        // Given
        HoSPersonType hoSPersonType = new HoSPersonType();
        EnhetType enhet = new EnhetType();
        enhet.setEpost("test@test.se");
        enhet.setEnhetsnamn("TestEnhet");
        hoSPersonType.setEnhet(enhet);
        doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), isNull(), isNull());

        MailNotification mailNotification = mailNotification("intygsId",
                MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");

        // When
        mailNotificationService.sendMailForIncomingQuestion(mailNotification);

        // Then
        verify(hsaOrganizationUnitService, times(0)).getVardenhet(anyString());
    }

    @Test
    public void testSendMailForIncomingQuestionMailIsSentToPrivatePractitioner() throws Exception {
        // Given
        HoSPersonType hoSPersonType = new HoSPersonType();
        EnhetType enhet = new EnhetType();
        String epost = "test@test.se";
        enhet.setEpost(epost);
        enhet.setEnhetsnamn("TestEnhet");
        hoSPersonType.setEnhet(enhet);
        doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), isNull(), isNull());

        MailNotification mailNotification = mailNotification("intygsId",
                MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");

        // When
        mailNotificationService.sendMailForIncomingQuestion(mailNotification);

        // Then
        verify(mailSender, times(1)).send(mimeCaptor.capture());
        MimeMessage mimeMessage = mimeCaptor.getValue();
        Address[] allRecipients = mimeMessage.getAllRecipients();
        assertEquals(1, allRecipients.length);
        assertEquals(epost, allRecipients[0].toString());
    }

    @Test
    public void testSendMailForIncomingAnswerHsaIsCalledIfNotPrivatePractitioner() throws Exception {
        // Given
        MailNotification mailNotification = mailNotification("intygsId",
                "ThisIsNotPp" + MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");

        // When
        try {
            mailNotificationService.sendMailForIncomingAnswer(mailNotification);
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Then
        verify(hsaOrganizationUnitService, times(1)).getVardenhet(anyString());
    }

    @Test
    public void testSendMailForIncomingAnswerHsaIsNotCalledIfPrivatePractitioner() throws Exception {
        // Given
        HoSPersonType hoSPersonType = new HoSPersonType();
        EnhetType enhet = new EnhetType();
        enhet.setEpost("test@test.se");
        enhet.setEnhetsnamn("TestEnhet");
        hoSPersonType.setEnhet(enhet);
        doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), isNull(), isNull());

        MailNotification mailNotification = mailNotification("intygsId",
                MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");

        // When
        mailNotificationService.sendMailForIncomingAnswer(mailNotification);

        // Then
        verify(hsaOrganizationUnitService, times(0)).getVardenhet(anyString());
    }

    @Test
    public void testSendMailForIncomingAnswerMailIsSentToPrivatePractitioner() throws Exception {
        // Given
        HoSPersonType hoSPersonType = new HoSPersonType();
        EnhetType enhet = new EnhetType();
        String epost = "test@test.se";
        enhet.setEpost(epost);
        enhet.setEnhetsnamn("TestEnhet");
        hoSPersonType.setEnhet(enhet);
        doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), isNull(), isNull());

        MailNotification mailNotification = mailNotification("intygsId",
                MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");

        // When
        mailNotificationService.sendMailForIncomingAnswer(mailNotification);

        // Then
        verify(mailSender, times(1)).send(mimeCaptor.capture());
        MimeMessage mimeMessage = mimeCaptor.getValue();
        Address[] allRecipients = mimeMessage.getAllRecipients();
        assertEquals(1, allRecipients.length);
        assertEquals(epost, allRecipients[0].toString());
    }

    @Test
    public void testIntygsUrlUthopp() throws Exception {
        final String intygsId = "intygsId";
        // Given
        MailNotification mailNotification = mailNotification(intygsId, null);

        when(utkastRepository.findOne(intygsId)).thenReturn(null);

        // When
        final String url = mailNotificationService.intygsUrl(mailNotification);

        // Then
        assertEquals("WebCertHostUrl/webcert/web/user/certificate/intygsId/questions", url);
        verify(utkastRepository).findOne(intygsId);
    }

    @Test
    public void testIntygsUrlLandsting() throws Exception {
        final String intygsId = "intygsId";
        // Given
        MailNotification mailNotification = mailNotification(intygsId, null);

        Utkast utkast = new Utkast();
        when(utkastRepository.findOne(intygsId)).thenReturn(utkast);

        // When
        final String url = mailNotificationService.intygsUrl(mailNotification);

        // Then
        assertEquals("WebCertHostUrl/webcert/web/user/basic-certificate/intygsId/questions", url);
        verify(utkastRepository).findOne(intygsId);
    }

    @Test
    public void testIntygsUrlPp() throws Exception {
        final String intygsId = "intygsId";
        // Given
        MailNotification mailNotification = mailNotification(intygsId,
                MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "AndSomeOtherText");

        // When
        final String url = mailNotificationService.intygsUrl(mailNotification);

        // Then
        assertEquals("WebCertHostUrl/webcert/web/user/pp-certificate/intygsId/questions?enhet="
                + MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "AndSomeOtherText", url);
        verifyZeroInteractions(utkastRepository);
    }

    @Test
    public void testIntygsUrlUthoppNotFk7263() throws Exception {
        final String intygsId = "intygsId";
        // Given
        MailNotification mailNotification = mailNotification(intygsId, null, LuseEntryPoint.MODULE_ID);

        when(utkastRepository.findOne(intygsId)).thenReturn(null);

        // When
        final String url = mailNotificationService.intygsUrl(mailNotification);

        // Then
        assertEquals("WebCertHostUrl/webcert/web/user/certificate/luse/intygsId/questions", url);
        verify(utkastRepository).findOne(intygsId);
    }

    @Test
    public void testIntygsUrlLandstingNotFk7263() throws Exception {
        final String intygsId = "intygsId";
        // Given
        MailNotification mailNotification = mailNotification(intygsId, null, LuseEntryPoint.MODULE_ID);

        Utkast utkast = new Utkast();
        when(utkastRepository.findOne(intygsId)).thenReturn(utkast);

        // When
        final String url = mailNotificationService.intygsUrl(mailNotification);

        // Then
        assertEquals("WebCertHostUrl/webcert/web/user/basic-certificate/luse/intygsId/questions", url);
        verify(utkastRepository).findOne(intygsId);
    }

    @Test
    public void testIntygsUrlPpNotFk7263() throws Exception {
        final String intygsId = "intygsId";
        // Given
        MailNotification mailNotification = mailNotification(intygsId,
                MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "AndSomeOtherText", LuseEntryPoint.MODULE_ID);

        // When
        final String url = mailNotificationService.intygsUrl(mailNotification);

        // Then
        assertEquals("WebCertHostUrl/webcert/web/user/pp-certificate/luse/intygsId/questions?enhet="
                + MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "AndSomeOtherText", url);
        verifyZeroInteractions(utkastRepository);
    }

    private MailNotification mailNotification(String intygsId, String enhetsId) {
        return mailNotification(intygsId, enhetsId, Fk7263EntryPoint.MODULE_ID);
    }

    private MailNotification mailNotification(String intygsId, String enhetsId, String intygsTyp) {
        return new MailNotification(null, intygsId, intygsTyp, enhetsId, null, null);
    }
}
