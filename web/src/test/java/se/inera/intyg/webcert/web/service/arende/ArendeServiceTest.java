/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.arende;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.integration.hsa.services.HsaEmployeeService;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserDetails;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.converter.ArendeViewConverter;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderException;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationEvent;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArendeServiceTest extends AuthoritiesConfigurationTestSetup {

    private static final LocalDateTime JANUARY = LocalDateTime.parse("2013-01-12T11:22:11");
    private static final LocalDateTime FEBRUARY = LocalDateTime.parse("2013-02-12T11:22:11");
    private static final LocalDateTime DECEMBER_YEAR_9999 = LocalDateTime.parse("9999-12-11T10:22:00");
    private static final Personnummer PATIENT_ID = new Personnummer("19121212-1212");
    private static final String INTYG_ID = "intyg-1";
    private static final String INTYG_TYP = "luse";
    private static final String ENHET_ID = "enhet";
    private static final String MEDDELANDE_ID = "meddelandeId";

    @Mock
    private ArendeRepository arendeRepository;

    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private WebCertUserService webcertUserService;

    @Mock
    private AuthoritiesHelper authoritiesHelper;

    @Mock
    private MonitoringLogService monitoringLog;

    @Spy
    private ArendeViewConverter arendeViewConverter;

    @Mock
    private HsaEmployeeService hsaEmployeeService;

    @Mock
    private FragaSvarService fragaSvarService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CertificateSenderService certificateSenderService;

    @InjectMocks
    private ArendeServiceImpl service;

    @Before
    public void setUp() {
        // DateTimeUtils.setCurrentMillisFixed(FIXED_TIME_MILLIS);

        // always return the Arende that is saved
        when(arendeRepository.save(any(Arende.class))).thenAnswer(invocation -> (Arende) invocation.getArguments()[0]);

    }

    @After
    public void cleanUp() {
        // DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void testProcessIncomingMessage() throws WebCertServiceException {
        final String signeratAvName = "signeratAvName";

        Arende arende = new Arende();
        arende.setIntygsId(INTYG_ID);

        Utkast utkast = buildUtkast();
        utkast.getSkapadAv().setNamn(signeratAvName);
        when(utkastRepository.findOne(INTYG_ID)).thenReturn(utkast);

        Arende res = service.processIncomingMessage(arende);

        assertNotNull(res);
        assertNotNull(res.getTimestamp());
        assertNotNull(res.getSenasteHandelse());
        assertEquals(utkast.getSignatur().getSigneradAv(), res.getSigneratAv());
        assertEquals(signeratAvName, res.getSigneratAvName());

        verify(utkastRepository).findOne(INTYG_ID);
        verify(notificationService).sendNotificationForQuestionReceived(any(Arende.class));
    }

    @Test
    public void testProcessIncomingMessageSendsNotificationForQuestionReceived() throws WebCertServiceException {
        Arende arende = new Arende();
        arende.setIntygsId(INTYG_ID);

        Utkast utkast = buildUtkast();
        when(utkastRepository.findOne(INTYG_ID)).thenReturn(utkast);

        Arende res = service.processIncomingMessage(arende);

        assertNotNull(res);
        assertEquals(INTYG_ID, res.getIntygsId());

        verify(utkastRepository).findOne(INTYG_ID);
        verify(notificationService).sendNotificationForQuestionReceived(any(Arende.class));
    }

    @Test
    public void testProcessIncomingMessageSendsNotificationForAnswerRecieved() throws WebCertServiceException {
        final String frageid = "frageid";

        Arende fragearende = new Arende();

        Arende svararende = new Arende();
        svararende.setIntygsId(INTYG_ID);
        svararende.setSvarPaId(frageid);

        Utkast utkast = buildUtkast();
        when(utkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(arendeRepository.findOneByMeddelandeId(eq(frageid))).thenReturn(fragearende);

        Arende res = service.processIncomingMessage(svararende);

        assertNotNull(res);
        assertEquals(INTYG_ID, res.getIntygsId());

        verify(arendeRepository).findOneByMeddelandeId(eq(frageid));
        verify(arendeRepository, times(2)).save(any(Arende.class));
        verify(notificationService).sendNotificationForAnswerRecieved(any(Arende.class));
    }

    @Test
    public void testProcessIncomingMessageSendsNotificationForQuestionRecievedIfPaminnelse() throws WebCertServiceException {
        final String paminnelseMeddelandeId = "paminnelseMeddelandeId";

        Arende arende = new Arende();
        arende.setIntygsId(INTYG_ID);
        arende.setPaminnelseMeddelandeId(paminnelseMeddelandeId);

        Utkast utkast = buildUtkast();
        when(utkastRepository.findOne(INTYG_ID)).thenReturn(utkast);

        Arende res = service.processIncomingMessage(arende);

        assertNotNull(res);
        assertEquals(INTYG_ID, res.getIntygsId());

        verify(utkastRepository).findOne(INTYG_ID);
        verify(notificationService).sendNotificationForQuestionReceived(any(Arende.class));
    }

    @Test
    public void testProcessIncomingMessageUpdatingRelatedSvar() throws WebCertServiceException {
        final String frageid = "frageid";

        Arende fragearende = new Arende();

        Arende svararende = new Arende();
        svararende.setIntygsId(INTYG_ID);
        svararende.setSvarPaId(frageid);

        Utkast utkast = buildUtkast();
        utkast.setIntygsTyp("intygstyp");
        utkast.setEnhetsId(ENHET_ID);

        when(utkastRepository.findOne(eq(INTYG_ID))).thenReturn(utkast);
        when(arendeRepository.findOneByMeddelandeId(eq(frageid))).thenReturn(fragearende);

        Arende res = service.processIncomingMessage(svararende);
        assertEquals(Status.ANSWERED, res.getStatus());
        assertNotNull(res.getSenasteHandelse());

        verify(arendeRepository).findOneByMeddelandeId(eq(frageid));
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository, times(2)).save(arendeCaptor.capture());

        Arende updatedQuestion = arendeCaptor.getAllValues().get(1);
        assertNotNull(updatedQuestion.getSenasteHandelse());
        assertEquals(Status.ANSWERED, updatedQuestion.getStatus());
        verify(notificationService, only()).sendNotificationForAnswerRecieved(any(Arende.class));
    }

    @Test
    public void testProcessIncomingMessageUpdatingRelatedPaminnelse() throws WebCertServiceException {
        final String paminnelseid = "paminnelseid";

        Arende paminnelse = new Arende();

        Arende svararende = new Arende();
        svararende.setIntygsId(INTYG_ID);
        svararende.setPaminnelseMeddelandeId(paminnelseid);

        Utkast utkast = buildUtkast();
        utkast.setIntygsTyp("intygstyp");
        utkast.setEnhetsId(ENHET_ID);

        when(utkastRepository.findOne(eq(INTYG_ID))).thenReturn(utkast);
        when(arendeRepository.findOneByMeddelandeId(eq(paminnelseid))).thenReturn(paminnelse);

        Arende res = service.processIncomingMessage(svararende);
        assertNotNull(res.getSenasteHandelse());

        verify(arendeRepository).findOneByMeddelandeId(eq(paminnelseid));
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository, times(2)).save(arendeCaptor.capture());

        Arende updatedQuestion = arendeCaptor.getAllValues().get(1);
        assertNotNull(updatedQuestion.getSenasteHandelse());
        verify(notificationService, only()).sendNotificationForQuestionReceived(any(Arende.class));
    }

    @Test
    public void testProcessIncomingMessageCertificateNotFound() {
        when(utkastRepository.findOne(anyString())).thenReturn(null);
        try {
            service.processIncomingMessage(new Arende());
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, e.getErrorCode());
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyZeroInteractions(notificationService);
        }
    }

    @Test
    public void testProcessIncomingMessageCertificateNotSigned() {
        when(utkastRepository.findOne(anyString())).thenReturn(new Utkast());
        try {
            service.processIncomingMessage(new Arende());
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.INVALID_STATE, e.getErrorCode());
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyZeroInteractions(notificationService);
        }
    }

    @Test
    public void testProcessIncomingMessageMEDDELANDE_IDNotUnique() {
        when(arendeRepository.findOneByMeddelandeId(anyString())).thenReturn(new Arende());
        try {
            service.processIncomingMessage(new Arende());
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.INVALID_STATE, e.getErrorCode());
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyZeroInteractions(notificationService);
        }
    }

    @Test
    public void testProcessIncomingMessageThrowsExceptionIfCertificateIsRevoked() throws WebCertServiceException {
        Utkast utkast = buildUtkast();
        utkast.setAterkalladDatum(LocalDateTime.now());
        when(utkastRepository.findOne(anyString())).thenReturn(utkast);
        try {
            service.processIncomingMessage(new Arende());
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.CERTIFICATE_REVOKED, e.getErrorCode());
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyZeroInteractions(notificationService);
        }
    }


    @Test
    public void createQuestionTest() throws CertificateSenderException {
        LocalDateTime now = LocalDateTime.now();
        Utkast utkast = buildUtkast();
        when(utkastRepository.findOne(anyString())).thenReturn(utkast);
        when(webcertUserService.isAuthorizedForUnit(anyString(), anyBoolean())).thenReturn(true);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());
        Arende arende = new Arende();
        arende.setSenasteHandelse(now);
        ArendeConversationView result = service.createMessage("INTYG_ID", ArendeAmne.KONTKT, "rubrik", "meddelande");
        assertNotNull(result.getFraga());
        assertNull(result.getSvar());
        assertNotNull(result.getSenasteHandelse());
        verify(webcertUserService).isAuthorizedForUnit(anyString(), anyBoolean());
        verify(arendeRepository).save(any(Arende.class));
        verify(monitoringLog).logArendeCreated(anyString(), anyString(), anyString(), any(ArendeAmne.class), anyBoolean());
        verify(certificateSenderService).sendMessageToRecipient(anyString(), anyString());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_QUESTION_FROM_CARE);
    }

    @Test
    public void createQuestionInvalidAmneTest() {
        try {
            service.createMessage("INTYG_ID", ArendeAmne.KOMPLT, "rubrik", "meddelande");
            fail("should throw exception");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e.getErrorCode());
            verifyZeroInteractions(arendeRepository);
            verifyZeroInteractions(notificationService);
        }
    }

    @Test
    public void createQuestionCertificateDoesNotExistTest() {
        when(utkastRepository.findOne(anyString())).thenReturn(null);
        try {
            service.createMessage("INTYG_ID", ArendeAmne.KONTKT, "rubrik", "meddelande");
            fail("should throw exception");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, e.getErrorCode());
            verifyZeroInteractions(arendeRepository);
            verifyZeroInteractions(notificationService);
        }
    }

    @Test
    public void createQuestionCertificateNotSignedTest() {
        when(utkastRepository.findOne(anyString())).thenReturn(new Utkast());
        try {
            service.createMessage("INTYG_ID", ArendeAmne.KONTKT, "rubrik", "meddelande");
            fail("should throw exception");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.INVALID_STATE, e.getErrorCode());
            verifyZeroInteractions(arendeRepository);
            verifyZeroInteractions(notificationService);
        }
    }

    @Test
    public void createQuestionInvalidCertificateTypeTest() {
        Utkast utkast = new Utkast();
        utkast.setSignatur(new Signatur());
        utkast.setIntygsTyp("fk7263");
        when(utkastRepository.findOne(anyString())).thenReturn(utkast);
        try {
            service.createMessage("INTYG_ID", ArendeAmne.KONTKT, "rubrik", "meddelande");
            fail("should throw exception");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.INVALID_STATE, e.getErrorCode());
            verifyZeroInteractions(arendeRepository);
            verifyZeroInteractions(notificationService);
        }
    }

    @Test
    public void createQuestionUnauthorizedTest() {
        Utkast utkast = new Utkast();
        utkast.setSignatur(new Signatur());
        when(utkastRepository.findOne(anyString())).thenReturn(utkast);
        when(webcertUserService.isAuthorizedForUnit(anyString(), anyBoolean())).thenReturn(false);
        try {
            service.createMessage("INTYG_ID", ArendeAmne.KONTKT, "rubrik", "meddelande");
            fail("should throw exception");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, e.getErrorCode());
            verifyZeroInteractions(arendeRepository);
            verifyZeroInteractions(notificationService);
        }
    }

    @Test
    public void testCreateQuestionIfCertificateIsRevoked() throws WebCertServiceException {
        Utkast utkast = buildUtkast();
        utkast.setAterkalladDatum(LocalDateTime.now());
        when(utkastRepository.findOne(anyString())).thenReturn(utkast);
        try {
            service.createMessage("INTYG_ID", ArendeAmne.KONTKT, "rubrik", "meddelande");
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.CERTIFICATE_REVOKED, e.getErrorCode());
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyZeroInteractions(notificationService);
        }
    }

    @Test
    public void answerTest() throws CertificateSenderException {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
        Arende fraga = buildArende(svarPaMeddelandeId, null);
        fraga.setAmne(ArendeAmne.OVRIGT);
        fraga.setSenasteHandelse(LocalDateTime.now());
        fraga.setStatus(Status.PENDING_INTERNAL_ACTION);
        fraga.setPatientPersonId("191212121212");
        when(arendeRepository.findOneByMeddelandeId(svarPaMeddelandeId)).thenReturn(fraga);
        when(webcertUserService.isAuthorizedForUnit(anyString(), anyBoolean())).thenReturn(true);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());
        ArendeConversationView result = service.answer(svarPaMeddelandeId, "svarstext");
        assertNotNull(result.getFraga());
        assertNotNull(result.getSvar());
        assertNotNull(result.getSenasteHandelse());
        verify(webcertUserService).isAuthorizedForUnit(anyString(), anyBoolean());
        verify(arendeRepository, times(2)).save(any(Arende.class));
        verify(monitoringLog).logArendeCreated(anyString(), anyString(), anyString(), any(ArendeAmne.class), anyBoolean());
        verify(certificateSenderService).sendMessageToRecipient(anyString(), anyString());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
    }

    @Test(expected = WebCertServiceException.class)
    public void answerSvarsTextNullTest() throws CertificateSenderException {
        try {
            service.answer("svarPaMeddelandeId", null);
        } finally {
            verifyZeroInteractions(arendeRepository);
            verifyZeroInteractions(notificationService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void answerSvarsTextEmptyTest() throws CertificateSenderException {
        try {
            service.answer("svarPaMeddelandeId", "");
        } finally {
            verifyZeroInteractions(arendeRepository);
            verifyZeroInteractions(notificationService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void answerQuestionWithInvalidStatusTest() throws CertificateSenderException {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
        Arende fraga = new Arende();
        fraga.setStatus(Status.PENDING_EXTERNAL_ACTION);
        when(arendeRepository.findOneByMeddelandeId(svarPaMeddelandeId)).thenReturn(fraga);
        when(webcertUserService.isAuthorizedForUnit(anyString(), anyBoolean())).thenReturn(true);
        try {
            service.answer(svarPaMeddelandeId, "svarstext");
        } finally {
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyZeroInteractions(notificationService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void answerPaminnQuestionTest() throws CertificateSenderException {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
        Arende fraga = new Arende();
        fraga.setStatus(Status.PENDING_INTERNAL_ACTION);
        fraga.setAmne(ArendeAmne.PAMINN);
        when(arendeRepository.findOneByMeddelandeId(svarPaMeddelandeId)).thenReturn(fraga);
        when(webcertUserService.isAuthorizedForUnit(anyString(), anyBoolean())).thenReturn(true);
        try {
            service.answer(svarPaMeddelandeId, "svarstext");
        } finally {
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyZeroInteractions(notificationService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void answerKompltQuestionUnauthorizedTest() throws CertificateSenderException {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
        Arende fraga = new Arende();
        fraga.setStatus(Status.PENDING_INTERNAL_ACTION);
        fraga.setAmne(ArendeAmne.KOMPLT);
        when(arendeRepository.findOneByMeddelandeId(svarPaMeddelandeId)).thenReturn(fraga);
        when(webcertUserService.isAuthorizedForUnit(anyString(), anyBoolean())).thenReturn(true);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());
        try {
            service.answer(svarPaMeddelandeId, "svarstext");
        } finally {
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyZeroInteractions(notificationService);
        }
    }

    @Test
    public void answerKompltQuestionAuthorizedTest() throws CertificateSenderException {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
        Arende fraga = buildArende(svarPaMeddelandeId, null);
        fraga.setStatus(Status.PENDING_INTERNAL_ACTION);
        fraga.setAmne(ArendeAmne.KOMPLT);
        fraga.setPatientPersonId("191212121212");
        when(arendeRepository.findOneByMeddelandeId(svarPaMeddelandeId)).thenReturn(fraga);
        when(webcertUserService.isAuthorizedForUnit(anyString(), anyBoolean())).thenReturn(true);
        WebCertUser webcertUser = new WebCertUser();
        webcertUser.setAuthorities(new HashMap<>());
        Privilege privilege = new Privilege();
        privilege.setRequestOrigins(new ArrayList<>());
        webcertUser.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA, privilege);
        when(webcertUserService.getUser()).thenReturn(webcertUser);

        ArendeConversationView result = service.answer(svarPaMeddelandeId, "svarstext");

        assertNotNull(result.getFraga());
        assertNotNull(result.getSvar());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
        verify(arendeRepository, times(2)).save(any(Arende.class));
    }

    @Test
    public void answerKompltQuestionClosesAllCompletionsAsHandled() throws CertificateSenderException {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
        Arende fraga = buildArende(svarPaMeddelandeId, null);
        fraga.setStatus(Status.PENDING_INTERNAL_ACTION);
        fraga.setAmne(ArendeAmne.KOMPLT);
        fraga.setPatientPersonId("191212121212");
        Arende komplt1 = buildArende(UUID.randomUUID().toString(), null);
        komplt1.setStatus(Status.PENDING_INTERNAL_ACTION);
        komplt1.setAmne(ArendeAmne.KOMPLT);
        komplt1.setPatientPersonId("191212121212");
        Arende komplt2 = buildArende(UUID.randomUUID().toString(), null);
        komplt2.setStatus(Status.PENDING_INTERNAL_ACTION);
        komplt2.setAmne(ArendeAmne.KOMPLT);
        komplt2.setPatientPersonId("191212121212");
        Arende otherSubject = buildArende(UUID.randomUUID().toString(), null);
        otherSubject.setStatus(Status.PENDING_INTERNAL_ACTION);
        otherSubject.setAmne(ArendeAmne.AVSTMN);
        otherSubject.setPatientPersonId("191212121212");
        when(arendeRepository.findOneByMeddelandeId(svarPaMeddelandeId)).thenReturn(fraga);
        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(Arrays.asList(fraga, komplt1, otherSubject, komplt2));
        when(webcertUserService.isAuthorizedForUnit(anyString(), anyBoolean())).thenReturn(true);
        WebCertUser webcertUser = new WebCertUser();
        webcertUser.setAuthorities(new HashMap<>());
        Privilege privilege = new Privilege();
        privilege.setRequestOrigins(new ArrayList<>());
        webcertUser.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA, privilege);
        when(webcertUserService.getUser()).thenReturn(webcertUser);

        ArendeConversationView result = service.answer(svarPaMeddelandeId, "svarstext");

        assertNotNull(result.getFraga());
        assertNotNull(result.getSvar());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
        verify(arendeRepository).findByIntygsId(INTYG_ID);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository, times(4)).save(arendeCaptor.capture());
        for (Arende a : arendeCaptor.getAllValues()) {
            assertEquals(Status.CLOSED, a.getStatus());
            assertTrue(Arrays.asList(result.getSvar().getInternReferens(), svarPaMeddelandeId, komplt1.getId(), komplt2.getId()).contains(a.getId()));
        }
    }

    @Test
    public void answerUpdatesQuestionTest() throws CertificateSenderException {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
        Arende fraga = buildArende(svarPaMeddelandeId, null);
        fraga.setAmne(ArendeAmne.OVRIGT);
        fraga.setMeddelandeId(svarPaMeddelandeId);
        fraga.setStatus(Status.PENDING_INTERNAL_ACTION);
        fraga.setPatientPersonId("191212121212");
        when(arendeRepository.findOneByMeddelandeId(svarPaMeddelandeId)).thenReturn(fraga);
        when(webcertUserService.isAuthorizedForUnit(anyString(), anyBoolean())).thenReturn(true);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());
        ArendeConversationView result = service.answer(svarPaMeddelandeId, "svarstext");
        assertNotNull(result.getFraga());
        assertNotNull(result.getSvar());
        assertNotNull(result.getSenasteHandelse());
        verify(webcertUserService).isAuthorizedForUnit(anyString(), anyBoolean());
        verify(monitoringLog).logArendeCreated(anyString(), anyString(), anyString(), any(ArendeAmne.class), anyBoolean());
        verify(certificateSenderService).sendMessageToRecipient(anyString(), anyString());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository, times(2)).save(arendeCaptor.capture());

        Arende updatedQuestion = arendeCaptor.getAllValues().get(1);
        assertNotNull(updatedQuestion.getSenasteHandelse());
        assertEquals(Status.CLOSED, updatedQuestion.getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void setForwardedArendeNotFoundTest() {
        try {
            service.setForwarded(MEDDELANDE_ID, true);
        } finally {
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyZeroInteractions(notificationService);
        }
    }

    @Test
    public void setForwardedTrueTest() {
        Arende arende = buildArende(MEDDELANDE_ID, null);
        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);
        service.setForwarded(MEDDELANDE_ID, true);

        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        verify(arendeRepository).findBySvarPaId(MEDDELANDE_ID); // lookup answers for response
        verify(arendeRepository).findByPaminnelseMeddelandeId(MEDDELANDE_ID); // lookup reminders for response

        assertEquals(true, arendeCaptor.getValue().getVidarebefordrad());
        verifyZeroInteractions(notificationService);
    }

    @Test
    public void setForwardedFalseTest() {
        Arende arende = buildArende(MEDDELANDE_ID, null);
        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);
        service.setForwarded(MEDDELANDE_ID, false);

        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        verify(arendeRepository).findBySvarPaId(MEDDELANDE_ID); // lookup answers for response
        verify(arendeRepository).findByPaminnelseMeddelandeId(MEDDELANDE_ID); // lookup reminders for response

        assertEquals(false, arendeCaptor.getValue().getVidarebefordrad());
        verifyZeroInteractions(notificationService);
    }

    @Test(expected = WebCertServiceException.class)
    public void closeArendeAsHandledArendeNotFoundTest() {
        try {
            service.closeArendeAsHandled(MEDDELANDE_ID, INTYG_TYP);
        } finally {
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyZeroInteractions(notificationService);
        }
    }

    @Test
    public void closeArendeAsHandledTest() {
        Arende arende = buildArende(MEDDELANDE_ID, null);
        arende.setSkickatAv(FrageStallare.FORSAKRINGSKASSAN.getKod());
        arende.setStatus(Status.PENDING_INTERNAL_ACTION);
        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);

        service.closeArendeAsHandled(MEDDELANDE_ID, INTYG_TYP);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        assertEquals(Status.CLOSED, arendeCaptor.getValue().getStatus());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED);
        verify(fragaSvarService, never()).closeQuestionAsHandled(anyLong());
    }

    @Test
    public void closeArendeAsHandledFk7263Test() {
        service.closeArendeAsHandled("1", "fk7263");
        verifyZeroInteractions(arendeRepository);
        verifyZeroInteractions(notificationService);
        verify(fragaSvarService).closeQuestionAsHandled(1L);
    }

    @Test
    public void closeArendeAsHandledFromWCNoAnswerTest() {
        Arende arende = buildArende(MEDDELANDE_ID, null);
        arende.setSkickatAv(FrageStallare.WEBCERT.getKod());
        arende.setStatus(Status.PENDING_EXTERNAL_ACTION);
        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);

        service.closeArendeAsHandled(MEDDELANDE_ID, INTYG_TYP);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        assertEquals(Status.CLOSED, arendeCaptor.getValue().getStatus());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_HANDLED);
        verify(fragaSvarService, never()).closeQuestionAsHandled(anyLong());
    }

    @Test
    public void closeArendeAsHandledAnswerTest() {
        Arende arende = buildArende(MEDDELANDE_ID, null);
        arende.setSkickatAv(FrageStallare.WEBCERT.getKod());
        arende.setStatus(Status.ANSWERED);
        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);
        when(arendeRepository.findBySvarPaId(MEDDELANDE_ID)).thenReturn(Arrays.asList(buildArende(UUID.randomUUID().toString(), null))); // there
        // are
        // answers

        service.closeArendeAsHandled(MEDDELANDE_ID, INTYG_TYP);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        assertEquals(Status.CLOSED, arendeCaptor.getValue().getStatus());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED);
        verify(fragaSvarService, never()).closeQuestionAsHandled(anyLong());
    }

    @Test(expected = WebCertServiceException.class)
    public void openArendeAsUnhandledArendeNotFoundTest() {
        try {
            service.openArendeAsUnhandled(MEDDELANDE_ID);
        } finally {
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyZeroInteractions(notificationService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void openArendeAsUnhandledFromFKAndAnsweredTest() {
        Arende arende = new Arende();
        arende.setSkickatAv(FrageStallare.FORSAKRINGSKASSAN.getKod());
        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);
        when(arendeRepository.findBySvarPaId(MEDDELANDE_ID)).thenReturn(Arrays.asList(new Arende())); // there are
                                                                                                      // answers

        service.openArendeAsUnhandled(MEDDELANDE_ID);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void openArendeAsUnhandledQuestionFromFK() {
        Arende arende = buildArende(MEDDELANDE_ID, null);
        arende.setSkickatAv(FrageStallare.FORSAKRINGSKASSAN.getKod());
        arende.setStatus(Status.CLOSED);
        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);

        service.openArendeAsUnhandled(MEDDELANDE_ID);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        assertEquals(Status.PENDING_INTERNAL_ACTION, arendeCaptor.getValue().getStatus());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_UNHANDLED);
    }

    @Test
    public void openArendeAsUnhandledAnswerFromFK() {
        Arende arende = buildArende(MEDDELANDE_ID, null);
        arende.setSkickatAv(FrageStallare.WEBCERT.getKod());
        arende.setStatus(Status.CLOSED);
        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);
        when(arendeRepository.findBySvarPaId(MEDDELANDE_ID)).thenReturn(Arrays.asList(buildArende(UUID.randomUUID().toString(), null))); // there
        // are
        // answers

        service.openArendeAsUnhandled(MEDDELANDE_ID);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        assertEquals(Status.ANSWERED, arendeCaptor.getValue().getStatus());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED);
    }

    @Test
    public void openArendeAsUnhandledQuestionFromWCTest() {
        Arende arende = buildArende(MEDDELANDE_ID, null);
        arende.setSkickatAv(FrageStallare.WEBCERT.getKod());
        arende.setStatus(Status.CLOSED);
        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);

        service.openArendeAsUnhandled(MEDDELANDE_ID);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        assertEquals(Status.PENDING_EXTERNAL_ACTION, arendeCaptor.getValue().getStatus());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_UNHANDLED);
    }

    @Test
    public void testListSignedByForUnits() {
        final List<String> selectedUnits = Arrays.asList("enhet1", "enhet2");
        final String[] lakare1 = { "hsaid1", "namn1" };
        final String[] lakare2 = { "hsaid2", "namn2" };
        final String[] lakare3 = { "hsaid3", "namn3" };
        final String[] lakare4 = { "hsaid4", "namn4" };
        final List<Object[]> repoResult = Arrays.asList(lakare1, lakare2, lakare3);
        final List<Object[]> expected = Arrays.asList(lakare1, lakare2, lakare3, lakare4);

        WebCertUser user = Mockito.mock(WebCertUser.class);
        when(user.getIdsOfSelectedVardenhet()).thenReturn(selectedUnits);
        when(webcertUserService.getUser()).thenReturn(user);
        when(arendeRepository.findSigneratAvByEnhet(selectedUnits)).thenReturn(repoResult);
        when(fragaSvarService.getFragaSvarHsaIdByEnhet(eq(null))).thenReturn(Arrays.asList(new Lakare(lakare4[0], lakare4[1])));

        List<Lakare> res = service.listSignedByForUnits(null);

        assertEquals(expected.stream().map(arr -> new Lakare((String) arr[0], (String) arr[1])).collect(Collectors.toList()), res);

        verify(webcertUserService).getUser();
        verify(arendeRepository).findSigneratAvByEnhet(selectedUnits);
    }

    @Test
    public void testListSignedByForUnitsSpecifiedUnit() {
        final List<String> selectedUnit = Arrays.asList("enhet1");
        final String[] lakare1 = { "hsaid1", "namn1" };
        final String[] lakare2 = { "hsaid2", "namn2" };
        final String[] lakare3 = { "hsaid3", "namn3" };
        final List<Object[]> repoResult = Arrays.asList(lakare1, lakare2, lakare3);

        when(webcertUserService.isAuthorizedForUnit(anyString(), eq(true))).thenReturn(true);
        when(arendeRepository.findSigneratAvByEnhet(selectedUnit)).thenReturn(repoResult);
        when(fragaSvarService.getFragaSvarHsaIdByEnhet(eq(null))).thenReturn(new ArrayList<>());

        List<Lakare> res = service.listSignedByForUnits(selectedUnit.get(0));

        assertEquals(repoResult.stream().map(arr -> new Lakare((String) arr[0], (String) arr[1])).collect(Collectors.toList()), res);

        verify(arendeRepository).findSigneratAvByEnhet(selectedUnit);
    }

    @Test
    public void testGetArendeForIntyg() {
        List<Arende> arendeList = new ArrayList<>();

        arendeList.add(buildArende(UUID.randomUUID().toString(), DECEMBER_YEAR_9999, FEBRUARY));
        arendeList.add(buildArende(UUID.randomUUID().toString(), JANUARY, JANUARY));
        arendeList.get(1).setSvarPaId(arendeList.get(0).getMeddelandeId()); // svar
        arendeList.add(buildArende(UUID.randomUUID().toString(), DECEMBER_YEAR_9999, DECEMBER_YEAR_9999));
        arendeList.get(2).setAmne(ArendeAmne.PAMINN);
        arendeList.get(2).setPaminnelseMeddelandeId(arendeList.get(0).getMeddelandeId()); // paminnelse
        arendeList.add(buildArende(UUID.randomUUID().toString(), FEBRUARY, FEBRUARY));
        arendeList.add(buildArende(UUID.randomUUID().toString(), DECEMBER_YEAR_9999, DECEMBER_YEAR_9999));
        arendeList.add(buildArende(UUID.randomUUID().toString(), JANUARY, JANUARY));

        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(arendeList);

        when(webcertUserService.getUser()).thenReturn(createUser());

        List<ArendeConversationView> result = service.getArenden(INTYG_ID);

        verify(arendeRepository).findByIntygsId(INTYG_ID);
        verify(webcertUserService).getUser();

        assertEquals(4, result.size());
        assertEquals(1, result.get(0).getPaminnelser().size());
        assertEquals(arendeList.get(0).getMeddelandeId(), result.get(0).getFraga().getInternReferens());
        assertEquals(arendeList.get(1).getMeddelandeId(), result.get(0).getSvar().getInternReferens());
        assertEquals(arendeList.get(2).getMeddelandeId(), result.get(0).getPaminnelser().get(0).getInternReferens());
        assertEquals(arendeList.get(3).getMeddelandeId(), result.get(2).getFraga().getInternReferens());
        assertEquals(arendeList.get(4).getMeddelandeId(), result.get(1).getFraga().getInternReferens());
        assertEquals(arendeList.get(5).getMeddelandeId(), result.get(3).getFraga().getInternReferens());
        assertEquals(DECEMBER_YEAR_9999, result.get(0).getSenasteHandelse());
        assertEquals(DECEMBER_YEAR_9999, result.get(1).getSenasteHandelse());
        assertEquals(FEBRUARY, result.get(2).getSenasteHandelse());
        assertEquals(JANUARY, result.get(3).getSenasteHandelse());
    }

    @Test
    public void testGetArendenFiltersOnEnhet() {
        List<Arende> arendeList = new ArrayList<>();

        arendeList.add(buildArende(UUID.randomUUID().toString(), ENHET_ID));
        arendeList.get(0).setSenasteHandelse(FEBRUARY);
        arendeList.add(buildArende(UUID.randomUUID().toString(), "otherUnit"));
        arendeList.get(1).setSenasteHandelse(JANUARY);
        arendeList.add(buildArende(UUID.randomUUID().toString(), ENHET_ID));
        arendeList.get(2).setSenasteHandelse(DECEMBER_YEAR_9999);
        arendeList.add(buildArende(UUID.randomUUID().toString(), "unit-123"));
        arendeList.get(3).setSenasteHandelse(FEBRUARY);

        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(arendeList);

        when(webcertUserService.getUser()).thenReturn(createUser());

        List<ArendeConversationView> result = service.getArenden(INTYG_ID);

        verify(arendeRepository).findByIntygsId(INTYG_ID);
        verify(webcertUserService).getUser();
        verify(arendeViewConverter).convertToDto(arendeList.get(0));
        verify(arendeViewConverter).convertToDto(arendeList.get(2));
        verify(arendeViewConverter, never()).convertToDto(arendeList.get(1));
        verify(arendeViewConverter, never()).convertToDto(arendeList.get(3));

        assertEquals(2, result.size());
    }

    @Test(expected = WebCertServiceException.class)
    public void testFilterArendeWithAuthFail() {
        WebCertUser webCertUser = createUser();
        when(webcertUserService.getUser()).thenReturn(webCertUser);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setEnhetId("no-auth");

        service.filterArende(params);
    }

    @Test
    public void testFilterArendeWithEnhetsIdAsParam() {
        WebCertUser webCertUser = createUser();
        when(webcertUserService.isAuthorizedForUnit(any(String.class), eq(true))).thenReturn(true);

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now(), null));
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now().minusDays(1), null));

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);
        when(arendeRepository.filterArendeCount(any(Filter.class))).thenReturn(queryResults.size() + 1);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.setTotalCount(0);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setEnhetId(webCertUser.getValdVardenhet().getId());

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(webcertUserService).isAuthorizedForUnit(anyString(), eq(true));

        verify(arendeRepository).filterArende(any(Filter.class));
        verify(arendeRepository).filterArendeCount(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(2, response.getResults().size());
        assertEquals(3, response.getTotalCount());
    }

    @Test
    public void testFilterArendeWithNoEnhetsIdAsParam() {
        when(webcertUserService.getUser()).thenReturn(createUser());

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now(), null));
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now().plusDays(1), null));

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);
        when(arendeRepository.filterArendeCount(any(Filter.class))).thenReturn(queryResults.size() + 1);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.setTotalCount(0);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(webcertUserService).getUser();

        verify(arendeRepository).filterArende(any(Filter.class));
        verify(arendeRepository).filterArendeCount(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(2, response.getResults().size());
        assertEquals(3, response.getTotalCount());
    }

    @Test
    public void testFilterArendeMergesFragaSvar() {
        when(webcertUserService.getUser()).thenReturn(createUser());

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now(), null));
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now().plusDays(1), null));

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);
        when(arendeRepository.filterArendeCount(any(Filter.class))).thenReturn(queryResults.size() + 1);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.getResults().add(buildArendeListItem("intyg1", LocalDateTime.now().minusDays(1)));
        fsResponse.setTotalCount(1);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(webcertUserService).getUser();

        verify(arendeRepository).filterArende(any(Filter.class));
        verify(arendeRepository).filterArendeCount(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(3, response.getResults().size());
        assertEquals(4, response.getTotalCount());
    }

    @Test
    public void testFilterArendeInvalidStartPosition() {
        when(webcertUserService.getUser()).thenReturn(createUser());

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now(), null));
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now().plusDays(1), null));

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);
        when(arendeRepository.filterArendeCount(any(Filter.class))).thenReturn(queryResults.size() + 1);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.getResults().add(buildArendeListItem("intyg1", LocalDateTime.now().minusDays(1)));
        fsResponse.setTotalCount(1);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setStartFrom(5);

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(webcertUserService).getUser();

        verify(arendeRepository).filterArende(any(Filter.class));
        verify(arendeRepository).filterArendeCount(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(0, response.getResults().size());
        assertEquals(4, response.getTotalCount());
    }

    @Test
    public void testFilterArendeSelection() {
        when(webcertUserService.getUser()).thenReturn(createUser());
        when(authoritiesHelper.getIntygstyperForPrivilege(any(UserDetails.class), anyString())).thenReturn(new HashSet<>());

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now(), null));
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now().plusDays(1), null));

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);
        when(arendeRepository.filterArendeCount(any(Filter.class))).thenReturn(queryResults.size() + 1);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.getResults().add(buildArendeListItem("intyg1", LocalDateTime.now().minusDays(1)));
        fsResponse.setTotalCount(1);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setStartFrom(2);
        params.setPageSize(10);

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(webcertUserService).getUser();

        verify(arendeRepository).filterArende(any(Filter.class));
        verify(arendeRepository).filterArendeCount(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(1, response.getResults().size());
        assertEquals(4, response.getTotalCount());
    }

    @Test
    public void testFilterArendeSortsArendeListItemsByReceivedDate() {
        final String intygId1 = "intygId1";
        final String intygId2 = "intygId2";
        final String intygId3 = "intygId3";
        final String MEDDELANDE_ID = "arendeWithPaminnelseMEDDELANDE_ID";

        when(webcertUserService.getUser()).thenReturn(createUser());

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), intygId3, LocalDateTime.now().plusDays(2), null, ENHET_ID));

        Arende arendeWithPaminnelse = buildArende(UUID.randomUUID().toString(), intygId2, LocalDateTime.now(), null, ENHET_ID);
        arendeWithPaminnelse.setMeddelandeId(MEDDELANDE_ID);
        queryResults.add(arendeWithPaminnelse);

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);
        when(arendeRepository.filterArendeCount(any(Filter.class))).thenReturn(queryResults.size());
        when(arendeRepository.findByPaminnelseMeddelandeId(eq(MEDDELANDE_ID))).thenReturn(Arrays.asList(new Arende()));

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.getResults().add(buildArendeListItem(intygId1, LocalDateTime.now().minusDays(1)));
        fsResponse.setTotalCount(1);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();

        QueryFragaSvarResponse response = service.filterArende(params);

        assertEquals(3, response.getResults().size());
        assertEquals(intygId3, response.getResults().get(0).getIntygId());
        assertEquals(intygId2, response.getResults().get(1).getIntygId());
        assertEquals(intygId1, response.getResults().get(2).getIntygId());
    }

    @Test
    public void testGetArende() {
        final String MEDDELANDE_ID = "med0123";
        final String id = UUID.randomUUID().toString();
        Arende arende = buildArende(id, LocalDateTime.now(), null);

        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);

        Arende res = service.getArende(MEDDELANDE_ID);
        assertEquals(id, res.getMeddelandeId());
    }

    @Test
    public void testCloseAllNonClosedQuestions() {
        Arende arendeFromWc = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arendeFromWc.setSkickatAv(FrageStallare.WEBCERT.getKod());
        arendeFromWc.setStatus(Status.ANSWERED);
        Arende arendeFromFk = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arendeFromFk.setSkickatAv(FrageStallare.FORSAKRINGSKASSAN.getKod());
        Arende arendeAlreadyClosed = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arendeAlreadyClosed.setStatus(Status.CLOSED);
        // svar and paminnelse will be ignored
        Arende arendeSvar = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arendeSvar.setSvarPaId(arendeFromWc.getMeddelandeId());
        Arende arendePaminnelse = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arendePaminnelse.setAmne(ArendeAmne.PAMINN);
        arendePaminnelse.setPaminnelseMeddelandeId(arendeFromFk.getMeddelandeId());
        when(arendeRepository.findByIntygsId(INTYG_ID))
                .thenReturn(Arrays.asList(arendeFromWc, arendeFromFk, arendeAlreadyClosed, arendeSvar, arendePaminnelse));

        service.closeAllNonClosed(INTYG_ID);

        verify(arendeRepository).findByIntygsId(INTYG_ID);
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED);
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository, times(2)).save(arendeCaptor.capture());
        assertEquals(Status.CLOSED, arendeCaptor.getAllValues().get(0).getStatus());
        assertEquals(Status.CLOSED, arendeCaptor.getAllValues().get(1).getStatus());
        verify(fragaSvarService).closeAllNonClosedQuestions(INTYG_ID);
    }

    @Test
    public void testCloseCompletionsAsHandled() {
        final String intygId = "intygId";
        Arende arende1 = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arende1.setAmne(ArendeAmne.KOMPLT);
        arende1.setSkickatAv(FrageStallare.FORSAKRINGSKASSAN.getKod());
        Arende arende2 = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arende2.setAmne(ArendeAmne.KONTKT);
        arende2.setSkickatAv(FrageStallare.FORSAKRINGSKASSAN.getKod());
        Arende arende3 = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arende3.setAmne(ArendeAmne.KOMPLT);
        arende3.setSkickatAv(FrageStallare.FORSAKRINGSKASSAN.getKod());
        Arende arende4 = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arende4.setAmne(ArendeAmne.KOMPLT);
        arende4.setStatus(Status.CLOSED); // already closed
        arende4.setSkickatAv(FrageStallare.FORSAKRINGSKASSAN.getKod());

        when(arendeRepository.findByIntygsId(intygId)).thenReturn(Arrays.asList(arende1, arende2, arende3, arende4));
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);

        service.closeCompletionsAsHandled(intygId, "luse");

        verify(arendeRepository).findByIntygsId(intygId);
        verify(arendeRepository, times(2)).save(arendeCaptor.capture());
        assertEquals(arende1.getMeddelandeId(), arendeCaptor.getAllValues().get(0).getMeddelandeId());
        assertEquals(Status.CLOSED, arendeCaptor.getAllValues().get(0).getStatus());
        assertEquals(arende3.getMeddelandeId(), arendeCaptor.getAllValues().get(1).getMeddelandeId());
        assertEquals(Status.CLOSED, arendeCaptor.getAllValues().get(1).getStatus());
        verify(notificationService, times(2)).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED);
    }

    @Test
    public void testCloseCompletionsAsHandledNoMatches() {
        final String intygId = "intygId";

        when(arendeRepository.findByIntygsId(intygId)).thenReturn(new ArrayList<>());

        service.closeCompletionsAsHandled(intygId, "luse");

        verify(arendeRepository).findByIntygsId(intygId);
        verify(arendeRepository, never()).save(any(Arende.class));
        verifyZeroInteractions(notificationService);
    }

    @Test
    public void closeCompletionsAsHandledFk7263Test() {
        final String intygId = "intygId";
        service.closeCompletionsAsHandled(intygId, "fk7263");
        verifyZeroInteractions(arendeRepository);
        verifyZeroInteractions(notificationService);
        verify(fragaSvarService).closeCompletionsAsHandled(intygId);
    }

    private Arende buildArende(String meddelandeId, String enhetId) {
        return buildArende(meddelandeId, INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), enhetId);
    }

    private Arende buildArende(String meddelandeId, LocalDateTime senasteHandelse, LocalDateTime timestamp) {
        return buildArende(meddelandeId, INTYG_ID, senasteHandelse, timestamp, ENHET_ID);
    }

    private Arende buildArende(String meddelandeId, String intygId, LocalDateTime senasteHandelse, LocalDateTime timestamp, String enhetId) {
        Arende arende = new Arende();
        arende.setStatus(Status.PENDING_INTERNAL_ACTION);
        arende.setAmne(ArendeAmne.OVRIGT);
        arende.setReferensId("<fk-extern-referens>");
        arende.setMeddelandeId(meddelandeId);
        arende.setEnhetId(enhetId);
        arende.setSenasteHandelse(senasteHandelse);
        arende.setMeddelande("frageText");
        arende.setTimestamp(timestamp);
        List<MedicinsktArende> komplettering = new ArrayList<>();
        arende.setIntygsId(intygId);
        arende.setPatientPersonId(PATIENT_ID.getPersonnummer());
        arende.setSigneratAv("Signatur");
        arende.setSistaDatumForSvar(senasteHandelse.plusDays(7).toLocalDate());
        arende.setKomplettering(komplettering);
        arende.setRubrik("rubrik");
        arende.setSkickatAv("Avsandare");
        arende.setVidarebefordrad(false);

        return arende;
    }

    private ArendeListItem buildArendeListItem(String INTYG_ID, LocalDateTime receivedDate) {
        ArendeListItem arende = new ArendeListItem();
        arende.setIntygId(INTYG_ID);
        arende.setReceivedDate(receivedDate);

        return arende;
    }

    private Utkast buildUtkast() {
        final String signeratAv = "signeratAv";
        Utkast utkast = new Utkast();
        utkast.setIntygsId(INTYG_ID);
        utkast.setSkapadAv(new VardpersonReferens());
        utkast.getSkapadAv().setHsaId(signeratAv);
        utkast.setSignatur(mock(Signatur.class));
        when(utkast.getSignatur().getSigneradAv()).thenReturn(signeratAv);
        return utkast;
    }

    private WebCertUser createUser() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));
        user.setHsaId("testuser");
        user.setNamn("test userman");

        Vardenhet vardenhet = new Vardenhet(ENHET_ID, "enhet");

        Vardgivare vardgivare = new Vardgivare("vardgivare", "Vardgivaren");
        vardgivare.getVardenheter().add(vardenhet);

        user.setVardgivare(Collections.singletonList(vardgivare));
        user.setValdVardenhet(vardenhet);

        return user;
    }

}
