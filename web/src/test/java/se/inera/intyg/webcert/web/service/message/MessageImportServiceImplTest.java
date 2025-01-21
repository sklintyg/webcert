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
package se.inera.intyg.webcert.web.service.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.infra.message.dto.MessageFromIT;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.integration.ITIntegrationService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations.FrontendRelations;

@RunWith(MockitoJUnitRunner.class)
public class MessageImportServiceImplTest {

    @Mock
    private UtkastRepository draftRepository;

    @Mock
    private ITIntegrationService itIntegrationService;

    @Mock
    private IntygService certificateService;

    @Mock
    private ArendeRepository messageRepository;

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private MessageImportServiceImpl messageImportService;

    private static String CERTIFICATE_ID = "28a02dae-0d16-4745-8ecf-4cea05cd637d";
    private static String CERTIFICATE_TYPE = "lisjp";
    private static String CARE_PROVIDER_ID = "TSTNMT2321000156-102Q";
    private static String CARE_UNIT_ID = "TSTNMT2321000156-1077";

    private static String QUESTION_TO_RECIPIENT_MESSAGE_ID = "6952fb62-fcb7-4b47-88cc-a57b9231776a";
    private static String QUESTION_TO_RECIPIENT_SUBJECT = "KONTKT";
    private static String QUESTION_TO_RECIPIENT_LOGICAL_ADDRESS = "FK";
    private static String QUESTION_TO_RECIPIENT_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:SendMessageToRecipient xmlns:ns6=\"urn:riv:clinicalprocess:healthcond:certificate:3.4\" xmlns:ns5=\"urn:riv:clinicalprocess:healthcond:certificate:3.2\" xmlns:ns2=\"urn:riv:clinicalprocess:healthcond:certificate:SendMessageToRecipientResponder:2\" xmlns:ns4=\"urn:riv:clinicalprocess:healthcond:certificate:3\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:ns3=\"urn:riv:clinicalprocess:healthcond:certificate:types:3\"><ns2:meddelande-id>6952fb62-fcb7-4b47-88cc-a57b9231776a</ns2:meddelande-id><ns2:skickatTidpunkt>2020-06-10T12:51:49</ns2:skickatTidpunkt><ns2:intygs-id><ns3:root>TSTNMT2321000156-1077</ns3:root><ns3:extension>28a02dae-0d16-4745-8ecf-4cea05cd637d</ns3:extension></ns2:intygs-id><ns2:patientPerson-id><ns3:root>1.2.752.129.2.1.3.1</ns3:root><ns3:extension>191212121212</ns3:extension></ns2:patientPerson-id><ns2:logiskAdressMottagare>FK</ns2:logiskAdressMottagare><ns2:amne><ns3:code>KONTKT</ns3:code><ns3:codeSystem>ffa59d8f-8d7e-46ae-ac9e-31804e8e8499</ns3:codeSystem><ns3:displayName>Kontakt</ns3:displayName></ns2:amne><ns2:meddelande>Kan ni ta kontakt?</ns2:meddelande><ns2:skickatAv><ns4:personal-id><ns3:root>1.2.752.129.2.1.4.1</ns3:root><ns3:extension>TSTNMT2321000156-1079</ns3:extension></ns4:personal-id><ns4:fullstandigtNamn>Arnold Johansson</ns4:fullstandigtNamn><ns4:forskrivarkod>0000000</ns4:forskrivarkod><ns4:enhet><ns4:enhets-id><ns3:root>1.2.752.129.2.1.4.1</ns3:root><ns3:extension>TSTNMT2321000156-1077</ns3:extension></ns4:enhets-id><ns4:arbetsplatskod><ns3:root>1.2.752.29.4.71</ns3:root><ns3:extension>1234567890</ns3:extension></ns4:arbetsplatskod><ns4:enhetsnamn>NMT vg3 ve1</ns4:enhetsnamn><ns4:postadress>NMT gata 3</ns4:postadress><ns4:postnummer>12345</ns4:postnummer><ns4:postort>Testhult</ns4:postort><ns4:telefonnummer>0101112131416</ns4:telefonnummer><ns4:epost>enhet3@webcert.invalid.se</ns4:epost><ns4:vardgivare><ns4:vardgivare-id><ns3:root>1.2.752.129.2.1.4.1</ns3:root><ns3:extension>TSTNMT2321000156-102Q</ns3:extension></ns4:vardgivare-id><ns4:vardgivarnamn>NMT vg3</ns4:vardgivarnamn></ns4:vardgivare></ns4:enhet></ns2:skickatAv></ns2:SendMessageToRecipient>";
    private static LocalDateTime QUESTION_TO_RECIPIENT_TIMESTAMP = LocalDateTime.parse("2020-06-10T12:51:49");

