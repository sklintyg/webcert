/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.privatepractitioner.PrivatePractitionerService;
import se.inera.intyg.webcert.web.privatepractitioner.toggle.PrivatePractitionerServiceProfile;
import se.inera.intyg.webcert.web.service.employee.EmployeeNameService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.riv.infrastructure.directory.privatepractitioner.v1.EnhetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;

@ExtendWith(MockitoExtension.class)
class MailNotificationServiceImplTest {

    private static final String SIGNED_BY_HSA_ID = "SIGNED_BY_HSA_ID";
    private static final String EXPECTED_NAME = "ExpectedName";
    private static final String EXPECTED_UNIT = "ExpectedUnit";
    private static final String UNIT_ID = "unitId";
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
    private PrivatePractitionerService privatePractitionerService;

    @Mock
    private PrivatePractitionerServiceProfile privatePractitionerServiceProfile;

    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private EmployeeNameService employeeNameService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailNotificationService, "adminMailAddress", "AdminMail");
        ReflectionTestUtils.setField(mailNotificationService, "fromAddress", "FromAddress");
        ReflectionTestUtils.setField(mailNotificationService, "webCertHostUrl", "WebCertHostUrl");
        ReflectionTestUtils.setField(mailNotificationService, "ppLogicalAddress", "PpLogicalAddress");
    }

    @Captor
    private ArgumentCaptor<MimeMessage> mimeCaptor;

    @Test
    void testSendMailForIncomingQuestionHsaIsCalledIfNotPrivatePractitioner() throws MessagingException {
        // Given
        MailNotification mailNotification = mailNotification("intygsId",
            "ThisIsNotPp" + MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");

        Vardenhet vardenhet = new Vardenhet("aflkjdsalkjjlk", "ExpectedUnit", null, null, "adsflkjasdflkjadfsjlk");
        vardenhet.setEpost("epost@mockadress.net");
        doReturn(vardenhet).when(hsaOrganizationUnitService).getVardenhet(anyString());

        MimeMessage mimeMessage = new MimeMessage(mock(MimeMessage.class));
        doReturn(mimeMessage).when(mailSender).createMimeMessage();

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
    void testSendMailForIncomingQuestionHsaIsNotCalledIfPrivatePractitioner() throws MessagingException {
        // Given
        HoSPersonType hoSPersonType = new HoSPersonType();
        EnhetType enhet = new EnhetType();
        enhet.setEpost("test@test.se");
        enhet.setEnhetsnamn("TestEnhet");
        hoSPersonType.setEnhet(enhet);
        doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), eq(SIGNED_BY_HSA_ID), isNull());

        MimeMessage mimeMessage = new MimeMessage(mock(MimeMessage.class));
        doReturn(mimeMessage).when(mailSender).createMimeMessage();

        MailNotification mailNotification = mailNotification("intygsId",
            MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");

        // When
        mailNotificationService.sendMailForIncomingQuestion(mailNotification);

        // Then
        verify(hsaOrganizationUnitService, times(0)).getVardenhet(anyString());
    }

    @Test
    void testSendMailForIncomingQuestionMailIsSentToPrivatePractitioner() throws Exception {
        // Given
        HoSPersonType hoSPersonType = new HoSPersonType();
        EnhetType enhet = new EnhetType();
        String epost = "test@test.se";
        enhet.setEpost(epost);
        enhet.setEnhetsnamn("TestEnhet");
        hoSPersonType.setEnhet(enhet);
        doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), eq(SIGNED_BY_HSA_ID), isNull());

        MailNotification mailNotification = mailNotification("intygsId",
            MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");

        MimeMessage message = new MimeMessage(mock(MimeMessage.class));
        doReturn(message).when(mailSender).createMimeMessage();

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
    void testSendMailForIncomingAnswerHsaIsCalledIfNotPrivatePractitioner() throws MessagingException {
        // Given
        MailNotification mailNotification = mailNotification("intygsId",
            "ThisIsNotPp" + MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");

        Vardenhet vardenhet = new Vardenhet("aflkjdsalkjjlk", "ExpectedUnit", null, null, "adsflkjasdflkjadfsjlk");
        vardenhet.setEpost("epost@mockadress.net");
        doReturn(vardenhet).when(hsaOrganizationUnitService).getVardenhet(anyString());

        MimeMessage message = new MimeMessage(mock(MimeMessage.class));
        doReturn(message).when(mailSender).createMimeMessage();

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
    void testSendMailForIncomingAnswerHsaIsNotCalledIfPrivatePractitioner() throws MessagingException {
        // Given
        HoSPersonType hoSPersonType = new HoSPersonType();
        EnhetType enhet = new EnhetType();
        enhet.setEpost("test@test.se");
        enhet.setEnhetsnamn("TestEnhet");
        hoSPersonType.setEnhet(enhet);
        doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), eq(SIGNED_BY_HSA_ID), isNull());

        MailNotification mailNotification = mailNotification("intygsId",
            MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");

        MimeMessage message = new MimeMessage(mock(MimeMessage.class));
        doReturn(message).when(mailSender).createMimeMessage();

        // When
        mailNotificationService.sendMailForIncomingAnswer(mailNotification);

        // Then
        verify(hsaOrganizationUnitService, times(0)).getVardenhet(anyString());
    }

    @Test
    void testSendMailForIncomingAnswerMailIsSentToPrivatePractitioner() throws Exception {
        // Given
        HoSPersonType hoSPersonType = new HoSPersonType();
        EnhetType enhet = new EnhetType();
        String epost = "test@test.se";
        enhet.setEpost(epost);
        enhet.setEnhetsnamn("TestEnhet");
        hoSPersonType.setEnhet(enhet);
        doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), eq(SIGNED_BY_HSA_ID), isNull());

        MailNotification mailNotification = mailNotification("intygsId",
            MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234");

        MimeMessage message = new MimeMessage(mock(MimeMessage.class));
        doReturn(message).when(mailSender).createMimeMessage();

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
    void testIntygsUrlUthopp() {
        final String intygsId = "intygsId";
        // Given
        MailNotification mailNotification = mailNotification(intygsId, null);

        when(utkastRepository.findById(intygsId)).thenReturn(Optional.empty());

        // When
        final String url = mailNotificationService.intygsUrl(mailNotification);

        // Then
        assertEquals("WebCertHostUrl/webcert/web/user/certificate/intygsId/questions", url);
        verify(utkastRepository).findById(intygsId);
    }

    @Test
    void testIntygsUrlLandsting() {
        final String intygsId = "intygsId";
        // Given
        MailNotification mailNotification = mailNotification(intygsId, null);

        Utkast utkast = new Utkast();
        when(utkastRepository.findById(intygsId)).thenReturn(Optional.of(utkast));

        // When
        final String url = mailNotificationService.intygsUrl(mailNotification);

        // Then
        assertEquals("WebCertHostUrl/webcert/web/user/basic-certificate/intygsId/questions", url);
        verify(utkastRepository).findById(intygsId);
    }

    @Test
    void testIntygsUrlPp() {
        final String intygsId = "intygsId";
        // Given
        MailNotification mailNotification = mailNotification(intygsId,
            MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "AndSomeOtherText");

        // When
        final String url = mailNotificationService.intygsUrl(mailNotification);

        // Then
        assertEquals("WebCertHostUrl/webcert/web/user/pp-certificate/intygsId/questions?enhet="
            + MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "AndSomeOtherText", url);
        verifyNoInteractions(utkastRepository);
    }

    @Test
    void testIntygsUrlUthoppNotFk7263() {
        final String intygsId = "intygsId";
        // Given
        MailNotification mailNotification = mailNotification(intygsId, null, LuseEntryPoint.MODULE_ID);

        when(utkastRepository.findById(intygsId)).thenReturn(Optional.empty());

        // When
        final String url = mailNotificationService.intygsUrl(mailNotification);

        // Then
        assertEquals("WebCertHostUrl/webcert/web/user/certificate/luse/intygsId/questions", url);
        verify(utkastRepository).findById(intygsId);
    }

    @Test
    void testIntygsUrlLandstingNotFk7263() {
        final String intygsId = "intygsId";
        // Given
        MailNotification mailNotification = mailNotification(intygsId, null, LuseEntryPoint.MODULE_ID);

        Utkast utkast = new Utkast();
        when(utkastRepository.findById(intygsId)).thenReturn(Optional.of(utkast));

        // When
        final String url = mailNotificationService.intygsUrl(mailNotification);

        // Then
        assertEquals("WebCertHostUrl/webcert/web/user/basic-certificate/luse/intygsId/questions", url);
        verify(utkastRepository).findById(intygsId);
    }

    @Test
    void testIntygsUrlPpNotFk7263() {
        final String intygsId = "intygsId";
        // Given
        MailNotification mailNotification = mailNotification(intygsId,
            MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "AndSomeOtherText", LuseEntryPoint.MODULE_ID);

        // When
        final String url = mailNotificationService.intygsUrl(mailNotification);

        // Then
        assertEquals("WebCertHostUrl/webcert/web/user/pp-certificate/luse/intygsId/questions?enhet="
            + MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "AndSomeOtherText", url);
        verifyNoInteractions(utkastRepository);
    }

    private MailNotification mailNotification(String intygsId, String enhetsId) {
        return mailNotification(intygsId, enhetsId, Fk7263EntryPoint.MODULE_ID);
    }

    private MailNotification mailNotification(String intygsId, String enhetsId, String intygsTyp) {
        return new MailNotification(null, intygsId, intygsTyp, enhetsId, null, SIGNED_BY_HSA_ID);
    }


    @Test
    void bodyShallContainEmployeeNameAndUnitNameForIncomingQuestionsForPrivatePractitioner()
        throws MessagingException, IOException {
        final var expectedContent = "<p>Försäkringskassan har ställt en fråga på ett intyg utfärdat av "
            + "<b>ExpectedName</b> på <b>ExpectedUnit</b>."
            + "<br><a href=\"WebCertHostUrl/webcert/web/user/pp-certificate/intygsId/questions?enhet=SE165565594230-WEBCERT1234\">"
            + "Läs och besvara frågan i Webcert</a></p><p>OBS! Sätt i ditt SITHS-kort innan du klickar på länken.</p>";

        final var mailNotification = mailNotification(
            "intygsId",
            MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234"
        );
        final var hoSPersonType = new HoSPersonType();
        final var unit = new EnhetType();
        final var epost = "test@test.se";
        unit.setEpost(epost);
        unit.setEnhetsnamn(EXPECTED_UNIT);
        hoSPersonType.setEnhet(unit);

        MimeMessage message = new MimeMessage(mock(MimeMessage.class));
        doReturn(message).when(mailSender).createMimeMessage();

        doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), eq(SIGNED_BY_HSA_ID), isNull());
        doReturn(EXPECTED_NAME).when(employeeNameService).getEmployeeHsaName(SIGNED_BY_HSA_ID);

        mailNotificationService.sendMailForIncomingQuestion(mailNotification);

        verify(mailSender, times(1)).send(mimeCaptor.capture());
        assertEquals(expectedContent, mimeCaptor.getValue().getContent());
    }

    @Test
    void bodyShallContainEmployeeNameAndUnitNameForIncomingQuestions()
        throws MessagingException, IOException {
        final var expectedContent = "<p>Försäkringskassan har ställt en fråga på ett intyg utfärdat av <b>ExpectedName</b> på "
            + "<b>ExpectedUnit</b>.<br><a href=\"WebCertHostUrl/webcert/web/user/certificate/intygsId/questions?enhet=unitId\">"
            + "Läs och besvara frågan i Webcert</a></p><p>OBS! Sätt i ditt SITHS-kort innan du klickar på länken.</p>";

        final var mailNotification = mailNotification(
            "intygsId",
            UNIT_ID
        );
        final var hoSPersonType = new HoSPersonType();
        final var unit = new EnhetType();
        final var epost = "test@test.se";
        unit.setEpost(epost);
        unit.setEnhetsnamn(EXPECTED_UNIT);
        hoSPersonType.setEnhet(unit);

        Vardenhet vardenhet = new Vardenhet("aflkjdsalkjjlk", "ExpectedUnit", null, null, "adsflkjasdflkjadfsjlk");
        vardenhet.setEpost("epost@mockadress.net");
        doReturn(vardenhet).when(hsaOrganizationUnitService).getVardenhet(anyString());

        MimeMessage message = new MimeMessage(mock(MimeMessage.class));
        doReturn(message).when(mailSender).createMimeMessage();

        doReturn(EXPECTED_NAME).when(employeeNameService).getEmployeeHsaName(SIGNED_BY_HSA_ID);

        mailNotificationService.sendMailForIncomingQuestion(mailNotification);

        verify(mailSender, times(1)).send(mimeCaptor.capture());
        assertEquals(expectedContent, mimeCaptor.getValue().getContent());
    }

    @Test
    void bodyShallContainEmployeeNameAndUnitNameForIncomingAnswerForPrivatePractitioner()
        throws MessagingException, IOException {
        final var expectedContent = "<p>Det har kommit ett svar från Försäkringskassan på en fråga som <b>ExpectedName</b> "
            + "på <b>ExpectedUnit</b> har ställt. har ställt."
            + "<br><a href=\"WebCertHostUrl/webcert/web/user/pp-certificate/intygsId/questions?enhet=SE165565594230-WEBCERT1234\">"
            + "Läs svaret i Webcert</a></p><p>OBS! Sätt i ditt SITHS-kort innan du klickar på länken.</p>";

        final var mailNotification = mailNotification(
            "intygsId",
            MailNotificationServiceImpl.PRIVATE_PRACTITIONER_HSAID_PREFIX + "1234"
        );
        final var hoSPersonType = new HoSPersonType();
        final var unit = new EnhetType();
        final var epost = "test@test.se";
        unit.setEpost(epost);
        unit.setEnhetsnamn(EXPECTED_UNIT);
        hoSPersonType.setEnhet(unit);

        MimeMessage message = new MimeMessage(mock(MimeMessage.class));
        doReturn(message).when(mailSender).createMimeMessage();

        doReturn(hoSPersonType).when(ppService).getPrivatePractitioner(anyString(), eq(SIGNED_BY_HSA_ID), isNull());
        doReturn(EXPECTED_NAME).when(employeeNameService).getEmployeeHsaName(SIGNED_BY_HSA_ID);

        mailNotificationService.sendMailForIncomingAnswer(mailNotification);

        verify(mailSender, times(1)).send(mimeCaptor.capture());
        assertEquals(expectedContent, mimeCaptor.getValue().getContent());
    }

    @Test
    void bodyShallContainEmployeeNameAndUnitNameForIncomingAnswer()
        throws MessagingException, IOException {
        final var expectedContent = "<p>Det har kommit ett svar från Försäkringskassan på en fråga som <b>ExpectedName</b> på "
            + "<b>ExpectedUnit</b> har ställt. har ställt."
            + "<br><a href=\"WebCertHostUrl/webcert/web/user/certificate/intygsId/questions?enhet=unitId\">"
            + "Läs svaret i Webcert</a></p><p>OBS! Sätt i ditt SITHS-kort innan du klickar på länken.</p>";

        final var mailNotification = mailNotification(
            "intygsId",
            UNIT_ID
        );
        final var hoSPersonType = new HoSPersonType();
        final var unit = new EnhetType();
        final var epost = "test@test.se";
        unit.setEpost(epost);
        unit.setEnhetsnamn(EXPECTED_UNIT);
        hoSPersonType.setEnhet(unit);

        MimeMessage message = new MimeMessage(mock(MimeMessage.class));
        doReturn(message).when(mailSender).createMimeMessage();

        Vardenhet vardenhet = new Vardenhet("aflkjdsalkjjlk", "ExpectedUnit", null, null, "adsflkjasdflkjadfsjlk");
        vardenhet.setEpost("epost@mockadress.net");
        doReturn(vardenhet).when(hsaOrganizationUnitService).getVardenhet(anyString());

        doReturn(EXPECTED_NAME).when(employeeNameService).getEmployeeHsaName(SIGNED_BY_HSA_ID);

        mailNotificationService.sendMailForIncomingAnswer(mailNotification);

        verify(mailSender, times(1)).send(mimeCaptor.capture());
        assertEquals(expectedContent, mimeCaptor.getValue().getContent());
    }
}