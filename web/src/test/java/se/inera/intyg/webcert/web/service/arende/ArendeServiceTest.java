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
package se.inera.intyg.webcert.web.service.arende;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import jakarta.xml.ws.WebServiceException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.integration.hsatk.services.HsatkEmployeeService;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserDetails;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
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
import se.inera.intyg.webcert.web.csintegration.certificate.IntegratedUnitNotificationEvaluator;
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderException;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.employee.EmployeeNameService;
import se.inera.intyg.webcert.web.service.facade.list.PaginationAndLoggingService;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.message.MessageImportService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationEvent;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolverResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.StatisticsGroupByUtil;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations.FrontendRelations;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ArendeServiceTest extends AuthoritiesConfigurationTestSetup {

    private static final LocalDateTime JANUARY = LocalDateTime.parse("2013-01-12T11:22:11");
    private static final LocalDateTime FEBRUARY = LocalDateTime.parse("2013-02-12T11:22:11");
    private static final LocalDateTime DECEMBER_YEAR_9999 = LocalDateTime.parse("9999-12-11T10:22:00");

    private static final long FIXED_TIME_NANO = 1456329300599000L;

    private static final Instant FIXED_TIME_INSTANT = Instant.ofEpochSecond(FIXED_TIME_NANO / 1_000_000, FIXED_TIME_NANO % 1_000_000);

    private static final String INTYG_ID = "intyg-1";
    private static final String INTYG_TYP = "luse";
    private static final String ENHET_ID = "enhet";
    private static final String MEDDELANDE_ID = "meddelandeId";
    private static final String PERSON_ID = "191212121212";
    private static final String SKICKAT_AV = "FKASSA";
    private static final Personnummer PNR = Personnummer.createPersonnummer(PERSON_ID).orElseThrow();
    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";
    private static final LocalDateTime ISSUING_DATE = LocalDateTime.now();
    @Mock
    private EmployeeNameService employeeNameService;
    @Mock
    private IntegratedUnitNotificationEvaluator integratedUnitNotificationEvaluator;
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
    private FragaSvarService fragaSvarService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CertificateEventService certificateEventService;

    @Mock
    private CertificateSenderService certificateSenderService;

    @Mock
    private ArendeDraftService arendeDraftService;

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private StatisticsGroupByUtil statisticsGroupByUtil;

    @Mock
    private IntygModuleFacade modelFacade;

    @Mock
    private CertificateAccessServiceHelper certificateAccessServiceHelper;

    @Mock
    private HsatkEmployeeService hsaEmployeeService;

    @Mock
    private IntygService intygService;

    @Mock
    private LogService logService;

    @Mock
    private MessageImportService messageImportService;

    @Mock
    private PaginationAndLoggingService paginationAndLoggingService;

    @InjectMocks
    private ArendeServiceImpl service;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        service.setMockSystemClock(Clock.fixed(FIXED_TIME_INSTANT, ZoneId.systemDefault()));

        // always return the Arende that is saved
        when(arendeRepository.save(any(Arende.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        // Return hsaId as name
        when(hsaEmployeeService.getEmployee(anyString(), any())).thenAnswer(invocation -> {
            final var personInformation = new PersonInformation();
            personInformation.setMiddleAndSurName((String) invocation.getArguments()[0]);
            return List.of(personInformation);
        });

        PatientDetailsResolverResponse response = new PatientDetailsResolverResponse();
        response.setTestIndicator(false);
        response.setDeceased(false);
        response.setProtectedPerson(SekretessStatus.FALSE);
        Map<Personnummer, PatientDetailsResolverResponse> statusMap = mock(Map.class);
        when(statusMap.get(any(Personnummer.class))).thenReturn(response);
        Mockito.when(patientDetailsResolver.getPersonStatusesForList(any())).thenReturn(statusMap);
    }

    @Test
    public void testProcessIncomingMessage() throws WebCertServiceException {
        final String signeratAvName = "signeratAvName";

        Arende arende = new Arende();
        arende.setIntygsId(INTYG_ID);

        Utkast utkast = buildUtkast();
        utkast.getSkapadAv().setNamn(signeratAvName);
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.of(utkast));

        Arende res = service.processIncomingMessage(arende);

        assertNotNull(res);
        assertEquals(FIXED_TIME_INSTANT, res.getTimestamp().toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));
        assertEquals(FIXED_TIME_INSTANT,
            res.getSenasteHandelse().toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));
        assertEquals(utkast.getSignatur().getSigneradAv(), res.getSigneratAv());
        assertEquals(signeratAvName, res.getSigneratAvName());

        verify(utkastRepository).findById(INTYG_ID);
        verify(notificationService).sendNotificationForQuestionReceived(any(Arende.class), eq(CARE_PROVIDER_ID), eq(ISSUING_DATE));
        verifyNoInteractions(arendeDraftService);
    }

    @Test
    public void testProcessIncomingMessageSendsNotificationForQuestionReceived() throws WebCertServiceException {
        Arende arende = new Arende();
        arende.setIntygsId(INTYG_ID);
        arende.setSkickatAv(SKICKAT_AV);

        Utkast utkast = buildUtkast();
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.of(utkast));

        Arende res = service.processIncomingMessage(arende);

        assertNotNull(res);
        assertEquals(INTYG_ID, res.getIntygsId());

        verify(utkastRepository).findById(INTYG_ID);
        verify(certificateEventService).createCertificateEvent(INTYG_ID, SKICKAT_AV, EventCode.NYFRFM, EventCode.NYFRFM.getDescription());
        verify(notificationService).sendNotificationForQuestionReceived(any(Arende.class), eq(CARE_PROVIDER_ID), eq(ISSUING_DATE));
        verifyNoInteractions(arendeDraftService);
    }

    @Test
    public void testProcessIncomingMessageSendsNotificationForAnswerRecieved() throws WebCertServiceException {
        final String frageid = "frageid";

        Arende fragearende = new Arende();

        Arende svararende = new Arende();
        svararende.setIntygsId(INTYG_ID);
        svararende.setSvarPaId(frageid);
        svararende.setSkickatAv(SKICKAT_AV);

        Utkast utkast = buildUtkast();
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.of(utkast));
        when(arendeRepository.findOneByMeddelandeId(frageid)).thenReturn(fragearende);

        Arende res = service.processIncomingMessage(svararende);

        assertNotNull(res);
        assertEquals(INTYG_ID, res.getIntygsId());

        verify(arendeRepository).findOneByMeddelandeId(frageid);
        verify(arendeRepository, times(2)).save(any(Arende.class));
        verify(certificateEventService).createCertificateEvent(INTYG_ID, SKICKAT_AV, EventCode.NYSVFM);
        verify(notificationService).sendNotificationForAnswerRecieved(any(Arende.class), eq(CARE_PROVIDER_ID), eq(ISSUING_DATE));
        verifyNoInteractions(arendeDraftService);
    }

    @Test
    public void testProcessIncomingMessageSendsNotificationForQuestionRecievedIfPaminnelse() throws WebCertServiceException {
        final String paminnelseMeddelandeId = "paminnelseMeddelandeId";

        Arende arende = new Arende();
        arende.setIntygsId(INTYG_ID);
        arende.setPaminnelseMeddelandeId(paminnelseMeddelandeId);
        arende.setSkickatAv(SKICKAT_AV);

        Utkast utkast = buildUtkast();
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.of(utkast));

        Arende res = service.processIncomingMessage(arende);

        assertNotNull(res);
        assertEquals(INTYG_ID, res.getIntygsId());

        verify(utkastRepository).findById(INTYG_ID);
        verify(certificateEventService)
            .createCertificateEvent(INTYG_ID, SKICKAT_AV, EventCode.PAMINNELSE);
        verify(notificationService).sendNotificationForQuestionReceived(any(Arende.class), eq(CARE_PROVIDER_ID), eq(ISSUING_DATE));
        verifyNoInteractions(arendeDraftService);
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

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.of(utkast));
        when(arendeRepository.findOneByMeddelandeId(frageid)).thenReturn(fragearende);

        Arende res = service.processIncomingMessage(svararende);
        assertEquals(Status.ANSWERED, res.getStatus());
        assertEquals(FIXED_TIME_INSTANT,
            res.getSenasteHandelse().toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));

        verify(arendeRepository).findOneByMeddelandeId(frageid);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository, times(2)).save(arendeCaptor.capture());

        Arende updatedQuestion = arendeCaptor.getAllValues().get(1);
        assertEquals(FIXED_TIME_INSTANT,
            updatedQuestion.getSenasteHandelse().toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));
        assertEquals(Status.ANSWERED, updatedQuestion.getStatus());
        verify(notificationService, only()).sendNotificationForAnswerRecieved(any(Arende.class), eq(CARE_PROVIDER_ID), eq(ISSUING_DATE));
        verifyNoInteractions(arendeDraftService);
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

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.of(utkast));
        when(arendeRepository.findOneByMeddelandeId(paminnelseid)).thenReturn(paminnelse);

        Arende res = service.processIncomingMessage(svararende);
        assertEquals(FIXED_TIME_INSTANT,
            res.getSenasteHandelse().toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));

        verify(arendeRepository).findOneByMeddelandeId(paminnelseid);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository, times(2)).save(arendeCaptor.capture());

        Arende updatedQuestion = arendeCaptor.getAllValues().get(1);
        assertEquals(FIXED_TIME_INSTANT,
            updatedQuestion.getSenasteHandelse().toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));
        verify(notificationService, only()).sendNotificationForQuestionReceived(any(Arende.class), eq(CARE_PROVIDER_ID), eq(ISSUING_DATE));
        verifyNoInteractions(arendeDraftService);
    }

    @Test
    public void testProcessIncomingMessageCertificateNotFoundInWC() {
        final String signeratAvName = "signeratAvName";

        Arende arende = new Arende();
        arende.setIntygsId(INTYG_ID);
        arende.setMeddelandeId(MEDDELANDE_ID);

        doReturn(true).when(messageImportService).isImportNeeded(INTYG_ID);

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.empty());

        final var intygContentHolder = mock(IntygContentHolder.class);
        final var utlatande = buildUtlatande();
        doReturn(utlatande).when(intygContentHolder).getUtlatande();

        when(modelFacade.getUtlatandeFromInternalModel(any(), any())).thenReturn(utlatande);
        when(intygService.fetchIntygDataForInternalUse(anyString(), anyBoolean())).thenReturn(intygContentHolder);

        Arende res = service.processIncomingMessage(arende);

        assertNotNull(res);
        assertEquals(FIXED_TIME_INSTANT, res.getTimestamp().toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));
        assertEquals(FIXED_TIME_INSTANT,
            res.getSenasteHandelse().toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));
        assertEquals(signeratAvName, res.getSigneratAvName());

        verify(messageImportService).importMessages(INTYG_ID, MEDDELANDE_ID);
    }

    @Test
    public void testProcessIncomingMessageAnswerAlreadyExists() {
        final String svarPaId = "svarPaId";
        Arende arende = new Arende();
        arende.setSvarPaId(svarPaId);
        when(arendeRepository.findBySvarPaId(svarPaId)).thenReturn(ImmutableList.of(new Arende()));

        try {
            service.processIncomingMessage(arende);
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.INVALID_STATE, e.getErrorCode());
            verify(arendeRepository).findBySvarPaId(svarPaId);
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
        }
    }

    @Test
    public void testProcessIncomingMessageCertificateNotSigned() {
        when(utkastRepository.findById(nullable(String.class))).thenReturn(Optional.of(new Utkast()));
        try {
            service.processIncomingMessage(new Arende());
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.INVALID_STATE, e.getErrorCode());
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
        }
    }

    @Test
    public void testProcessIncomingMessageMessageAlreadyExists() {
        when(arendeRepository.findOneByMeddelandeId(isNull())).thenReturn(new Arende());
        try {
            service.processIncomingMessage(new Arende());
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.MESSAGE_ALREADY_EXISTS, e.getErrorCode());
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
        }
    }

    @Test
    public void testProcessIncomingMessageThrowsExceptionIfCertificateIsRevoked() throws WebCertServiceException {
        Utkast utkast = buildUtkast();
        utkast.setAterkalladDatum(LocalDateTime.now());
        when(utkastRepository.findById(nullable(String.class))).thenReturn(Optional.of(utkast));
        try {
            service.processIncomingMessage(new Arende());
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.CERTIFICATE_REVOKED, e.getErrorCode());
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
        }
    }

    @Test
    public void createQuestionTest() throws CertificateSenderException {
        LocalDateTime now = LocalDateTime.now();
        Utkast utkast = buildUtkast();

        doReturn(Optional.of(utkast)).when(utkastRepository).findById(anyString());

        when(webcertUserService.getUser()).thenReturn(new WebCertUser());

        Arende arende = new Arende();
        arende.setSenasteHandelse(now);
        setupMockForAccessService(ActionLinkType.SKAPA_FRAGA, false);
        ArendeConversationView result = service.createMessage(INTYG_ID, ArendeAmne.KONTKT, "rubrik", "meddelande");

        assertNotNull(result.getFraga());
        assertNull(result.getSvar());
        assertEquals(FIXED_TIME_INSTANT,
            Objects.requireNonNull(result.getSenasteHandelse()).toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));

        verify(arendeRepository).save(any(Arende.class));
        verify(monitoringLog).logArendeCreated(anyString(), isNull(), isNull(), any(ArendeAmne.class), anyBoolean(), anyString());
        verify(certificateSenderService).sendMessageToRecipient(anyString(), anyString());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_QUESTION_FROM_CARE);
        verify(arendeDraftService).delete(INTYG_ID, null);
        verify(logService, times(1)).logCreateMessage(any(), any(), any());
    }

    @Test
    public void createQuestionInvalidAmneTest() {
        try {
            service.createMessage(INTYG_ID, ArendeAmne.KOMPLT, "rubrik", "meddelande");
            fail("should throw exception");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e.getErrorCode());
            verifyNoInteractions(arendeRepository);
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
            verifyNoInteractions(logService);
        }
    }

    @Test
    public void createQuestionCertificateDoesNotExistInWCTest() {
        when(utkastRepository.findById(anyString())).thenReturn(Optional.empty());

        final var intygContentHolder = mock(IntygContentHolder.class);
        final var utlatande = buildUtlatande();
        doReturn(utlatande).when(intygContentHolder).getUtlatande();

        when(modelFacade.getUtlatandeFromInternalModel(any(), any())).thenReturn(utlatande);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());
        when(intygService.getIntygTypeInfo(anyString(), any())).thenReturn(mock(IntygTypeInfo.class));
        when(intygService.fetchIntygData(anyString(), any(), anyBoolean())).thenReturn(intygContentHolder);
        doThrow(WebCertServiceException.class).when(employeeNameService).getEmployeeHsaName(any());

        try {
            service.createMessage(INTYG_ID, ArendeAmne.KONTKT, "rubrik", "meddelande");
            fail("should throw exception");
        } catch (WebCertServiceException e) {
            verifyNoInteractions(arendeRepository);
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
            verifyNoInteractions(logService);
        }
    }

    @Test
    public void createQuestionCertificateNotSignedTest() {
        Utkast utkast = buildUtkast();
        utkast.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        utkast.setSignatur(null);

        Utlatande utlatande = buildUtlatande();

        when(utkastRepository.findById(anyString())).thenReturn(Optional.of(utkast));
        when(modelFacade.getUtlatandeFromInternalModel(any(), any())).thenReturn(utlatande);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());

        try {
            service.createMessage(INTYG_ID, ArendeAmne.KONTKT, "rubrik", "meddelande");
            fail("should throw exception");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.INVALID_STATE, e.getErrorCode());
            verifyNoInteractions(arendeRepository);
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
            verifyNoInteractions(logService);
        }
    }

    private Utlatande buildUtlatande() {
        final var utlatande = mock(Utlatande.class);
        doReturn(INTYG_ID).when(utlatande).getId();
        final var grundData = mock(GrundData.class);
        doReturn(grundData).when(utlatande).getGrundData();
        final var skapadAv = mock(HoSPersonal.class);
        doReturn(skapadAv).when(grundData).getSkapadAv();
        doReturn("signeratAvName").when(skapadAv).getFullstandigtNamn();
        final var vardenhet = mock(se.inera.intyg.common.support.model.common.internal.Vardenhet.class);
        doReturn(vardenhet).when(skapadAv).getVardenhet();
        final var vardgivare = mock(se.inera.intyg.common.support.model.common.internal.Vardgivare.class);
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        final var patient = mock(Patient.class);
        doReturn(patient).when(grundData).getPatient();
        doReturn(PNR).when(patient).getPersonId();

        return utlatande;
    }

    @Test
    public void createQuestionInvalidCertificateTypeTest() {
        Utkast utkast = buildUtkast();
        utkast.setSignatur(new Signatur());
        utkast.setIntygsTyp("fk7263");
        when(utkastRepository.findById(anyString())).thenReturn(Optional.of(utkast));

        final var intygContentHolder = mock(IntygContentHolder.class);
        final var utlatande = buildUtlatande();
        doReturn(utlatande).when(intygContentHolder).getUtlatande();

        when(modelFacade.getUtlatandeFromInternalModel(any(), any())).thenReturn(utlatande);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());
        when(intygService.getIntygTypeInfo(anyString(), any())).thenReturn(mock(IntygTypeInfo.class));
        when(intygService.fetchIntygData(anyString(), any(), anyBoolean())).thenReturn(intygContentHolder);

        try {
            service.createMessage(INTYG_ID, ArendeAmne.KONTKT, "rubrik", "meddelande");
            fail("should throw exception");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.INVALID_STATE, e.getErrorCode());
            verifyNoInteractions(arendeRepository);
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
            verifyNoInteractions(logService);
        }
    }

    @Test
    public void testCreateQuestionIfCertificateIsRevoked() throws WebCertServiceException {
        Utkast utkast = buildUtkast();
        utkast.setAterkalladDatum(LocalDateTime.now());
        when(utkastRepository.findById(anyString())).thenReturn(Optional.of(utkast));

        final var intygContentHolder = mock(IntygContentHolder.class);
        final var utlatande = buildUtlatande();
        doReturn(utlatande).when(intygContentHolder).getUtlatande();

        when(modelFacade.getUtlatandeFromInternalModel(any(), any())).thenReturn(utlatande);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());
        when(intygService.getIntygTypeInfo(anyString(), any())).thenReturn(mock(IntygTypeInfo.class));
        when(intygService.fetchIntygData(anyString(), any(), anyBoolean())).thenReturn(intygContentHolder);

        try {
            service.createMessage(INTYG_ID, ArendeAmne.KONTKT, "rubrik", "meddelande");
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.CERTIFICATE_REVOKED, e.getErrorCode());
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
            verifyNoInteractions(logService);
        }
    }

    @Test
    public void answerTest() throws CertificateSenderException {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";

        Arende fraga = buildArende(svarPaMeddelandeId, null);
        fraga.setAmne(ArendeAmne.OVRIGT);
        fraga.setSenasteHandelse(LocalDateTime.now());
        fraga.setStatus(Status.PENDING_INTERNAL_ACTION);
        fraga.setPatientPersonId(PERSON_ID);
        when(arendeRepository.findOneByMeddelandeId(svarPaMeddelandeId)).thenReturn(fraga);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());
        setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);
        ArendeConversationView result = service.answer(svarPaMeddelandeId, "svarstext");

        assertNotNull(result.getFraga());
        assertNotNull(result.getSvar());
        assertEquals(FIXED_TIME_INSTANT,
            Objects.requireNonNull(result.getSenasteHandelse()).toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));

        verify(arendeRepository, times(2)).save(any(Arende.class));
        verify(monitoringLog).logArendeCreated(anyString(), anyString(), isNull(), any(ArendeAmne.class), anyBoolean(), anyString());
        verify(certificateSenderService).sendMessageToRecipient(anyString(), anyString());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
        verify(arendeDraftService).delete(INTYG_ID, svarPaMeddelandeId);
        verify(logService, times(1)).logCreateMessage(any(), any(), any());
    }

    @Test(expected = WebCertServiceException.class)
    public void answerSvarsTextNullTest() {
        try {
            service.answer("svarPaMeddelandeId", null);
        } finally {
            verifyNoInteractions(arendeRepository);
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
            verifyNoInteractions(logService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void answerSvarsTextEmptyTest() {
        try {
            service.answer("svarPaMeddelandeId", "");
        } finally {
            verifyNoInteractions(arendeRepository);
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
            verifyNoInteractions(logService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void answerQuestionWithInvalidStatusTest() {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
        Arende fraga = new Arende();
        fraga.setStatus(Status.PENDING_EXTERNAL_ACTION);
        fraga.setIntygsId("123");
        when(arendeRepository.findOneByMeddelandeId(svarPaMeddelandeId)).thenReturn(fraga);
        try {
            setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);
            service.answer(svarPaMeddelandeId, "svarstext");
        } finally {
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
            verifyNoInteractions(logService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void answerPaminnQuestionTest() {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
        Arende fraga = new Arende();
        fraga.setStatus(Status.PENDING_INTERNAL_ACTION);
        fraga.setAmne(ArendeAmne.PAMINN);
        fraga.setIntygsId("asdf");
        when(arendeRepository.findOneByMeddelandeId(svarPaMeddelandeId)).thenReturn(fraga);
        try {
            setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);
            service.answer(svarPaMeddelandeId, "svarstext");
        } finally {
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
            verifyNoInteractions(logService);
        }
    }

    @Test
    public void answerKompltQuestionAuthorizedTest() {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";

        Arende fraga = buildArende(svarPaMeddelandeId, ENHET_ID);
        fraga.setStatus(Status.PENDING_INTERNAL_ACTION);
        fraga.setAmne(ArendeAmne.KOMPLT);
        fraga.setPatientPersonId(PERSON_ID);

        final var arendeList = new ArrayList<Arende>();
        arendeList.add(fraga);

        when(arendeRepository.findOneByMeddelandeId(svarPaMeddelandeId)).thenReturn(fraga);
        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(arendeList);

        WebCertUser webcertUser = createUser();

        when(webcertUserService.getUser()).thenReturn(webcertUser);
        setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);
        List<ArendeConversationView> result = service.answerKomplettering(INTYG_ID, "svarstext");

        assertNotNull(result.get(0).getFraga());
        assertNotNull(result.get(0).getSvar());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
        verify(arendeRepository, times(2)).save(any(Arende.class));
        verify(arendeDraftService).delete(INTYG_ID, svarPaMeddelandeId);
    }

    @Test
    public void answerKompltQuestionClosesAllCompletionsAsHandled() {
        final LocalDateTime originTime = LocalDateTime.parse("2018-12-06T17:47:23.128");

        final String svarPaMeddelandeId = "komplt0MeddelandeId";
        Arende komplt0 = buildArende(svarPaMeddelandeId, ENHET_ID, originTime);
        komplt0.setStatus(Status.PENDING_INTERNAL_ACTION);
        komplt0.setAmne(ArendeAmne.KOMPLT);
        komplt0.setPatientPersonId(PERSON_ID);

        final String komplt1MeddelandId = "komplt1MeddelandeId";
        Arende komplt1 = buildArende(komplt1MeddelandId, ENHET_ID, originTime.plusDays(1));
        komplt1.setStatus(Status.PENDING_INTERNAL_ACTION);
        komplt1.setAmne(ArendeAmne.KOMPLT);
        komplt1.setPatientPersonId(PERSON_ID);

        final String komplt2MeddelandId = "komplt2MeddelandeId";
        Arende komplt2 = buildArende(komplt2MeddelandId, ENHET_ID, originTime.plusDays(2));
        komplt2.setStatus(Status.PENDING_INTERNAL_ACTION);
        komplt2.setAmne(ArendeAmne.KOMPLT);
        komplt2.setPatientPersonId(PERSON_ID);

        // Den kompletteringsfraga som blir besvarad, då den är senast/nyast.
        final String avstamningMeddelandeId = "avstamningMeddelandeId";
        Arende otherSubject = buildArende(avstamningMeddelandeId, ENHET_ID, originTime.plusDays(3));
        otherSubject.setStatus(Status.PENDING_INTERNAL_ACTION);
        otherSubject.setAmne(ArendeAmne.AVSTMN);
        otherSubject.setPatientPersonId(PERSON_ID);

        final ArrayList<Arende> arendeList = new ArrayList<>();
        arendeList.add(komplt0);
        arendeList.add(komplt1);
        arendeList.add(otherSubject);
        arendeList.add(komplt2);

        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(arendeList);
        when(arendeRepository.findOneByMeddelandeId(komplt2MeddelandId)).thenReturn(komplt2);

        WebCertUser webcertUser = createUser();
        when(webcertUserService.getUser()).thenReturn(webcertUser);

        setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);
        List<ArendeConversationView> result = service.answerKomplettering(INTYG_ID, "svarstext");

        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
        verify(arendeRepository).findByIntygsId(INTYG_ID);
        verify(arendeDraftService, times(5)).delete(eq(INTYG_ID), anyString());

        assertTrue(result.stream()
            .map(ArendeConversationView::getFraga)
            .filter(f -> f.getAmne() == ArendeAmne.KOMPLT)
            .allMatch(f -> f.getStatus() == Status.CLOSED));

        assertNotNull(result.stream()
            .map(ArendeConversationView::getSvar)
            .filter(Objects::nonNull)
            .map(ArendeView::getInternReferens));
    }

    @Test
    public void answerUpdatesQuestionTest() throws CertificateSenderException {
        final String svarPaMeddelandeId = "svarPaMeddelandeId";
        Arende fraga = buildArende(svarPaMeddelandeId, null);
        fraga.setAmne(ArendeAmne.OVRIGT);
        fraga.setMeddelandeId(svarPaMeddelandeId);
        fraga.setStatus(Status.PENDING_INTERNAL_ACTION);
        fraga.setPatientPersonId(PERSON_ID);
        when(arendeRepository.findOneByMeddelandeId(svarPaMeddelandeId)).thenReturn(fraga);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());

        setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);

        ArendeConversationView result = service.answer(svarPaMeddelandeId, "svarstext");
        assertNotNull(result.getFraga());
        assertNotNull(result.getSvar());
        assertEquals(FIXED_TIME_INSTANT,
            Objects.requireNonNull(result.getSenasteHandelse()).toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));
        verify(monitoringLog).logArendeCreated(anyString(), anyString(), isNull(), any(ArendeAmne.class), anyBoolean(), anyString());
        verify(certificateSenderService).sendMessageToRecipient(anyString(), anyString());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository, times(2)).save(arendeCaptor.capture());
        verify(arendeDraftService).delete(INTYG_ID, svarPaMeddelandeId);

        Arende updatedQuestion = arendeCaptor.getAllValues().get(1);
        assertEquals(FIXED_TIME_INSTANT,
            updatedQuestion.getSenasteHandelse().toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));
        assertEquals(Status.CLOSED, updatedQuestion.getStatus());
        verify(logService, times(1)).logCreateMessage(any(), any(), any());
    }

    @Test(expected = WebCertServiceException.class)
    public void setForwardedArendeNotFoundTest() {
        try {
            setupMockForAccessService(ActionLinkType.VIDAREBEFODRA_FRAGA);

            service.setForwarded(INTYG_ID);
        } finally {
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyNoInteractions(notificationService);
        }
    }

    @Test
    public void setForwardedTrueTest() {
        final Arende arende = buildArende(MEDDELANDE_ID, ENHET_ID);
        arende.setArendeToVidareBerordrat();

        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(ImmutableList.of(arende));
        when(webcertUserService.getUser()).thenReturn(createUser());
        when(arendeRepository.saveAll(anyList())).thenReturn(ImmutableList.of(arende));
        setupMockForAccessService(ActionLinkType.VIDAREBEFODRA_FRAGA);
        final List<ArendeConversationView> arendeConversationViews = service.setForwarded(INTYG_ID);

        assertTrue(arendeConversationViews.stream()
            .allMatch(arendeConversationView -> arendeConversationView.getFraga().getVidarebefordrad()));

        verifyNoInteractions(notificationService);
    }

    @Test
    public void setForwardedWithAnswerTest() {

        final Arende arende = buildArende(MEDDELANDE_ID, ENHET_ID);
        final String svarid = "svarid";

        Arende svararende = buildArende(svarid, ENHET_ID);
        svararende.setSvarPaId(MEDDELANDE_ID);

        arende.setArendeToVidareBerordrat();

        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(ImmutableList.of(arende, svararende));
        when(webcertUserService.getUser()).thenReturn(createUser());
        when(arendeRepository.saveAll(anyList())).thenReturn(ImmutableList.of(arende));

        setupMockForAccessService(ActionLinkType.VIDAREBEFODRA_FRAGA);
        final List<ArendeConversationView> arendeConversationViews = service.setForwarded(INTYG_ID);

        assertTrue(arendeConversationViews.stream()
            .allMatch(arendeConversationView -> arendeConversationView.getFraga().getVidarebefordrad()));

        // Should contain both fraga and svar
        assertNotNull(arendeConversationViews.get(0).getFraga());
        assertNotNull(arendeConversationViews.get(0).getSvar());

        verifyNoInteractions(notificationService);
    }

    @Test(expected = WebCertServiceException.class)
    public void closeArendeAsHandledArendeNotFoundTest() {
        try {
            service.closeArendeAsHandled(MEDDELANDE_ID, INTYG_TYP);
        } finally {
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyNoInteractions(notificationService);
            verifyNoInteractions(arendeDraftService);
            verifyNoInteractions(logService);
        }
    }

    @Test
    public void closeArendeAsHandledTest() {

        Arende arende = buildArende(MEDDELANDE_ID, null);
        arende.setSkickatAv(FrageStallare.FORSAKRINGSKASSAN.getKod());
        arende.setStatus(Status.PENDING_INTERNAL_ACTION);
        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());

        setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);

        service.closeArendeAsHandled(MEDDELANDE_ID, INTYG_TYP);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        assertEquals(Status.CLOSED, arendeCaptor.getValue().getStatus());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED);
        verify(certificateEventService).createCertificateEvent(INTYG_ID, webcertUserService.getUser().getHsaId(), EventCode.HANFRFM,
            NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED.name());
        verify(fragaSvarService, never()).closeQuestionAsHandled(anyLong());
        verify(arendeDraftService).delete(INTYG_ID, MEDDELANDE_ID);
        verify(logService, times(1)).logCreateMessage(any(), any(), any());
    }

    @Test
    public void closeArendeAsHandledFk7263Test() {
        Arende arende = mock(Arende.class);
        when(arendeRepository.findOneByMeddelandeId("1")).thenReturn(arende);
        doReturn("asdf").when(arende).getIntygsId();
        setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);
        service.closeArendeAsHandled("1", "fk7263");
        verifyNoInteractions(notificationService);
        verify(fragaSvarService).closeQuestionAsHandled(1L);
        verifyNoInteractions(arendeDraftService);
        verifyNoInteractions(logService);
    }

    @Test
    public void closeArendeAsHandledFromWCNoAnswerTest() {
        Arende arende = buildArende(MEDDELANDE_ID, null);
        arende.setSkickatAv(FrageStallare.WEBCERT.getKod());
        arende.setStatus(Status.PENDING_EXTERNAL_ACTION);

        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());

        setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);
        service.closeArendeAsHandled(MEDDELANDE_ID, INTYG_TYP);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        assertEquals(Status.CLOSED, arendeCaptor.getValue().getStatus());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_HANDLED);
        verify(certificateEventService).createCertificateEvent(INTYG_ID, webcertUserService.getUser().getHsaId(), EventCode.HANFRFV,
            NotificationEvent.QUESTION_FROM_CARE_HANDLED.name());
        verify(fragaSvarService, never()).closeQuestionAsHandled(anyLong());
        verify(arendeDraftService).delete(INTYG_ID, MEDDELANDE_ID);
        verify(logService, times(1)).logCreateMessage(any(), any(), any());
    }

    @Test
    public void closeArendeAsHandledAnswerTest() {
        Arende arende = buildArende(MEDDELANDE_ID, null);
        arende.setSkickatAv(FrageStallare.WEBCERT.getKod());
        arende.setStatus(Status.ANSWERED);
        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());
        when(arendeRepository.findBySvarPaId(MEDDELANDE_ID))
            .thenReturn(Collections.singletonList(buildArende(UUID.randomUUID().toString(), null))); // there
        // are
        // answers
        setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);
        service.closeArendeAsHandled(MEDDELANDE_ID, INTYG_TYP);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        assertEquals(Status.CLOSED, arendeCaptor.getValue().getStatus());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED);
        verify(certificateEventService).createCertificateEvent(INTYG_ID, webcertUserService.getUser().getHsaId(), EventCode.HANFRFV,
            NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED.name());
        verify(fragaSvarService, never()).closeQuestionAsHandled(anyLong());
        verify(arendeDraftService).delete(INTYG_ID, MEDDELANDE_ID);
        verify(logService, times(1)).logCreateMessage(any(), any(), any());
    }

    @Test(expected = WebCertServiceException.class)
    public void openArendeAsUnhandledArendeNotFoundTest() {
        try {
            service.openArendeAsUnhandled(MEDDELANDE_ID);
        } finally {
            verify(arendeRepository, never()).save(any(Arende.class));
            verifyNoInteractions(notificationService);
            verifyNoInteractions(logService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void openArendeAsUnhandledFromFKAndAnsweredTest() {
        Arende arende = new Arende();
        arende.setSkickatAv(FrageStallare.FORSAKRINGSKASSAN.getKod());
        arende.setIntygsId("34234");
        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);
        when(arendeRepository.findBySvarPaId(MEDDELANDE_ID))
            .thenReturn(Collections.singletonList(new Arende())); // there are
        // answers
        setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);
        service.openArendeAsUnhandled(MEDDELANDE_ID);
        verifyNoMoreInteractions(notificationService);
        verify(logService, times(1)).logCreateMessage(any(), any(), any());
    }

    @Test
    public void openArendeAsUnhandledQuestionFromFK() {
        Arende arende = buildArende(MEDDELANDE_ID, null);
        arende.setSkickatAv(FrageStallare.FORSAKRINGSKASSAN.getKod());
        arende.setStatus(Status.CLOSED);

        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());

        setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);

        service.openArendeAsUnhandled(MEDDELANDE_ID);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        assertEquals(Status.PENDING_INTERNAL_ACTION, arendeCaptor.getValue().getStatus());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_UNHANDLED);
        verify(logService, times(1)).logCreateMessage(any(), any(), any());
    }

    @Test
    public void openArendeAsUnhandledAnswerFromFK() {
        Arende arende = buildArende(MEDDELANDE_ID, null);
        arende.setSkickatAv(FrageStallare.WEBCERT.getKod());
        arende.setStatus(Status.CLOSED);

        when(webcertUserService.getUser()).thenReturn(new WebCertUser());
        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);
        when(arendeRepository.findBySvarPaId(MEDDELANDE_ID))
            .thenReturn(Collections.singletonList(buildArende(UUID.randomUUID().toString(), null))); // there
        // are
        // answers
        setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);
        service.openArendeAsUnhandled(MEDDELANDE_ID);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        assertEquals(Status.ANSWERED, arendeCaptor.getValue().getStatus());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED);
        verify(logService, times(1)).logCreateMessage(any(), any(), any());
    }

    @Test
    public void openArendeAsUnhandledQuestionFromWCTest() {
        Arende arende = buildArende(MEDDELANDE_ID, null);
        arende.setSkickatAv(FrageStallare.WEBCERT.getKod());
        arende.setStatus(Status.CLOSED);

        when(arendeRepository.findOneByMeddelandeId(MEDDELANDE_ID)).thenReturn(arende);
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());

        setupMockForAccessService(ActionLinkType.BESVARA_KOMPLETTERING);
        service.openArendeAsUnhandled(MEDDELANDE_ID);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository).save(arendeCaptor.capture());
        assertEquals(Status.PENDING_EXTERNAL_ACTION, arendeCaptor.getValue().getStatus());
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_UNHANDLED);
        verify(logService, times(1)).logCreateMessage(any(), any(), any());
    }

    @Test
    public void testListSignedByForUnits() {
        final List<String> selectedUnits = Arrays.asList("enhet1", "enhet2");
        final String[] lakare1 = {"hsaid1", "namn1"};
        final String[] lakare2 = {"hsaid2", "namn2"};
        final String[] lakare3 = {"hsaid3", "namn3"};
        final String[] lakare4 = {"hsaid4", "namn4"};
        final String[] lakare4_1 = {"hsaid4", "namn4_1"};
        final List<Object[]> repoResult = Arrays.asList(lakare1, lakare2, lakare3, lakare4);
        final List<Object[]> expected = Arrays.asList(lakare1, lakare2, lakare3, lakare4);

        WebCertUser user = Mockito.mock(WebCertUser.class);
        when(user.getIdsOfSelectedVardenhet()).thenReturn(selectedUnits);
        when(webcertUserService.getUser()).thenReturn(user);
        when(arendeRepository.findSigneratAvByEnhet(selectedUnits)).thenReturn(repoResult);
        when(fragaSvarService.getFragaSvarHsaIdByEnhet(null)).thenReturn(
            Collections.singletonList(new Lakare(lakare4_1[0], lakare4_1[1])));

        List<Lakare> res = service.listSignedByForUnits(null);

        assertEquals(expected.stream().map(arr -> new Lakare((String) arr[0], (String) arr[1])).collect(Collectors.toList()), res);

        verify(webcertUserService).getUser();
        verify(arendeRepository).findSigneratAvByEnhet(selectedUnits);
    }

    @Test
    public void testListSignedByForUnitsNoHsa() {
        final List<String> selectedUnits = Arrays.asList("enhet1", "enhet2");
        final String[] lakare1 = {"hsaid1", "namn1"};
        final String[] lakare2 = {"hsaid2", "namn2"};
        final String[] lakare3 = {"hsaid3", "namn3"};
        final String[] lakare4 = {"hsaid4", "namn4"};
        final String[] lakare4_2 = {"hsaid4", "namn4_2"};
        final String[] lakare4_1 = {"hsaid4", "namn4_1"};
        final List<Object[]> repoResult = Arrays.asList(lakare1, lakare2, lakare3, lakare4, lakare4_2);
        final List<Object[]> expected = Arrays.asList(lakare1, lakare2, lakare3, lakare4);

        WebCertUser user = Mockito.mock(WebCertUser.class);
        when(user.getIdsOfSelectedVardenhet()).thenReturn(selectedUnits);
        when(webcertUserService.getUser()).thenReturn(user);
        when(arendeRepository.findSigneratAvByEnhet(selectedUnits)).thenReturn(repoResult);
        when(fragaSvarService.getFragaSvarHsaIdByEnhet(null)).thenReturn(
            Collections.singletonList(new Lakare(lakare4_1[0], lakare4_1[1])));
        when(hsaEmployeeService.getEmployee(anyString(), any())).thenThrow(WebServiceException.class);

        List<Lakare> res = service.listSignedByForUnits(null);

        assertEquals(expected.stream().map(arr -> new Lakare((String) arr[0], (String) arr[1])).collect(Collectors.toList()), res);

        verify(webcertUserService).getUser();
        verify(arendeRepository).findSigneratAvByEnhet(selectedUnits);
    }

    @Test
    public void testListSignedByForUnitsSpecifiedUnit() {
        final List<String> selectedUnit = Collections.singletonList("enhet1");
        final String[] lakare1 = {"hsaid1", "namn1"};
        final String[] lakare2 = {"hsaid2", "namn2"};
        final String[] lakare3 = {"hsaid3", "namn3"};
        final List<Object[]> repoResult = Arrays.asList(lakare1, lakare2, lakare3);

        when(webcertUserService.isAuthorizedForUnit(anyString(), eq(true))).thenReturn(true);
        when(arendeRepository.findSigneratAvByEnhet(selectedUnit)).thenReturn(repoResult);

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

        setupMockForAccessService(ActionLinkType.LASA_FRAGA);
        List<ArendeConversationView> result = service.getArenden(INTYG_ID);

        verify(arendeRepository).findByIntygsId(INTYG_ID);

        assertEquals(4, result.size());
        assertEquals(1, Objects.requireNonNull(result.get(0).getPaminnelser()).size());
        assertEquals(arendeList.get(0).getMeddelandeId(), result.get(0).getFraga().getInternReferens());
        assertEquals(arendeList.get(1).getMeddelandeId(), Objects.requireNonNull(result.get(0).getSvar()).getInternReferens());
        assertEquals(arendeList.get(2).getMeddelandeId(), Objects.requireNonNull(result.get(0).getPaminnelser()).get(0)
            .getInternReferens());
        assertEquals(arendeList.get(3).getMeddelandeId(), result.get(2).getFraga().getInternReferens());
        assertEquals(arendeList.get(4).getMeddelandeId(), result.get(1).getFraga().getInternReferens());
        assertEquals(arendeList.get(5).getMeddelandeId(), result.get(3).getFraga().getInternReferens());
        assertEquals(DECEMBER_YEAR_9999, result.get(0).getSenasteHandelse());
        assertEquals(DECEMBER_YEAR_9999, result.get(1).getSenasteHandelse());
        assertEquals(FEBRUARY, result.get(2).getSenasteHandelse());
        assertEquals(JANUARY, result.get(3).getSenasteHandelse());
    }

    @Test
    public void testGetArendeForIntygNotInWC() {
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

        final String intygsTyp = "Intygstyp";
        final IntygTypeInfo intygTypeInfo = mock(IntygTypeInfo.class);
        final IntygContentHolder intygContentHolder = mock(IntygContentHolder.class);

        final Utlatande utlatande = mock(Utlatande.class);
        doReturn(utlatande).when(modelFacade).getUtlatandeFromInternalModel(any(), any());

        final GrundData grundData = mock(GrundData.class);
        doReturn(grundData).when(utlatande).getGrundData();
        doReturn(LocalDateTime.now()).when(grundData).getSigneringsdatum();

        final HoSPersonal skapadAv = mock(HoSPersonal.class);
        doReturn(skapadAv).when(grundData).getSkapadAv();
        doReturn("Signerat av").when(skapadAv).getFullstandigtNamn();

        final se.inera.intyg.common.support.model.common.internal.Vardenhet vardenhet = mock(
            se.inera.intyg.common.support.model.common.internal.Vardenhet.class);
        doReturn(vardenhet).when(skapadAv).getVardenhet();

        final Patient patient = mock(Patient.class);
        doReturn(patient).when(grundData).getPatient();

        final Personnummer personnummer = Personnummer.createPersonnummer(PERSON_ID).orElse(null);
        doReturn(personnummer).when(patient).getPersonId();

        final var relations = new Relations();
        final var frontendRelations = new FrontendRelations();
        final var complementedByIntyg = new WebcertCertificateRelation("ID", RelationKod.KOMPLT, LocalDateTime.now(), null, false);
        frontendRelations.setComplementedByIntyg(complementedByIntyg);
        relations.setLatestChildRelations(frontendRelations);
        doReturn(relations).when(intygContentHolder).getRelations();

        doReturn(true).when(messageImportService).isImportNeeded(INTYG_ID);

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.empty());
        when(intygService.getIntygTypeInfo(INTYG_ID, null)).thenReturn(intygTypeInfo);
        when(intygTypeInfo.getIntygType()).thenReturn(intygsTyp);
        when(intygService.fetchIntygData(INTYG_ID, intygsTyp, false)).thenReturn(intygContentHolder);
        when(intygService.fetchIntygDataForInternalUse(INTYG_ID, true)).thenReturn(intygContentHolder);
        when(intygContentHolder.getUtlatande()).thenReturn(utlatande);

        List<ArendeConversationView> result = service.getArenden(INTYG_ID);

        verify(arendeRepository).findByIntygsId(INTYG_ID);
        verify(messageImportService).importMessages(INTYG_ID);

        assertEquals(4, result.size());
        assertEquals(1, Objects.requireNonNull(result.get(0).getPaminnelser()).size());
        assertEquals(arendeList.get(0).getMeddelandeId(), result.get(0).getFraga().getInternReferens());
        assertEquals(arendeList.get(1).getMeddelandeId(), Objects.requireNonNull(result.get(0).getSvar()).getInternReferens());
        assertEquals(arendeList.get(2).getMeddelandeId(), Objects.requireNonNull(result.get(0).getPaminnelser()).get(0)
            .getInternReferens());
        assertEquals(arendeList.get(3).getMeddelandeId(), result.get(2).getFraga().getInternReferens());
        assertEquals(arendeList.get(4).getMeddelandeId(), result.get(1).getFraga().getInternReferens());
        assertEquals(arendeList.get(5).getMeddelandeId(), result.get(3).getFraga().getInternReferens());
        assertEquals(DECEMBER_YEAR_9999, result.get(0).getSenasteHandelse());
        assertEquals(DECEMBER_YEAR_9999, result.get(1).getSenasteHandelse());
        assertEquals(FEBRUARY, result.get(2).getSenasteHandelse());
        assertEquals(JANUARY, result.get(3).getSenasteHandelse());
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
        final var user = createUser();
        when(webcertUserService.getUser()).thenReturn(user);
        when(webcertUserService.isAuthorizedForUnit(any(String.class), eq(true))).thenReturn(true);

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now(), null));
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now().minusDays(1), null));

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.setTotalCount(0);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setEnhetId(user.getValdVardenhet().getId());

        final var arendeListItem1 = new ArendeListItem();
        final var arendeListItem2 = new ArendeListItem();
        final var captor = ArgumentCaptor.forClass(List.class);

        when(paginationAndLoggingService.get(eq(params), any(), eq(user)))
            .thenReturn(List.of(arendeListItem1, arendeListItem2));

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(paginationAndLoggingService, times(1)).get(eq(params), captor.capture(), eq(user));
        verify(webcertUserService, times(2)).getUser();

        verify(webcertUserService).isAuthorizedForUnit(anyString(), eq(true));

        verify(arendeRepository).filterArende(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(2, response.getResults().size());
        assertEquals(2, response.getTotalCount());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    public void testFilterArendeHsaNotFound() {
        WebCertUser user = createUser();
        when(webcertUserService.getUser()).thenReturn(user);
        when(webcertUserService.isAuthorizedForUnit(any(String.class), eq(true))).thenReturn(true);

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now(), null));
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now().minusDays(1), null));

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.setTotalCount(0);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        when(hsaEmployeeService.getEmployee(anyString(), any())).thenThrow(WebServiceException.class);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        final var arendeListItem1 = new ArendeListItem();
        final var arendeListItem2 = new ArendeListItem();
        final var captor = ArgumentCaptor.forClass(List.class);

        when(paginationAndLoggingService.get(eq(params), any(), eq(user)))
            .thenReturn(List.of(arendeListItem1, arendeListItem2));

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(paginationAndLoggingService, times(1)).get(eq(params), captor.capture(), eq(user));
        verify(webcertUserService, times(2)).getUser();
        verify(arendeRepository).filterArende(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(2, response.getResults().size());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilterArendeFiltersOutNonVerifiedSekretessPatients() {
        WebCertUser webCertUser = createUser();

        Map<Personnummer, PatientDetailsResolverResponse> map = mock(Map.class);
        PatientDetailsResolverResponse patientResponse = new PatientDetailsResolverResponse();
        patientResponse.setTestIndicator(false);
        patientResponse.setDeceased(false);
        patientResponse.setProtectedPerson(SekretessStatus.UNDEFINED);
        when(map.get(any())).thenReturn(patientResponse);
        doReturn(map).when(patientDetailsResolver).getPersonStatusesForList(anyList());

        when(webcertUserService.getUser()).thenReturn(webCertUser);
        when(webcertUserService.isAuthorizedForUnit(any(), eq(true))).thenReturn(true);

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now(), null));
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now().minusDays(1), null));

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.setTotalCount(0);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setEnhetId(webCertUser.getValdVardenhet().getId());

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(patientDetailsResolver, times(1)).getPersonStatusesForList(anyList());
        verify(webcertUserService).isAuthorizedForUnit(anyString(), eq(true));

        verify(arendeRepository).filterArende(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(0, response.getResults().size());
    }

    @Test
    public void testFilterArendeWithNoEnhetsIdAsParam() {
        final var user = createUser();
        when(webcertUserService.getUser()).thenReturn(user);

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now(), null));
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now().plusDays(1), null));

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.setTotalCount(0);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();

        final var arendeListItem1 = new ArendeListItem();
        final var arendeListItem2 = new ArendeListItem();
        final var captor = ArgumentCaptor.forClass(List.class);

        when(paginationAndLoggingService.get(eq(params), any(), eq(user)))
            .thenReturn(List.of(arendeListItem1, arendeListItem2));

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(paginationAndLoggingService, times(1)).get(eq(params), captor.capture(), eq(user));
        verify(arendeRepository).filterArende(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(2, response.getResults().size());
        assertEquals(2, response.getTotalCount());
    }

    @Test
    public void testFilterArendeMergesFragaSvar() {
        final var user = createUser();
        when(webcertUserService.getUser()).thenReturn(user);

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now(), null));
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now().plusDays(1), null));

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.getResults().add(buildArendeListItem("intyg1", LocalDateTime.now().minusDays(1)));
        fsResponse.setTotalCount(1);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        final var arendeListItem1 = new ArendeListItem();
        final var arendeListItem2 = new ArendeListItem();
        final var captor = ArgumentCaptor.forClass(List.class);

        when(paginationAndLoggingService.get(eq(params), any(), eq(user)))
            .thenReturn(List.of(arendeListItem1, arendeListItem2));

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(paginationAndLoggingService, times(1)).get(eq(params), captor.capture(), eq(user));
        verify(webcertUserService, times(2)).getUser();
        verify(arendeRepository).filterArende(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(2, response.getResults().size());
        assertEquals(3, response.getTotalCount());
        assertEquals(3, captor.getValue().size());
    }

    @Test
    public void testFilterArendeInvalidStartPosition() {
        final var user = createUser();
        when(webcertUserService.getUser()).thenReturn(user);

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now(), null));
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now().plusDays(1), null));

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.getResults().add(buildArendeListItem("intyg1", LocalDateTime.now().minusDays(1)));
        fsResponse.setTotalCount(1);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setStartFrom(5);

        when(paginationAndLoggingService.get(params, Collections.emptyList(), user))
            .thenReturn(Collections.emptyList());

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(webcertUserService, times(2)).getUser();

        verify(arendeRepository).filterArende(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any(Filter.class));

        assertEquals(0, response.getResults().size());
    }

    @Test
    public void testFilterArendeSelection() {
        final var user = createUser();
        when(webcertUserService.getUser()).thenReturn(user);
        when(authoritiesHelper.getIntygstyperForPrivilege(any(UserDetails.class), any())).thenReturn(new HashSet<>());

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now(), null));
        queryResults.add(buildArende(UUID.randomUUID().toString(), LocalDateTime.now().plusDays(1), null));

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.getResults().add(buildArendeListItem("intyg1", LocalDateTime.now().minusDays(1)));
        fsResponse.setTotalCount(1);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setStartFrom(2);
        params.setPageSize(10);

        final var arendeListItem1 = new ArendeListItem();
        final var arendeListItem2 = new ArendeListItem();
        final var captor = ArgumentCaptor.forClass(List.class);

        when(paginationAndLoggingService.get(eq(params), any(), eq(user)))
            .thenReturn(List.of(arendeListItem1, arendeListItem2));

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(paginationAndLoggingService, times(1)).get(eq(params), captor.capture(), eq(user));
        verify(webcertUserService, times(2)).getUser();

        verify(arendeRepository, atLeastOnce()).filterArende(any(Filter.class));
        verify(fragaSvarService).filterFragaSvar(any());

        assertEquals(2, response.getResults().size());
        assertEquals(3, captor.getValue().size());
        assertEquals(3, response.getTotalCount());
    }

    @Test
    public void testFilterArendeSortsArendeListItemsByReceivedDate() {
        final String intygId1 = "intygId1";
        final String intygId2 = "intygId2";
        final String intygId3 = "intygId3";
        final String messageId = "arendeWithPaminnelseMEDDELANDE_ID";

        final var user = createUser();
        when(webcertUserService.getUser()).thenReturn(user);

        List<Arende> queryResults = new ArrayList<>();
        queryResults.add(buildArende(UUID.randomUUID().toString(), intygId3, LocalDateTime.now().plusDays(2), null, ENHET_ID));

        Arende arendeWithPaminnelse = buildArende(UUID.randomUUID().toString(), intygId2, LocalDateTime.now(), null, ENHET_ID);
        arendeWithPaminnelse.setMeddelandeId(messageId);
        queryResults.add(arendeWithPaminnelse);

        when(arendeRepository.filterArende(any(Filter.class))).thenReturn(queryResults);

        QueryFragaSvarResponse fsResponse = new QueryFragaSvarResponse();
        fsResponse.setResults(new ArrayList<>());
        fsResponse.getResults().add(buildArendeListItem(intygId1, LocalDateTime.now().minusDays(1)));
        fsResponse.setTotalCount(1);

        when(fragaSvarService.filterFragaSvar(any(Filter.class))).thenReturn(fsResponse);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        final var arendeListItem1 = new ArendeListItem();
        final var arendeListItem2 = new ArendeListItem();
        final var captor = ArgumentCaptor.forClass(List.class);

        when(paginationAndLoggingService.get(eq(params), any(), eq(user)))
            .thenReturn(List.of(arendeListItem1, arendeListItem2));

        QueryFragaSvarResponse response = service.filterArende(params);

        verify(paginationAndLoggingService, times(1)).get(eq(params), captor.capture(), eq(user));
        verify(webcertUserService, times(2)).getUser();

        verify(paginationAndLoggingService, times(1)).get(eq(params), captor.capture(), eq(user));

        final var arendeListCaptor = (List<ArendeListItem>) captor.getValue();

        assertEquals(3, arendeListCaptor.size());
        assertEquals(intygId3, arendeListCaptor.get(0).getIntygId());
        assertEquals(intygId2, arendeListCaptor.get(1).getIntygId());
        assertEquals(intygId1, arendeListCaptor.get(2).getIntygId());
    }

    @Test
    public void testGetArende() {
        final String messageId = "med0123";
        final String id = UUID.randomUUID().toString();
        Arende arende = buildArende(id, LocalDateTime.now(), null);

        when(arendeRepository.findOneByMeddelandeId(messageId)).thenReturn(arende);

        Arende res = service.getArende(messageId);
        assertEquals(id, res.getMeddelandeId());
    }

    @Test
    public void testCloseAllNonClosedQuestions() {
        Arende arendeFromWc = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arendeFromWc.setSkickatAv(FrageStallare.WEBCERT.getKod());
        arendeFromWc.setStatus(Status.ANSWERED);
        Arende arendeFromFk = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arendeFromFk.setSkickatAv(FrageStallare.FORSAKRINGSKASSAN.getKod());
        Arende arendeAlreadyClosed = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(),
            ENHET_ID);
        arendeAlreadyClosed.setStatus(Status.CLOSED);
        // svar and paminnelse will be ignored
        Arende arendeSvar = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arendeSvar.setSvarPaId(arendeFromWc.getMeddelandeId());
        Arende arendePaminnelse = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arendePaminnelse.setAmne(ArendeAmne.PAMINN);
        arendePaminnelse.setPaminnelseMeddelandeId(arendeFromFk.getMeddelandeId());
        when(arendeRepository.findByIntygsId(INTYG_ID))
            .thenReturn(Arrays.asList(arendeFromWc, arendeFromFk, arendeAlreadyClosed, arendeSvar, arendePaminnelse));
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());

        service.closeAllNonClosedQuestions(INTYG_ID);

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
    public void testReopenClosedCompletions() {
        Arende arende = buildArende(UUID.randomUUID().toString(), INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), ENHET_ID);
        arende.setAmne(ArendeAmne.KOMPLT);
        arende.setStatus(Status.CLOSED);
        arende.setSkickatAv(FrageStallare.FORSAKRINGSKASSAN.getKod());
        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(Collections.singletonList(arende));
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());

        service.reopenClosedCompletions(INTYG_ID);

        verify(arendeRepository).findByIntygsId(INTYG_ID);
        verify(notificationService).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_QUESTION_FROM_RECIPIENT);
        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);
        verify(arendeRepository, times(1)).save(arendeCaptor.capture());
        assertEquals(Status.PENDING_INTERNAL_ACTION, arendeCaptor.getAllValues().get(0).getStatus());
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
        when(webcertUserService.getUser()).thenReturn(new WebCertUser());

        ArgumentCaptor<Arende> arendeCaptor = ArgumentCaptor.forClass(Arende.class);

        service.closeCompletionsAsHandled(intygId, "luse");

        verify(arendeRepository).findByIntygsId(intygId);
        verify(arendeRepository, times(2)).save(arendeCaptor.capture());
        assertEquals(arende1.getMeddelandeId(), arendeCaptor.getAllValues().get(0).getMeddelandeId());
        assertEquals(Status.CLOSED, arendeCaptor.getAllValues().get(0).getStatus());
        assertEquals(arende3.getMeddelandeId(), arendeCaptor.getAllValues().get(1).getMeddelandeId());
        assertEquals(Status.CLOSED, arendeCaptor.getAllValues().get(1).getStatus());
        assertEquals(FIXED_TIME_INSTANT, arendeCaptor.getAllValues().get(0).getSenasteHandelse()
            .toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));
        assertEquals(FIXED_TIME_INSTANT, arendeCaptor.getAllValues().get(1).getSenasteHandelse()
            .toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));
        verify(notificationService, times(2)).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED);
        verifyNoInteractions(logService);
    }

    @Test
    public void testCloseCompletionsAsHandledNoMatches() {
        final String intygId = "intygId";

        when(arendeRepository.findByIntygsId(intygId)).thenReturn(new ArrayList<>());

        service.closeCompletionsAsHandled(intygId, "luse");

        verify(arendeRepository).findByIntygsId(intygId);
        verify(arendeRepository, never()).save(any(Arende.class));
        verifyNoInteractions(notificationService);
        verifyNoInteractions(logService);
    }

    @Test
    public void closeCompletionsAsHandledFk7263Test() {
        final String intygId = "intygId";
        service.closeCompletionsAsHandled(intygId, "fk7263");
        verifyNoInteractions(arendeRepository);
        verifyNoInteractions(notificationService);
        verify(fragaSvarService).closeCompletionsAsHandled(intygId);
    }

    @Test
    public void testGetNbrOfUnhandledArendenForCareUnits() {
        List<GroupableItem> queryResult = new ArrayList<>();

        when(arendeRepository.getUnhandledByEnhetIdsAndIntygstyper(anyList(), anySet())).thenReturn(queryResult);

        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("HSA1", 2L);

        when(statisticsGroupByUtil.toSekretessFilteredMap(queryResult)).thenReturn(resultMap);

        Map<String, Long> result = service.getNbrOfUnhandledArendenForCareUnits(Arrays.asList("HSA1", "HSA2"),
            Stream.of("FK7263").collect(Collectors.toSet()));

        verify(arendeRepository, times(1)).getUnhandledByEnhetIdsAndIntygstyper(anyList(), anySet());
        verify(statisticsGroupByUtil, times(1)).toSekretessFilteredMap(queryResult);

        assertEquals(1, result.size());
        assertEquals(2L, result.get("HSA1").longValue());
    }

    @Test
    public void testGetLatestMeddelandeIdForCurrentCareUnit() {
        Arende kompl = buildArende(MEDDELANDE_ID, ENHET_ID);
        kompl.setAmne(ArendeAmne.KOMPLT);
        List<Arende> arendeList = Collections.singletonList(kompl);

        when(webcertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);
        when(arendeRepository.findByIntygsId(INTYG_ID)).thenReturn(arendeList);

        assertEquals(MEDDELANDE_ID, service.getLatestMeddelandeIdForCurrentCareUnit(INTYG_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLatestMeddelandeIdForCurrentCareUnitFailsWithNoKomplArende() {
        when(webcertUserService.getUser()).thenReturn(createUser());

        service.getLatestMeddelandeIdForCurrentCareUnit(INTYG_ID);
        fail();
    }

    private Arende buildArende(String meddelandeId, String enhetId, LocalDateTime timestamp) {
        return buildArende(meddelandeId, INTYG_ID, timestamp, timestamp, enhetId);
    }

    private Arende buildArende(String meddelandeId, String enhetId) {
        return buildArende(meddelandeId, INTYG_ID, LocalDateTime.now(), LocalDateTime.now(), enhetId);
    }

    private Arende buildArende(String meddelandeId, LocalDateTime senasteHandelse, LocalDateTime timestamp) {
        return buildArende(meddelandeId, INTYG_ID, senasteHandelse, timestamp, ENHET_ID);
    }

    private Arende buildArende(String meddelandeId, String intygId, LocalDateTime senasteHandelse, LocalDateTime timestamp,
        String enhetId) {
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
        arende.setIntygTyp(INTYG_TYP);
        arende.setPatientPersonId(PNR.getPersonnummer());
        arende.setSigneratAv("Signatur");
        arende.setSistaDatumForSvar(senasteHandelse.plusDays(7).toLocalDate());
        arende.setKomplettering(komplettering);
        arende.setRubrik("rubrik");
        arende.setSkickatAv("Avsandare");
        arende.setVidarebefordrad(false);

        return arende;
    }

    private ArendeListItem buildArendeListItem(String intygId, LocalDateTime receivedDate) {
        ArendeListItem arende = new ArendeListItem();
        arende.setIntygId(intygId);
        arende.setReceivedDate(receivedDate);
        arende.setPatientId(PNR.getPersonnummer());
        arende.setSigneratAv("signeratAv");

        return arende;
    }

    private Utkast buildUtkast() {
        final String signeratAv = "signeratAv";
        final var signatur = mock(Signatur.class);

        doReturn(ISSUING_DATE).when(signatur).getSigneringsDatum();

        Utkast utkast = new Utkast();
        utkast.setIntygsId(INTYG_ID);
        utkast.setSkapadAv(new VardpersonReferens());
        utkast.getSkapadAv().setHsaId(signeratAv);
        utkast.setSignatur(signatur);
        utkast.setPatientPersonnummer(PNR);
        utkast.setModel("");
        utkast.setVardgivarId(CARE_PROVIDER_ID);

        when(utkast.getSignatur().getSigneradAv()).thenReturn(signeratAv);

        return utkast;
    }

    private WebCertUser buildUserOfRole(Role role) {

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setOrigin(UserOriginType.NORMAL.name());
        user.setHsaId("testuser");
        user.setNamn("test userman");

        Feature feature = new Feature();
        feature.setName(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR);
        feature.setGlobal(true);
        feature.setIntygstyper(ImmutableList.of(INTYG_TYP));

        user.setFeatures(ImmutableMap.of(
            AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, feature));

        Vardenhet vardenhet = new Vardenhet(ENHET_ID, "enhet");

        Vardgivare vardgivare = new Vardgivare("vardgivare", "Vardgivaren");
        vardgivare.getVardenheter().add(vardenhet);

        user.setVardgivare(Collections.singletonList(vardgivare));
        user.setValdVardenhet(vardenhet);

        return user;
    }

    private WebCertUser createUser() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);
        return buildUserOfRole(role);
    }

    private void setupMockForAccessService(ActionLinkType accessToCheck) {
        setupMockForAccessService(accessToCheck, true);
    }

    private void setupMockForAccessService(ActionLinkType accessToCheck, boolean utkastMock) {
        if (utkastMock) {
            final Utkast utkast = mock(Utkast.class);
            doReturn(Optional.of(utkast)).when(utkastRepository).findById(anyString());
        }

        final Utlatande utlatande = mock(Utlatande.class);
        doReturn(utlatande).when(modelFacade).getUtlatandeFromInternalModel(any(), any());

        final GrundData grundData = mock(GrundData.class);
        doReturn(grundData).when(utlatande).getGrundData();

        final HoSPersonal skapadAv = mock(HoSPersonal.class);
        doReturn(skapadAv).when(grundData).getSkapadAv();

        final se.inera.intyg.common.support.model.common.internal.Vardenhet vardenhet = mock(
            se.inera.intyg.common.support.model.common.internal.Vardenhet.class);
        doReturn(vardenhet).when(skapadAv).getVardenhet();

        final Patient patient = mock(Patient.class);
        doReturn(patient).when(grundData).getPatient();

        final Personnummer personnummer = Personnummer.createPersonnummer(PERSON_ID).orElse(null);
        doReturn(personnummer).when(patient).getPersonId();
    }
}