    private static String ANSWER_TO_CARE_MESSAGE_ID = "Messs15";
    private static String ANSWER_TO_CARE_SUBJECT = "KONTKT";
    private static String ANSWER_TO_CARE_LOGICAL_ADDRESS = "TSTNMT2321000156-1077";
    private static String ANSWER_TO_CARE_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:SendMessageToCare xmlns:ns6=\"urn:riv:clinicalprocess:healthcond:certificate:3.4\" xmlns:ns5=\"urn:riv:clinicalprocess:healthcond:certificate:3.2\" xmlns:ns2=\"urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:2\" xmlns:ns4=\"urn:riv:clinicalprocess:healthcond:certificate:3\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:ns3=\"urn:riv:clinicalprocess:healthcond:certificate:types:3\"><ns2:meddelande-id>Messs15</ns2:meddelande-id><ns2:referens-id>referens</ns2:referens-id><ns2:skickatTidpunkt>2019-07-13T17:23:00</ns2:skickatTidpunkt><ns2:intygs-id><ns3:root></ns3:root><ns3:extension>28a02dae-0d16-4745-8ecf-4cea05cd637d</ns3:extension></ns2:intygs-id><ns2:patientPerson-id><ns3:root>1.2.752.129.2.1.3.1</ns3:root><ns3:extension>191212121212</ns3:extension></ns2:patientPerson-id><ns2:logiskAdressMottagare>TSTNMT2321000156-1077</ns2:logiskAdressMottagare><ns2:amne><ns3:code>KONTKT</ns3:code><ns3:codeSystem>ffa59d8f-8d7e-46ae-ac9e-31804e8e8499</ns3:codeSystem><ns3:displayName>Kontakt</ns3:displayName></ns2:amne><ns2:rubrik>Svar från FK</ns2:rubrik><ns2:meddelande>Det ordnar vi vettu! Har hängt med MC-gäng vettu!</ns2:meddelande><ns2:svarPa><ns4:meddelande-id>6952fb62-fcb7-4b47-88cc-a57b9231776a</ns4:meddelande-id><ns4:referens-id></ns4:referens-id></ns2:svarPa><ns2:skickatAv><ns2:part><ns3:code>FKASSA</ns3:code><ns3:codeSystem>769bb12b-bd9f-4203-a5cd-fd14f2eb3b80</ns3:codeSystem><ns3:displayName>TRANSP</ns3:displayName></ns2:part><ns2:kontaktInfo>Nordic Medtest AB</ns2:kontaktInfo></ns2:skickatAv></ns2:SendMessageToCare>";
    private static LocalDateTime ANSWER_TO_CARE_TIMESTAMP = LocalDateTime.parse("2019-07-13T17:23:00");

    private static String QUESTION_TO_CARE_MESSAGE_ID = "ADMINFRÅGA__2";
    private static String QUESTION_TO_CARE_SUBJECT = "OVRIGT";
    private static String QUESTION_TO_CARE_LOGICAL_ADDRESS = "TSTNMT2321000156-1077";
    private static String QUESTION_TO_CARE_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:SendMessageToCare xmlns:ns6=\"urn:riv:clinicalprocess:healthcond:certificate:3.4\" xmlns:ns5=\"urn:riv:clinicalprocess:healthcond:certificate:3.2\" xmlns:ns2=\"urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:2\" xmlns:ns4=\"urn:riv:clinicalprocess:healthcond:certificate:3\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:ns3=\"urn:riv:clinicalprocess:healthcond:certificate:types:3\"><ns2:meddelande-id>ADMINFRÅGA__2</ns2:meddelande-id><ns2:skickatTidpunkt>2016-07-13T19:23:00</ns2:skickatTidpunkt><ns2:intygs-id><ns3:root></ns3:root><ns3:extension>28a02dae-0d16-4745-8ecf-4cea05cd637d</ns3:extension></ns2:intygs-id><ns2:patientPerson-id><ns3:root>1.2.752.129.2.1.3.1</ns3:root><ns3:extension>191212121212</ns3:extension></ns2:patientPerson-id><ns2:logiskAdressMottagare>TSTNMT2321000156-1077</ns2:logiskAdressMottagare><ns2:amne><ns3:code>OVRIGT</ns3:code><ns3:codeSystem>ffa59d8f-8d7e-46ae-ac9e-31804e8e8499</ns3:codeSystem></ns2:amne><ns2:rubrik>Rubrik från åäöÅÄÖ Rubrik från åäöÅÄÖ Rubrik från åäöÅÄÖ Rubrik från åäöÅÄÖ</ns2:rubrik><ns2:meddelande>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras vitae malesuada dolor. Mauris at diam lectus. Nam semper accumsan ante. Quisque volutpat erat eget iaculis bibendum. Cras interdum pulvinar mauris, id feugiat purus molestie quis. Curabitur semper elit rhoncus, tempus urna quis, bibendum quam. Ut eget urna magna. Vivamus a pulvinar leo, sodales cursus ligula. Aenean massa augue, rhoncus a dolor sit amet, eleifend vestibulum justo. Donec eu luctus ipsum, sit amet pharetra mi. Suspendisse potenti. Proin porta lacus nisl, in vestibulum lacus auctor eu. Praesent pretium laoreet gravida.</ns2:meddelande><ns2:skickatAv><ns2:part><ns3:code>FKASSA</ns3:code><ns3:codeSystem>769bb12b-bd9f-4203-a5cd-fd14f2eb3b80</ns3:codeSystem></ns2:part></ns2:skickatAv><ns2:sistaDatumForSvar>2020-07-13</ns2:sistaDatumForSvar></ns2:SendMessageToCare>";
    private static LocalDateTime QUESTION_TO_CARE_TIMESTAMP = LocalDateTime.parse("2016-07-13T19:23:00");

    private static String ANSWER_TO_RECIPIENT_MESSAGE_ID = "5c36a1c7-6b6c-4931-afce-9211bf961f0b";
    private static String ANSWER_TO_RECIPIENT_SUBJECT = "OVRIGT";
    private static String ANSWER_TO_RECIPIENT_LOGICAL_ADDRESS = "FK";
    private static String ANSWER_TO_RECIPIENT_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:SendMessageToRecipient xmlns:ns6=\"urn:riv:clinicalprocess:healthcond:certificate:3.4\" xmlns:ns5=\"urn:riv:clinicalprocess:healthcond:certificate:3.2\" xmlns:ns2=\"urn:riv:clinicalprocess:healthcond:certificate:SendMessageToRecipientResponder:2\" xmlns:ns4=\"urn:riv:clinicalprocess:healthcond:certificate:3\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:ns3=\"urn:riv:clinicalprocess:healthcond:certificate:types:3\"><ns2:meddelande-id>5c36a1c7-6b6c-4931-afce-9211bf961f0b</ns2:meddelande-id><ns2:skickatTidpunkt>2020-06-10T14:16:58</ns2:skickatTidpunkt><ns2:intygs-id><ns3:root>TSTNMT2321000156-1077</ns3:root><ns3:extension>28a02dae-0d16-4745-8ecf-4cea05cd637d</ns3:extension></ns2:intygs-id><ns2:patientPerson-id><ns3:root>1.2.752.129.2.1.3.1</ns3:root><ns3:extension>191212121212</ns3:extension></ns2:patientPerson-id><ns2:logiskAdressMottagare>FK</ns2:logiskAdressMottagare><ns2:amne><ns3:code>OVRIGT</ns3:code><ns3:codeSystem>ffa59d8f-8d7e-46ae-ac9e-31804e8e8499</ns3:codeSystem><ns3:displayName>Övrigt</ns3:displayName></ns2:amne><ns2:rubrik>Rubrik från åäöÅÄÖ Rubrik från åäöÅÄÖ Rubrik från åäöÅÄÖ Rubrik från åäöÅÄÖ</ns2:rubrik><ns2:meddelande>Svar på fråga från FK!</ns2:meddelande><ns2:svarPa><ns4:meddelande-id>ADMINFRÅGA__2</ns4:meddelande-id></ns2:svarPa><ns2:skickatAv><ns4:personal-id><ns3:root>1.2.752.129.2.1.4.1</ns3:root><ns3:extension>TSTNMT2321000156-1079</ns3:extension></ns4:personal-id><ns4:fullstandigtNamn>Arnold Johansson</ns4:fullstandigtNamn><ns4:forskrivarkod>0000000</ns4:forskrivarkod><ns4:enhet><ns4:enhets-id><ns3:root>1.2.752.129.2.1.4.1</ns3:root><ns3:extension>TSTNMT2321000156-1077</ns3:extension></ns4:enhets-id><ns4:arbetsplatskod><ns3:root>1.2.752.29.4.71</ns3:root><ns3:extension>1234567890</ns3:extension></ns4:arbetsplatskod><ns4:enhetsnamn>NMT vg3 ve1</ns4:enhetsnamn><ns4:postadress>NMT gata 3</ns4:postadress><ns4:postnummer>12345</ns4:postnummer><ns4:postort>Testhult</ns4:postort><ns4:telefonnummer>0101112131416</ns4:telefonnummer><ns4:epost>enhet3@webcert.invalid.se</ns4:epost><ns4:vardgivare><ns4:vardgivare-id><ns3:root>1.2.752.129.2.1.4.1</ns3:root><ns3:extension>TSTNMT2321000156-102Q</ns3:extension></ns4:vardgivare-id><ns4:vardgivarnamn>NMT vg3</ns4:vardgivarnamn></ns4:vardgivare></ns4:enhet></ns2:skickatAv></ns2:SendMessageToRecipient>";
    private static LocalDateTime ANSWER_TO_RECIPIENT_TIMESTAMP = LocalDateTime.parse("2020-06-10T14:16:58");

    private static String COMPLEMENT_TO_CARE_MESSAGE_ID = "GurraKOMPLETERING_1";
    private static String COMPLEMENT_TO_CARE_SUBJECT = "KOMPLT";
    private static String COMPLEMENT_TO_CARE_LOGICAL_ADDRESS = "TSTNMT2321000156-1077";
    private static String COMPLEMENT_TO_CARE_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:SendMessageToCare xmlns:ns6=\"urn:riv:clinicalprocess:healthcond:certificate:3.4\" xmlns:ns5=\"urn:riv:clinicalprocess:healthcond:certificate:3.2\" xmlns:ns2=\"urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:2\" xmlns:ns4=\"urn:riv:clinicalprocess:healthcond:certificate:3\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:ns3=\"urn:riv:clinicalprocess:healthcond:certificate:types:3\"><ns2:meddelande-id>GurraKOMPLETERING_1</ns2:meddelande-id><ns2:skickatTidpunkt>2020-05-14T12:23:00</ns2:skickatTidpunkt><ns2:intygs-id><ns3:root></ns3:root><ns3:extension>28a02dae-0d16-4745-8ecf-4cea05cd637d</ns3:extension></ns2:intygs-id><ns2:patientPerson-id><ns3:root>1.2.752.129.2.1.3.1</ns3:root><ns3:extension>191212121212</ns3:extension></ns2:patientPerson-id><ns2:logiskAdressMottagare>TSTNMT2321000156-1077</ns2:logiskAdressMottagare><ns2:amne><ns3:code>KOMPLT</ns3:code><ns3:codeSystem>ffa59d8f-8d7e-46ae-ac9e-31804e8e8499</ns3:codeSystem></ns2:amne><ns2:rubrik>Komplettering_Anders</ns2:rubrik><ns2:meddelande>Vi behöver komplettering på detta intyg</ns2:meddelande><ns2:skickatAv><ns2:part><ns3:code>FKASSA</ns3:code><ns3:codeSystem>769bb12b-bd9f-4203-a5cd-fd14f2eb3b80</ns3:codeSystem></ns2:part></ns2:skickatAv><ns2:komplettering><ns2:frage-id>1</ns2:frage-id><ns2:instans>1</ns2:instans><ns2:text>Var god och komplettera denna nu!</ns2:text></ns2:komplettering><ns2:sistaDatumForSvar>2020-05-16</ns2:sistaDatumForSvar></ns2:SendMessageToCare>";
    private static LocalDateTime COMPLEMENT_TO_CARE_TIMESTAMP = LocalDateTime.parse("2020-05-14T12:23:00");

    private static String REMINDER_TO_CARE_MESSAGE_ID = "GurraGurra!!! STAGE_022";
    private static String REMINDER_TO_CARE_SUBJECT = "PAMINN";
    private static String REMINDER_TO_CARE_LOGICAL_ADDRESS = "TSTNMT2321000156-1077";
    private static String REMINDER_TO_CARE_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:SendMessageToCare xmlns:ns6=\"urn:riv:clinicalprocess:healthcond:certificate:3.4\" xmlns:ns5=\"urn:riv:clinicalprocess:healthcond:certificate:3.2\" xmlns:ns2=\"urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:2\" xmlns:ns4=\"urn:riv:clinicalprocess:healthcond:certificate:3\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:ns3=\"urn:riv:clinicalprocess:healthcond:certificate:types:3\"><ns2:meddelande-id>GurraGurra!!! STAGE_022</ns2:meddelande-id><ns2:skickatTidpunkt>2016-07-13T17:23:00</ns2:skickatTidpunkt><ns2:intygs-id><ns3:root></ns3:root><ns3:extension>28a02dae-0d16-4745-8ecf-4cea05cd637d</ns3:extension></ns2:intygs-id><ns2:patientPerson-id><ns3:root>1.2.752.129.2.1.3.1</ns3:root><ns3:extension>191212121212</ns3:extension></ns2:patientPerson-id><ns2:logiskAdressMottagare>TSTNMT2321000156-1077</ns2:logiskAdressMottagare><ns2:amne><ns3:code>PAMINN</ns3:code><ns3:codeSystem>ffa59d8f-8d7e-46ae-ac9e-31804e8e8499</ns3:codeSystem></ns2:amne><ns2:rubrik>Fråga</ns2:rubrik><ns2:meddelande>assad</ns2:meddelande><ns2:paminnelseMeddelande-id>ADMINFRÅGA__2</ns2:paminnelseMeddelande-id><ns2:skickatAv><ns2:part><ns3:code>FKASSA</ns3:code><ns3:codeSystem>769bb12b-bd9f-4203-a5cd-fd14f2eb3b80</ns3:codeSystem></ns2:part></ns2:skickatAv></ns2:SendMessageToCare>";
    private static LocalDateTime REMINDER_TO_CARE_TIMESTAMP = LocalDateTime.parse("2016-07-13T17:23:00");

    @Test
    public void testImportNeededIfCertificateMissingInWC() {
        final var certificateId = "certificateId";

        doReturn(Optional.empty()).when(draftRepository).findById(certificateId);

        final var actualIsImportNeeded = messageImportService.isImportNeeded(certificateId);

        assertTrue(actualIsImportNeeded);
    }

    @Test
    public void testImportNotNeededIfCertificateExistsInWC() {
        final var certificateId = "certificateId";

        doReturn(Optional.of(new Utkast())).when(draftRepository).findById(certificateId);

        final var actualIsImportNeeded = messageImportService.isImportNeeded(certificateId);

        assertFalse(actualIsImportNeeded);
    }

    @Test
    public void testImportMessagesNoMessagesInIT() {
        final var certificateId = "certificateId";

        doReturn(Collections.emptyList()).when(itIntegrationService).findMessagesByCertificateId(certificateId);

        messageImportService.importMessages(certificateId);

        verify(messageRepository, never()).save(any());
    }

    @Test
    public void testImportMessagesNoMessageAfterExclude() {
        final var certificateId = "certificateId";
        final var messageId = "messageId";
        final var messageContent = "messageContent";
        final var subject = "subject";
        final var logicalAddress = "logicalAddress";
        final var timestamp = LocalDateTime.now();

        final var messageFromIT = MessageFromIT.create(certificateId, messageId, messageContent, subject, logicalAddress, timestamp);

        doReturn(Arrays.asList(messageFromIT)).when(itIntegrationService).findMessagesByCertificateId(certificateId);

        messageImportService.importMessages(certificateId, messageId);

        verify(messageRepository, never()).save(any());
    }

    @Test
    public void testImportMessagesNoMessagesNotImported() {
        final var certificateId = "certificateId";
        final var messageId = "messageId";
        final var messageContent = "messageContent";
        final var subject = "subject";
        final var logicalAddress = "logicalAddress";
        final var timestamp = LocalDateTime.now();

        final var messageFromIT = MessageFromIT.create(certificateId, messageId, messageContent, subject, logicalAddress, timestamp);

        doReturn(Arrays.asList(messageFromIT)).when(itIntegrationService).findMessagesByCertificateId(certificateId);
        doReturn(Arrays.asList(messageId)).when(messageRepository).findMeddelandeIdByMeddelandeId(anyList());

        messageImportService.importMessages(certificateId);

        verify(messageRepository, never()).save(any());
    }

    @Test
    public void testImportMessagesFailedToCallIT() {
        final var certificateId = "certificateId";

        doThrow(new RuntimeException()).when(itIntegrationService).findMessagesByCertificateId(certificateId);

        messageImportService.importMessages(certificateId);

        verify(messageRepository, never()).save(any());
    }

    @Test
    public void testImportMessagesSendQuestionToRecipient() {
        final var certificateId = CERTIFICATE_ID;
        final var messageId = QUESTION_TO_RECIPIENT_MESSAGE_ID;
        final var messageContent = QUESTION_TO_RECIPIENT_CONTENT;
        final var subject = QUESTION_TO_RECIPIENT_SUBJECT;
        final var logicalAddress = QUESTION_TO_RECIPIENT_LOGICAL_ADDRESS;
        final var timestamp = QUESTION_TO_RECIPIENT_TIMESTAMP;

        final var messageFromIT = MessageFromIT.create(certificateId, messageId, messageContent, subject, logicalAddress, timestamp);

        final var certificateHolder = mock(IntygContentHolder.class);
        doReturn(getCertificate()).when(certificateHolder).getUtlatande();

        doReturn(Arrays.asList(messageFromIT)).when(itIntegrationService).findMessagesByCertificateId(certificateId);
        doReturn(Collections.emptyList()).when(messageRepository).findMeddelandeIdByMeddelandeId(anyList());
        doReturn(certificateHolder).when(certificateService).fetchIntygDataForInternalUse(CERTIFICATE_ID, true);
        doAnswer(returnsFirstArg()).when(messageRepository).save(any());

        messageImportService.importMessages(certificateId);

        final var captor = ArgumentCaptor.forClass(Arende.class);
        verify(messageRepository, times(1)).save(captor.capture());

        final var importedMessage = captor.getValue();
        assertEquals(QUESTION_TO_RECIPIENT_MESSAGE_ID, importedMessage.getMeddelandeId());

        verify(monitoringLogService, times(1)).logMessageImported(CERTIFICATE_ID, QUESTION_TO_RECIPIENT_MESSAGE_ID,
            CARE_PROVIDER_ID, CARE_UNIT_ID, "SendMessageToRecipient");
    }

    @Test
    public void testImportMessagesSendAnswerToCare() {
        final var certificateId = CERTIFICATE_ID;
        final var messageId = ANSWER_TO_CARE_MESSAGE_ID;
        final var messageContent = ANSWER_TO_CARE_CONTENT;
        final var subject = ANSWER_TO_CARE_SUBJECT;
        final var logicalAddress = ANSWER_TO_CARE_LOGICAL_ADDRESS;
        final var timestamp = ANSWER_TO_CARE_TIMESTAMP;

        final var questionBeingAnswered = mock(Arende.class);

        final var messageFromIT = MessageFromIT.create(certificateId, messageId, messageContent, subject, logicalAddress, timestamp);

        final var certificateHolder = mock(IntygContentHolder.class);
        doReturn(getCertificate()).when(certificateHolder).getUtlatande();

        doReturn(Arrays.asList(messageFromIT)).when(itIntegrationService).findMessagesByCertificateId(certificateId);
        doReturn(Collections.emptyList()).when(messageRepository).findMeddelandeIdByMeddelandeId(anyList());
        doReturn(certificateHolder).when(certificateService).fetchIntygDataForInternalUse(CERTIFICATE_ID, true);
        doAnswer(returnsFirstArg()).when(messageRepository).save(any());
        doReturn(questionBeingAnswered).when(messageRepository).findOneByMeddelandeId(QUESTION_TO_RECIPIENT_MESSAGE_ID);

        messageImportService.importMessages(certificateId);

        final var captor = ArgumentCaptor.forClass(Arende.class);
        verify(messageRepository, times(1)).save(captor.capture());

        final var importedMessage = captor.getValue();
        assertEquals(ANSWER_TO_CARE_MESSAGE_ID, importedMessage.getMeddelandeId());

        verify(questionBeingAnswered, times(1)).setSenasteHandelse(ANSWER_TO_CARE_TIMESTAMP);
        verify(questionBeingAnswered, times(1)).setStatus(Status.CLOSED);

        verify(monitoringLogService, times(1)).logMessageImported(CERTIFICATE_ID, ANSWER_TO_CARE_MESSAGE_ID,
            CARE_PROVIDER_ID, CARE_UNIT_ID, "SendMessageToCare - Answer");
    }

    @Test
    public void testImportMessagesSendQuestionToCare() {
        final var certificateId = CERTIFICATE_ID;
        final var messageId = QUESTION_TO_CARE_MESSAGE_ID;
        final var messageContent = QUESTION_TO_CARE_CONTENT;
        final var subject = QUESTION_TO_CARE_SUBJECT;
        final var logicalAddress = QUESTION_TO_CARE_LOGICAL_ADDRESS;
        final var timestamp = QUESTION_TO_CARE_TIMESTAMP;

        final var messageFromIT = MessageFromIT.create(certificateId, messageId, messageContent, subject, logicalAddress, timestamp);

        final var certificateHolder = mock(IntygContentHolder.class);
        doReturn(getCertificate()).when(certificateHolder).getUtlatande();

        doReturn(Arrays.asList(messageFromIT)).when(itIntegrationService).findMessagesByCertificateId(certificateId);
        doReturn(Collections.emptyList()).when(messageRepository).findMeddelandeIdByMeddelandeId(anyList());
        doReturn(certificateHolder).when(certificateService).fetchIntygDataForInternalUse(CERTIFICATE_ID, true);
        doAnswer(returnsFirstArg()).when(messageRepository).save(any());

        messageImportService.importMessages(certificateId);

        final var captor = ArgumentCaptor.forClass(Arende.class);
        verify(messageRepository, times(1)).save(captor.capture());

        final var importedMessage = captor.getValue();
        assertEquals(QUESTION_TO_CARE_MESSAGE_ID, importedMessage.getMeddelandeId());

        verify(monitoringLogService, times(1)).logMessageImported(CERTIFICATE_ID, QUESTION_TO_CARE_MESSAGE_ID,
            CARE_PROVIDER_ID, CARE_UNIT_ID, "SendMessageToCare");
    }

    @Test
    public void testImportMessagesSendAnswerToRecipient() {
        final var certificateId = CERTIFICATE_ID;
        final var messageId = ANSWER_TO_RECIPIENT_MESSAGE_ID;
        final var messageContent = ANSWER_TO_RECIPIENT_CONTENT;
        final var subject = ANSWER_TO_RECIPIENT_SUBJECT;
        final var logicalAddress = ANSWER_TO_RECIPIENT_LOGICAL_ADDRESS;
        final var timestamp = ANSWER_TO_RECIPIENT_TIMESTAMP;

        final var questionBeingAnswered = mock(Arende.class);

        final var messageFromIT = MessageFromIT.create(certificateId, messageId, messageContent, subject, logicalAddress, timestamp);

        final var certificateHolder = mock(IntygContentHolder.class);
        doReturn(getCertificate()).when(certificateHolder).getUtlatande();

        doReturn(Arrays.asList(messageFromIT)).when(itIntegrationService).findMessagesByCertificateId(certificateId);
        doReturn(Collections.emptyList()).when(messageRepository).findMeddelandeIdByMeddelandeId(anyList());
        doReturn(certificateHolder).when(certificateService).fetchIntygDataForInternalUse(CERTIFICATE_ID, true);
        doAnswer(returnsFirstArg()).when(messageRepository).save(any());
        doReturn(questionBeingAnswered).when(messageRepository).findOneByMeddelandeId(QUESTION_TO_CARE_MESSAGE_ID);

        messageImportService.importMessages(certificateId);

        final var captor = ArgumentCaptor.forClass(Arende.class);
        verify(messageRepository, times(1)).save(captor.capture());

        final var importedMessage = captor.getValue();
        assertEquals(ANSWER_TO_RECIPIENT_MESSAGE_ID, importedMessage.getMeddelandeId());

        verify(questionBeingAnswered, times(1)).setSenasteHandelse(ANSWER_TO_RECIPIENT_TIMESTAMP);
        verify(questionBeingAnswered, times(1)).setStatus(Status.CLOSED);

        verify(monitoringLogService, times(1)).logMessageImported(CERTIFICATE_ID, ANSWER_TO_RECIPIENT_MESSAGE_ID,
            CARE_PROVIDER_ID, CARE_UNIT_ID, "SendMessageToRecipient - Answer");
    }

    @Test
    public void testImportMessagesSendComplementToCare() {
        final var certificateId = CERTIFICATE_ID;
        final var messageId = COMPLEMENT_TO_CARE_MESSAGE_ID;
        final var messageContent = COMPLEMENT_TO_CARE_CONTENT;
        final var subject = COMPLEMENT_TO_CARE_SUBJECT;
        final var logicalAddress = COMPLEMENT_TO_CARE_LOGICAL_ADDRESS;
        final var timestamp = COMPLEMENT_TO_CARE_TIMESTAMP;

        final var messageFromIT = MessageFromIT.create(certificateId, messageId, messageContent, subject, logicalAddress, timestamp);

        final var certificateHolder = mock(IntygContentHolder.class);
        doReturn(getCertificate()).when(certificateHolder).getUtlatande();

        final var relations = mock(Relations.class);
        doReturn(relations).when(certificateHolder).getRelations();
        final var latestChildRelations = mock(FrontendRelations.class);
        final var webcertCertificateRelation = mock(WebcertCertificateRelation.class);
        doReturn(latestChildRelations).when(relations).getLatestChildRelations();
        doReturn(webcertCertificateRelation).when(latestChildRelations).getComplementedByIntyg();
        doReturn(false).when(webcertCertificateRelation).isMakulerat();

        doReturn(Arrays.asList(messageFromIT)).when(itIntegrationService).findMessagesByCertificateId(certificateId);
        doReturn(Collections.emptyList()).when(messageRepository).findMeddelandeIdByMeddelandeId(anyList());
        doReturn(certificateHolder).when(certificateService).fetchIntygDataForInternalUse(CERTIFICATE_ID, true);
        doAnswer(returnsFirstArg()).when(messageRepository).save(any());

        messageImportService.importMessages(certificateId);

        final var captor = ArgumentCaptor.forClass(Arende.class);
        verify(messageRepository, times(1)).save(captor.capture());

        final var importedMessage = captor.getValue();
        assertEquals(COMPLEMENT_TO_CARE_MESSAGE_ID, importedMessage.getMeddelandeId());

        verify(monitoringLogService, times(1)).logMessageImported(CERTIFICATE_ID, COMPLEMENT_TO_CARE_MESSAGE_ID,
            CARE_PROVIDER_ID, CARE_UNIT_ID, "SendMessageToCare");
    }

    @Test
    public void testImportMessagesSendComplementToCareWithoutCertificate() {
        final var certificateId = CERTIFICATE_ID;
        final var messageId = COMPLEMENT_TO_CARE_MESSAGE_ID;
        final var messageContent = COMPLEMENT_TO_CARE_CONTENT;
        final var subject = COMPLEMENT_TO_CARE_SUBJECT;
        final var logicalAddress = COMPLEMENT_TO_CARE_LOGICAL_ADDRESS;
        final var timestamp = COMPLEMENT_TO_CARE_TIMESTAMP;

        final var messageFromIT = MessageFromIT.create(certificateId, messageId, messageContent, subject, logicalAddress, timestamp);

        final var certificateHolder = mock(IntygContentHolder.class);
        doReturn(getCertificate()).when(certificateHolder).getUtlatande();

        final var relations = mock(Relations.class);
        doReturn(relations).when(certificateHolder).getRelations();
        final var latestChildRelations = mock(FrontendRelations.class);
        final var webcertCertificateRelation = mock(WebcertCertificateRelation.class);
        doReturn(latestChildRelations).when(relations).getLatestChildRelations();
        doReturn(webcertCertificateRelation).when(latestChildRelations).getComplementedByIntyg();
        doReturn(true).when(webcertCertificateRelation).isMakulerat();

        doReturn(Arrays.asList(messageFromIT)).when(itIntegrationService).findMessagesByCertificateId(certificateId);
        doReturn(Collections.emptyList()).when(messageRepository).findMeddelandeIdByMeddelandeId(anyList());
        doReturn(certificateHolder).when(certificateService).fetchIntygDataForInternalUse(CERTIFICATE_ID, true);
        doAnswer(returnsFirstArg()).when(messageRepository).save(any());

        messageImportService.importMessages(certificateId);

        final var captor = ArgumentCaptor.forClass(Arende.class);
        verify(messageRepository, times(1)).save(captor.capture());

        final var importedMessage = captor.getValue();
        assertEquals(COMPLEMENT_TO_CARE_MESSAGE_ID, importedMessage.getMeddelandeId());

        verify(monitoringLogService, times(1)).logMessageImported(CERTIFICATE_ID, COMPLEMENT_TO_CARE_MESSAGE_ID,
            CARE_PROVIDER_ID, CARE_UNIT_ID, "SendMessageToCare");
    }

    @Test
    public void testImportMessagesSendReminderToCare() {
        final var certificateId = CERTIFICATE_ID;
        final var messageId = REMINDER_TO_CARE_MESSAGE_ID;
        final var messageContent = REMINDER_TO_CARE_CONTENT;
        final var subject = REMINDER_TO_CARE_SUBJECT;
        final var logicalAddress = REMINDER_TO_CARE_LOGICAL_ADDRESS;
        final var timestamp = REMINDER_TO_CARE_TIMESTAMP;

        final var questionBeingReminded = mock(Arende.class);

        final var messageFromIT = MessageFromIT.create(certificateId, messageId, messageContent, subject, logicalAddress, timestamp);

        final var certificateHolder = mock(IntygContentHolder.class);
        doReturn(getCertificate()).when(certificateHolder).getUtlatande();

        doReturn(Arrays.asList(messageFromIT)).when(itIntegrationService).findMessagesByCertificateId(certificateId);
        doReturn(Collections.emptyList()).when(messageRepository).findMeddelandeIdByMeddelandeId(anyList());
        doReturn(certificateHolder).when(certificateService).fetchIntygDataForInternalUse(CERTIFICATE_ID, true);
        doAnswer(returnsFirstArg()).when(messageRepository).save(any());
        doReturn(questionBeingReminded).when(messageRepository).findOneByMeddelandeId(QUESTION_TO_CARE_MESSAGE_ID);

        messageImportService.importMessages(certificateId);

        final var captor = ArgumentCaptor.forClass(Arende.class);
        verify(messageRepository, times(1)).save(captor.capture());

        final var importedMessage = captor.getValue();
        assertEquals(REMINDER_TO_CARE_MESSAGE_ID, importedMessage.getMeddelandeId());

        verify(questionBeingReminded, times(1)).setSenasteHandelse(REMINDER_TO_CARE_TIMESTAMP);

        verify(monitoringLogService, times(1)).logMessageImported(CERTIFICATE_ID, REMINDER_TO_CARE_MESSAGE_ID,
            CARE_PROVIDER_ID, CARE_UNIT_ID, "SendMessageToCare");
    }

    private Utlatande getCertificate() {
        final var certificate = mock(Utlatande.class);
        doReturn(CERTIFICATE_TYPE).when(certificate).getTyp();
        final var basicData = mock(GrundData.class);
        doReturn(basicData).when(certificate).getGrundData();
        final var createdBy = mock(HoSPersonal.class);
        doReturn(createdBy).when(basicData).getSkapadAv();
        final var careUnit = mock(Vardenhet.class);
        doReturn(careUnit).when(createdBy).getVardenhet();
        final var careProvider = mock(Vardgivare.class);
        doReturn(careProvider).when(careUnit).getVardgivare();
        doReturn(CARE_PROVIDER_ID).when(careProvider).getVardgivarid();
        doReturn(CARE_UNIT_ID).when(careUnit).getEnhetsid();

        return certificate;
    }

}
