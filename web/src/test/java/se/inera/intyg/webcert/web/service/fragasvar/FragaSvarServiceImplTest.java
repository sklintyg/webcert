/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.fragasvar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MoreCollectors;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswer.rivtabp20.v1.SendMedicalCertificateAnswerResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationEvent;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.StatisticsGroupByUtil;
import se.inera.intyg.webcert.web.web.controller.api.dto.FragaSvarView;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.*;
import static se.inera.intyg.webcert.web.util.ReflectionUtils.setStaticFinalAttribute;

@RunWith(MockitoJUnitRunner.class)
public class FragaSvarServiceImplTest extends AuthoritiesConfigurationTestSetup {

    private static final String INTYG_ID = "<intygsId>";
    private static final String PATIENT_ID = "19121212-1212";
    private static final String HSA_ID = "testuser";

    private static final Personnummer PNR = Personnummer.createPersonnummer(PATIENT_ID).orElse(null);

    private static final LocalDateTime JANUARY = LocalDateTime.parse("2013-01-12T11:22:11");
    private static final LocalDateTime MAY = LocalDateTime.parse("2013-05-01T11:11:11");
    private static final LocalDateTime AUGUST = LocalDateTime.parse("2013-08-02T11:11:11");
    private static final LocalDateTime DECEMBER_YEAR_9999 = LocalDateTime.parse("9999-12-11T10:22:00");

    @Mock
    private FragaSvarRepository fragasvarRepositoryMock;
    @Mock
    private SendMedicalCertificateAnswerResponderInterface sendAnswerToFKClientMock;
    @Mock
    private SendMedicalCertificateQuestionResponderInterface sendQuestionToFKClientMock;
    @Mock
    private IntygService intygServiceMock;
    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private NotificationService notificationServiceMock;
    @Mock
    private CertificateEventService certificateEventService;
    @Mock
    private Logger loggerMock;
    @Mock
    private MonitoringLogService monitoringServiceMock;
    @Mock
    private UtkastRepository utkastRepository;
    @Mock
    private ArendeDraftService arendeDraftService;
    @Mock
    private StatisticsGroupByUtil statisticsGroupByUtil;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private AuthoritiesHelper authoritiesHelper;

    @Spy
    private ObjectMapper objectMapper = new CustomObjectMapper();

    @InjectMocks
    private FragaSvarServiceImpl service;

    @Before
    public void setUpLoggerFactory() throws Exception {
        setStaticFinalAttribute(FragaSvarServiceImpl.class, "LOGGER", loggerMock);
    }

    @Before
    public void setupCommonBehaviour() {
        when(authoritiesHelper.isFeatureActive(eq(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR), anyString())).thenReturn(true);
    }

    @Test
    public void testFindByIntygSorting() {
        List<FragaSvar> unsortedList = new ArrayList<>();
        unsortedList.add(buildFragaSvar(1L, MAY, null));
        unsortedList.add(buildFragaSvar(2L, DECEMBER_YEAR_9999, null));
        unsortedList.add(buildFragaSvar(3L, null, JANUARY));
        unsortedList.add(buildFragaSvar(4L, null, AUGUST));
        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId((any(String.class)))).thenReturn(unsortedList);
        when(webCertUserService.getUser()).thenReturn(createUser());
        when(utkastRepository.findAllByRelationIntygsId(any(String.class))).thenReturn(Collections.emptyList());

        List<FragaSvarView> result = service.getFragaSvar("intygsid");

        assertEquals(4, result.size());

        assertEquals(2, (long) result.get(0).getFragaSvar().getInternReferens());
        assertEquals(4, (long) result.get(1).getFragaSvar().getInternReferens());
        assertEquals(1, (long) result.get(2).getFragaSvar().getInternReferens());
        assertEquals(3, (long) result.get(3).getFragaSvar().getInternReferens());

    }

    @Test
    public void testNumberOfUnhandledFragaSvarForCareUnits() {
        List<GroupableItem> queryResult = new ArrayList<>();

        when(fragasvarRepositoryMock.getUnhandledWithEnhetIdsAndIntygstyper(anyList(), anySet())).thenReturn(queryResult);

        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("HSA1", 2L);

        when(statisticsGroupByUtil.toSekretessFilteredMap(queryResult)).thenReturn(resultMap);

        Map<String, Long> result = service.getNbrOfUnhandledFragaSvarForCareUnits(Arrays.asList(
            "HSA1", "HSA2"),
            Stream.of("fk7263").collect(Collectors.toSet()));

        verify(fragasvarRepositoryMock, times(1)).getUnhandledWithEnhetIdsAndIntygstyper(anyList(), anySet());
        verify(statisticsGroupByUtil, times(1)).toSekretessFilteredMap(queryResult);

        assertEquals(1, result.size());
        assertEquals(2L, result.get("HSA1").longValue());
    }

    private FragaSvar buildFragaSvar(Long id, LocalDateTime fragaSkickadDatum, LocalDateTime svarSkickadDatum) {
        FragaSvar f = new FragaSvar();
        f.setStatus(Status.PENDING_INTERNAL_ACTION);
        f.setAmne(Amne.OVRIGT);
        f.setExternReferens("<fk-extern-referens>");
        f.setInternReferens(id);
        f.setFrageSkickadDatum(fragaSkickadDatum);
        f.setFrageText("frageText");
        f.setSvarSkickadDatum(svarSkickadDatum);

        IntygsReferens intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsId(INTYG_ID);
        intygsReferens.setIntygsTyp("fk7263");
        intygsReferens.setPatientId(PNR);
        f.setIntygsReferens(intygsReferens);
        f.setKompletteringar(new HashSet<>());
        f.setVardperson(new Vardperson());
        f.getVardperson().setEnhetsId("enhet");
        return f;
    }

    private FragaSvar buildFraga(Long id, String frageText, Amne amne, LocalDateTime fragaSkickadDatum) {
        FragaSvar f = new FragaSvar();
        f.setStatus(Status.PENDING_INTERNAL_ACTION);
        f.setAmne(amne);
        f.setExternReferens("<fk-extern-referens>");
        f.setInternReferens(id);
        f.setFrageSkickadDatum(fragaSkickadDatum);
        f.setFrageText(frageText);
        f.setVardAktorHsaId("vardaktor-hsa-id");

        IntygsReferens intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsId(INTYG_ID);
        intygsReferens.setIntygsTyp("fk7263");
        intygsReferens.setPatientId(PNR);
        f.setIntygsReferens(intygsReferens);
        f.setKompletteringar(new HashSet<>());
        f.setVardperson(new Vardperson());
        f.getVardperson().setEnhetsId("enhet");
        return f;
    }

    @Test
    public void testGetFragaSvarForIntyg() {
        List<FragaSvar> fragaSvarList = new ArrayList<>();
        fragaSvarList.add(buildFragaSvar(1L, DECEMBER_YEAR_9999, DECEMBER_YEAR_9999));
        fragaSvarList.add(buildFragaSvar(2L, LocalDateTime.now(), LocalDateTime.now()));
        fragaSvarList.add(buildFragaSvar(3L, JANUARY, JANUARY));
        List<ArendeDraft> drafts = Collections.singletonList(buildArendeDraft("intyg-1", Long.toString(1L), "text"));

        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId("intyg-1")).thenReturn(new ArrayList<>(fragaSvarList));
        when(webCertUserService.getUser()).thenReturn(createUser());
        when(utkastRepository.findAllByRelationIntygsId(any(String.class))).thenReturn(Collections.emptyList());
        when(arendeDraftService.listAnswerDrafts("intyg-1")).thenReturn(drafts);

        List<FragaSvarView> fragaSvarViewList = service.getFragaSvar("intyg-1");
        List<FragaSvar> resultFragaSvarList = fragaSvarViewList.stream()
            .map(FragaSvarView::getFragaSvar)
            .collect(Collectors.toList());

        verify(fragasvarRepositoryMock).findByIntygsReferensIntygsId("intyg-1");
        verify(webCertUserService).getUser();
        verify(arendeDraftService).listAnswerDrafts("intyg-1");

        assertEquals(3, resultFragaSvarList.size());
        assertEquals(fragaSvarList, resultFragaSvarList);

        List<String> resultArendeDrafts = fragaSvarViewList.stream()
            .map(FragaSvarView::getAnswerDraft)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        assertEquals(drafts.stream().map(ArendeDraft::getText).collect(Collectors.toList()), resultArendeDrafts);
    }

    @Test
    public void testGetFragaSvarFilteringForIntyg() {
        List<FragaSvar> fragaSvarList = new ArrayList<>();
        fragaSvarList.add(buildFragaSvar(1L, DECEMBER_YEAR_9999, DECEMBER_YEAR_9999));
        fragaSvarList.add(buildFragaSvar(2L, LocalDateTime.now(), LocalDateTime.now()));
        fragaSvarList.add(buildFragaSvar(3L, JANUARY, JANUARY));

        // the second question/answer pair was sent to a unit out of the current user's range -> has to be filtered
        fragaSvarList.get(1).getVardperson().setEnhetsId("another unit without my range");

        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId("intyg-1")).thenReturn(new ArrayList<>(fragaSvarList));
        when(webCertUserService.getUser()).thenReturn(createUser());
        when(utkastRepository.findAllByRelationIntygsId(any(String.class))).thenReturn(Collections.emptyList());

        List<FragaSvarView> result = service.getFragaSvar("intyg-1");

        verify(fragasvarRepositoryMock).findByIntygsReferensIntygsId("intyg-1");
        verify(webCertUserService).getUser();

        assertEquals(2, result.size());
        assertEquals(fragaSvarList.get(0), result.get(0).getFragaSvar());
        assertEquals(fragaSvarList.get(2), result.get(1).getFragaSvar());
    }

    @Test(expected = WebCertServiceException.class)
    public void testGetFragaSvarWithSekretessPatientForVardadminThrowsException() {
        List<FragaSvar> fragaSvarList = new ArrayList<>();
        fragaSvarList.add(buildFragaSvar(1L, DECEMBER_YEAR_9999, DECEMBER_YEAR_9999));
        fragaSvarList.add(buildFragaSvar(2L, LocalDateTime.now(), LocalDateTime.now()));
        fragaSvarList.add(buildFragaSvar(3L, JANUARY, JANUARY));

        // the second question/answer pair was sent to a unit out of the current user's range -> has to be filtered
        fragaSvarList.get(1).getVardperson().setEnhetsId("another unit without my range");

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.TRUE);
        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId("intyg-1")).thenReturn(new ArrayList<>(fragaSvarList));
        when(webCertUserService.getUser()).thenReturn(buildUserOfRole(AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_ADMIN)));

        service.getFragaSvar("intyg-1");
    }

    @Test(expected = WebCertServiceException.class)
    public void testGetFragaSvarWithPuFailsForVardadminThrowsException() {
        List<FragaSvar> fragaSvarList = new ArrayList<>();
        fragaSvarList.add(buildFragaSvar(1L, DECEMBER_YEAR_9999, DECEMBER_YEAR_9999));
        fragaSvarList.add(buildFragaSvar(2L, LocalDateTime.now(), LocalDateTime.now()));
        fragaSvarList.add(buildFragaSvar(3L, JANUARY, JANUARY));

        // the second question/answer pair was sent to a unit out of the current user's range -> has to be filtered
        fragaSvarList.get(1).getVardperson().setEnhetsId("another unit without my range");

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.UNDEFINED);
        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId("intyg-1")).thenReturn(new ArrayList<>(fragaSvarList));
        when(webCertUserService.getUser()).thenReturn(createUser());

        service.getFragaSvar("intyg-1");
    }

    @Test
    public void testSaveFragaOK() {

        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());

        // Setup when - given -then

        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp()))
            .thenReturn(getIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(eq("VardenhetY"), eq(false))).thenReturn(true);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fraga);

        // mock ws ok response
        SendMedicalCertificateQuestionResponseType wsResponse = new SendMedicalCertificateQuestionResponseType();
        wsResponse.setResult(ResultOfCallUtil.okResult());
        when(sendQuestionToFKClientMock.sendMedicalCertificateQuestion(
            any(AttributedURIType.class),
            any(SendMedicalCertificateQuestionType.class))).thenReturn(wsResponse);

        // test call
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
            fraga.getFrageText());

        verify(webCertUserService).getUser();
        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_QUESTION_FROM_CARE);
        verify(webCertUserService).isAuthorizedForUnit(anyString(), eq(false));
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        verify(sendQuestionToFKClientMock).sendMedicalCertificateQuestion(any(AttributedURIType.class),
            any(SendMedicalCertificateQuestionType.class));
        verify(monitoringServiceMock).logQuestionSent(anyString(), eq(1L), anyString(), anyString(), eq(Amne.OVRIGT));

        assertEquals(Status.PENDING_EXTERNAL_ACTION, capture.getValue().getStatus());
        assertEquals(createUser().getValdVardenhet().getId(),
            capture.getValue().getVardperson().getEnhetsId());

        verify(certificateEventService).createCertificateEvent(INTYG_ID, HSA_ID, EventCode.NYFRFV, Amne.OVRIGT.name());
        verify(arendeDraftService).delete(INTYG_ID, null);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNotSentToFK() {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());

        // create mocked Utlatande from intygstjansten
        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp()))
            .thenReturn(
                getIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());

        // test call
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                fraga.getFrageText());
        } finally {
            verifyNoInteractions(fragasvarRepositoryMock);
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
            verifyNoInteractions(arendeDraftService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNoFrageText() {
        try {
            service.saveNewQuestion("intygId", "intygTyp", Amne.OVRIGT, null);
        } finally {
            verifyNoInteractions(fragasvarRepositoryMock);
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
            verifyNoInteractions(arendeDraftService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNoAmne() {
        try {
            service.saveNewQuestion("intygId", "intygTyp", null, "frageText");
        } finally {
            verifyNoInteractions(fragasvarRepositoryMock);
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
            verifyNoInteractions(arendeDraftService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNotAuthorizedForUnit() {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());

        // create mocked Utlatande from intygstjansten
        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp()))
            .thenReturn(
                getIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());

        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(false);
        // test call
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                fraga.getFrageText());
        } finally {
            verifyNoInteractions(fragasvarRepositoryMock);
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
            verifyNoInteractions(arendeDraftService);
        }

    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaOnRevokedCertificate() {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());

        // create mocked Utlatande from intygstjansten
        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp()))
            .thenReturn(getRevokedIntygContentHolder());
        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

        // test call
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                fraga.getFrageText());
        } finally {
            verifyNoInteractions(fragasvarRepositoryMock);
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
            verifyNoInteractions(arendeDraftService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaWsHTMLError() throws Exception {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());

        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp()))
            .thenReturn(getIntygContentHolder());

        // mock error with content type html
        SOAPFault soapFault = SOAPFactory.newInstance().createFault();
        soapFault.setFaultString("Response was of unexpected text/html ContentType.");

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);
        when(fragasvarRepositoryMock.save(any(FragaSvar.class))).thenReturn(fraga);

        when(sendQuestionToFKClientMock.sendMedicalCertificateQuestion(
            any(AttributedURIType.class),
            any(SendMedicalCertificateQuestionType.class))).thenThrow(new SOAPFaultException(soapFault));

        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                fraga.getFrageText());
        } finally {
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
            verifyNoInteractions(arendeDraftService);
        }
    }

    @Test
    public void testSetVidareBefordradOK() {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId(any(String.class))).thenReturn(ImmutableList.of(fraga));

        assertFalse(fraga.getVidarebefordrad());
        List<FragaSvar> fragaSvarList = service.setVidareBefordrad(fraga.getIntygsReferens().getIntygsId());

        verify(fragasvarRepositoryMock).saveAll(anyList());
        verifyNoInteractions(notificationServiceMock);

        assertTrue(fragaSvarList
            .stream()
            .allMatch(FragaSvar::getVidarebefordrad));
    }

    @Test
    public void testSaveSvarOK() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.of(fragaSvar));
        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);
        when(fragasvarRepositoryMock.save(fragaSvar)).thenReturn(fragaSvar);

        // mock ws ok response
        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.okResult());
        when(
            sendAnswerToFKClientMock.sendMedicalCertificateAnswer(any(AttributedURIType.class),
                any(SendMedicalCertificateAnswerType.class))).thenReturn(wsResponse);

        FragaSvar result = service.saveSvar(1L, "svarsText");

        verify(fragasvarRepositoryMock).findById(1L);
        verify(webCertUserService, times(2)).getUser();
        verify(webCertUserService).isAuthorizedForUnit(anyString(), eq(false));
        verify(fragasvarRepositoryMock).save(fragaSvar);
        verify(sendAnswerToFKClientMock).sendMedicalCertificateAnswer(any(AttributedURIType.class),
            any(SendMedicalCertificateAnswerType.class));
        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
        verify(monitoringServiceMock).logAnswerSent(anyString(), any(Long.class), anyString(), anyString(), any(Amne.class));
        verify(certificateEventService)
            .createCertificateEvent(INTYG_ID, HSA_ID, EventCode.HANFRFM, NotificationEvent.NEW_ANSWER_FROM_CARE.name());

        assertEquals("svarsText", result.getSvarsText());
        assertEquals(Status.CLOSED, result.getStatus());
        assertNotNull(result.getSvarSkickadDatum());
        verify(arendeDraftService).delete(INTYG_ID, Long.toString(1L));
    }

    @Test
    public void testAnswerKomplOK() {

        final LocalDateTime date = LocalDateTime.of(2017, 5, 22, 5, 20);
        final String svarsText = "bra text";

        FragaSvar komplFragaSvar = buildFragaSvar(1L, date, date);
        komplFragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
        komplFragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());

        FragaSvar ovrigtFragaSvar = buildFragaSvar(2L, date.minusDays(1), date.minusDays(1));
        ovrigtFragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());

        komplFragaSvar.setStatus(Status.CLOSED);

        ResultOfCall resultOfCall = new ResultOfCall();
        resultOfCall.setResultCode(ResultCodeEnum.OK);
        SendMedicalCertificateAnswerResponseType sendMedicalResponse = new SendMedicalCertificateAnswerResponseType();
        sendMedicalResponse.setResult(resultOfCall);

        doReturn(createUser())
            .when(webCertUserService)
            .getUser();

        doReturn(Lists.newArrayList(komplFragaSvar, ovrigtFragaSvar))
            .when(fragasvarRepositoryMock)
            .findByIntygsReferensIntygsId(eq(INTYG_ID));

        doReturn(komplFragaSvar)
            .when(fragasvarRepositoryMock)
            .save(komplFragaSvar);

        doReturn(sendMedicalResponse)
            .when(sendAnswerToFKClientMock)
            .sendMedicalCertificateAnswer(
                any(AttributedURIType.class),
                any(SendMedicalCertificateAnswerType.class)
            );

        final List<FragaSvarView> fragaSvarViews = service.answerKomplettering(INTYG_ID, svarsText);

        assertEquals(2, fragaSvarViews.size());

        FragaSvar komplResult = fragaSvarViews.stream()
            .map(FragaSvarView::getFragaSvar)
            .filter(fs -> fs.getAmne() == Amne.KOMPLETTERING_AV_LAKARINTYG)
            .collect(MoreCollectors.onlyElement());

        FragaSvar otherResult = fragaSvarViews.stream()
            .map(FragaSvarView::getFragaSvar)
            .filter(fs -> fs.getAmne() == Amne.OVRIGT)
            .collect(MoreCollectors.onlyElement());

        assertEquals(Status.CLOSED, komplResult.getStatus());
        assertEquals(Status.PENDING_INTERNAL_ACTION, otherResult.getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void testAnswerKomplNotPermitted() {

        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
        fragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());

        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.okResult());

        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);
        when(fragasvarRepositoryMock.findById(eq(1L))).thenReturn(Optional.of(fragaSvar));

        service.saveSvar(fragaSvar.getInternReferens(), "svarsText");
    }

    @Test(expected = WebCertServiceException.class)
    public void testExceptionThrownWhenIntygIsUnsentToFK() {

        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());
        String intygsId = fraga.getIntygsReferens().getIntygsId();
        // Setup when - given -then

        when(intygServiceMock.fetchIntygData(intygsId, fraga.getIntygsReferens().getIntygsTyp()))
            .thenReturn(getUnsentIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

        // test call
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                fraga.getFrageText());
        } finally {
            verifyNoInteractions(sendQuestionToFKClientMock);
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
            verifyNoInteractions(fragasvarRepositoryMock);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testExceptionThrownWhenIntygIsRevoked() {

        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());
        String intygsId = fraga.getIntygsReferens().getIntygsId();
        // Setup when - given -then

        when(intygServiceMock.fetchIntygData(intygsId, fraga.getIntygsReferens().getIntygsTyp()))
            .thenReturn(getRevokedIntygContentHolder());
        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

        // test call
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                fraga.getFrageText());
        } finally {
            verifyNoInteractions(sendQuestionToFKClientMock);
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
            verify(fragasvarRepositoryMock, times(0)).save(any(FragaSvar.class));
        }
    }

    private IntygContentHolder getIntygContentHolder() {
        List<se.inera.intyg.common.support.model.Status> status = new ArrayList<>();
        status.add(new se.inera.intyg.common.support.model.Status(CertificateState.RECEIVED, "HSVARD", LocalDateTime.now()));
        status.add(new se.inera.intyg.common.support.model.Status(CertificateState.SENT, "FKASSA", LocalDateTime.now()));
        return IntygContentHolder.builder()
            .setContents("<external-json/>")
            .setUtlatande(getUtlatande())
            .setStatuses(status)
            .setRevoked(false)
            .setRelations(new Relations())
            .setDeceased(false)
            .setSekretessmarkering(false)
            .setPatientNameChangedInPU(false)
            .setPatientAddressChangedInPU(false)
            .setTestIntyg(false)
            .build();
    }

    private IntygContentHolder getUnsentIntygContentHolder() {
        List<se.inera.intyg.common.support.model.Status> status = new ArrayList<>();
        status.add(new se.inera.intyg.common.support.model.Status(CertificateState.RECEIVED, "HSVARD", LocalDateTime.now()));
        return IntygContentHolder.builder()
            .setContents("<external-json/>")
            .setUtlatande(getUtlatande())
            .setStatuses(status)
            .setRevoked(false)
            .setRelations(new Relations())
            .setDeceased(false)
            .setSekretessmarkering(false)
            .setPatientNameChangedInPU(false)
            .setPatientAddressChangedInPU(false)
            .setTestIntyg(false)
            .build();
    }

    private IntygContentHolder getRevokedIntygContentHolder() {
        List<se.inera.intyg.common.support.model.Status> status = new ArrayList<>();
        status.add(new se.inera.intyg.common.support.model.Status(CertificateState.RECEIVED, "HSVARD", LocalDateTime.now()));
        status.add(new se.inera.intyg.common.support.model.Status(CertificateState.SENT, "FKASSA", LocalDateTime.now()));
        status.add(new se.inera.intyg.common.support.model.Status(CertificateState.CANCELLED, "HSVARD", LocalDateTime.now()));
        return IntygContentHolder.builder()
            .setContents("<external-json/>")
            .setUtlatande(getUtlatande())
            .setStatuses(status)
            .setRevoked(true)
            .setRelations(new Relations())
            .setDeceased(false)
            .setSekretessmarkering(false)
            .setPatientNameChangedInPU(false)
            .setPatientAddressChangedInPU(false)
            .setTestIntyg(false)
            .build();
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarWsError() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());

        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.of(fragaSvar));
        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);
        when(fragasvarRepositoryMock.save(fragaSvar)).thenReturn(fragaSvar);

        // mock ws error response
        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.failResult("some error"));
        when(sendAnswerToFKClientMock.sendMedicalCertificateAnswer(any(AttributedURIType.class),
            any(SendMedicalCertificateAnswerType.class))).thenReturn(wsResponse);

        try {
            service.saveSvar(1L, "svarsText");
        } finally {
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarWsHTMLError() throws Exception {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.of(fragaSvar));
        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);
        when(fragasvarRepositoryMock.save(fragaSvar)).thenReturn(fragaSvar);

        // mock error with content type html
        SOAPFault soapFault = SOAPFactory.newInstance().createFault();
        soapFault.setFaultString("Response was of unexpected text/html ContentType.");

        when(sendAnswerToFKClientMock.sendMedicalCertificateAnswer(any(AttributedURIType.class),
            any(SendMedicalCertificateAnswerType.class))).thenThrow(new SOAPFaultException(soapFault));

        try {
            service.saveSvar(1L, "svarsText");
        } finally {
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarWrongStateForAnswering() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setStatus(Status.ANSWERED);

        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.of(fragaSvar));
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

        try {
            service.saveSvar(1L, "svarsText");
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
        }
    }

    private Fk7263Utlatande getUtlatande() {
        // create mocked Utlatande from intygstjansten
        try {
            return new CustomObjectMapper().readValue(new ClassPathResource(
                "FragaSvarServiceImplTest/utlatande.json").getFile(), Fk7263Utlatande.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarForPaminnelse() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setAmne(Amne.PAMINNELSE);
        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.of(fragaSvar));

        try {
            service.saveSvar(1L, "svarsText");
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarNotAuthorizedForunit() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.of(fragaSvar));
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(false);

        try {
            service.saveSvar(1L, "svarsText");
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
            verifyNoInteractions(arendeDraftService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveMissingSvarsText() {
        try {
            service.saveSvar(1L, null);
        } finally {
            verifyNoInteractions(fragasvarRepositoryMock);
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveMissingIntygsId() {
        try {
            service.saveSvar(null, "svarsText");
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarIntygNotFound() {
        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        try {
            service.saveSvar(1L, "svarsText");
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
        }

    }

    @Test
    public void testCloseQuestionToFKAsHandledOK() {
        when(webCertUserService.getUser()).thenReturn(createUser());

        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setFrageStallare(FrageStallare.WEBCERT.getKod());
        fragaSvar.setFrageText("Fråga till FK");
        fragaSvar.setStatus(Status.PENDING_EXTERNAL_ACTION);

        ArgumentCaptor<FragaSvar> fsCapture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.of(fragaSvar));
        when(fragasvarRepositoryMock.save(fsCapture.capture())).thenReturn(fragaSvar);

        service.closeQuestionAsHandled(1L);

        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_HANDLED);
        verify(certificateEventService)
            .createCertificateEvent(anyString(), anyString(), eq(EventCode.HANFRFV), anyString());
        verify(fragasvarRepositoryMock).findById(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        assertEquals(Status.CLOSED, fsCapture.getValue().getStatus());
        verifyNoInteractions(arendeDraftService);
    }

    @Test
    public void testCloseQuestionFromFKAsHandledOK() {
        // This is a question from FK that we have no intention to answer so we close it
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        fragaSvar.setFrageText("Fråga från FK till WC");
        fragaSvar.setStatus(Status.PENDING_INTERNAL_ACTION);

        ArgumentCaptor<FragaSvar> fsCapture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.of(fragaSvar));
        when(fragasvarRepositoryMock.save(fsCapture.capture())).thenReturn(fragaSvar);
        when(webCertUserService.getUser()).thenReturn(createUser());

        service.closeQuestionAsHandled(1L);

        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED);
        verify(certificateEventService)
            .createCertificateEvent(anyString(), anyString(), eq(EventCode.HANFRFM), anyString());
        verify(fragasvarRepositoryMock).findById(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));

        assertEquals(Status.CLOSED, fsCapture.getValue().getStatus());
        verify(arendeDraftService).delete(INTYG_ID, Long.toString(1L));
    }

    @Test
    public void testCloseAnsweredQuestionToFKAsHandledOK() {
        // This is a question that we sent to FK that has been answered so we now have to close it
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setFrageStallare(FrageStallare.WEBCERT.getKod());
        fragaSvar.setFrageText("Fråga från WC till FK");
        fragaSvar.setStatus(Status.ANSWERED);
        fragaSvar.setSvarsText("Med ett svar från FK");

        ArgumentCaptor<FragaSvar> fsCapture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.of(fragaSvar));
        when(fragasvarRepositoryMock.save(fsCapture.capture())).thenReturn(fragaSvar);
        when(webCertUserService.getUser()).thenReturn(createUser());

        service.closeQuestionAsHandled(1L);

        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED);
        verify(certificateEventService)
            .createCertificateEvent(anyString(), anyString(), eq(EventCode.HANFRFV), anyString());
        verify(fragasvarRepositoryMock).findById(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));

        assertEquals(Status.CLOSED, fsCapture.getValue().getStatus());
        verifyNoInteractions(arendeDraftService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testCloseAshandledNotFound() {
        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        try {
            service.closeQuestionAsHandled(1L);
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
        }
    }

    @Test
    public void testOpenAsUnhandledFromFK() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        fragaSvar.setFrageText("Fråga till WC från FK");
        fragaSvar.setStatus(Status.CLOSED);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.of(fragaSvar));
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);
        when(webCertUserService.getUser()).thenReturn(createUser());

        service.openQuestionAsUnhandled(1L);

        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_UNHANDLED);
        verify(fragasvarRepositoryMock).findById(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        verify(certificateEventService).createCertificateEvent(anyString(), anyString(), eq(EventCode.NYFRFM),
            eq(NotificationEvent.QUESTION_FROM_RECIPIENT_UNHANDLED.name()));
        assertEquals(Status.PENDING_INTERNAL_ACTION, capture.getValue().getStatus());
    }

    @Test
    public void testOpenAsUnhandledToFKNoAnsw() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setFrageStallare(FrageStallare.WEBCERT.getKod());
        fragaSvar.setFrageText("Fråga till FK från WC");
        fragaSvar.setStatus(Status.CLOSED);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.of(fragaSvar));
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);
        when(webCertUserService.getUser()).thenReturn(createUser());

        service.openQuestionAsUnhandled(1L);

        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_UNHANDLED);
        verify(fragasvarRepositoryMock).findById(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        verify(certificateEventService).createCertificateEvent(anyString(), anyString(), eq(EventCode.HANFRFV),
            eq(NotificationEvent.QUESTION_FROM_CARE_UNHANDLED.name()));
        assertEquals(Status.PENDING_EXTERNAL_ACTION, capture.getValue().getStatus());
    }

    @Test
    public void testOpenAsUnhandledFromWCWithAnswer() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setFrageStallare(FrageStallare.WEBCERT.getKod());
        fragaSvar.setFrageText("Fråga till FK från WC");
        fragaSvar.setSvarsText("Med ett svar från FK");
        fragaSvar.setStatus(Status.CLOSED);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.of(fragaSvar));
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);
        when(webCertUserService.getUser()).thenReturn(createUser());

        service.openQuestionAsUnhandled(1L);

        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED);
        verify(fragasvarRepositoryMock).findById(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        verify(certificateEventService).createCertificateEvent(anyString(), anyString(), eq(EventCode.NYSVFM),
            eq(NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED.name()));
        assertEquals(Status.ANSWERED, capture.getValue().getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void testOpenAsUnhandledNotFound() {
        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        try {
            service.openQuestionAsUnhandled(1L);
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testOpenAsUnhandledInvalidState() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        fragaSvar.setFrageText("Fråga från FK");
        fragaSvar.setSvarsText("Svar till FK från WC");
        fragaSvar.setStatus(Status.CLOSED);

        when(fragasvarRepositoryMock.findById(1L)).thenReturn(Optional.of(fragaSvar));

        try {
            service.openQuestionAsUnhandled(1L);
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyNoInteractions(notificationServiceMock);
            verifyNoInteractions(certificateEventService);
        }
    }

    @Test
    public void testVerifyEnhetsAuthOK() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), any(Boolean.class))).thenReturn(true);
        service.verifyEnhetsAuth("enhet");

        verify(webCertUserService).isAuthorizedForUnit(anyString(), any(Boolean.class));
    }

    @Test(expected = WebCertServiceException.class)
    public void testVerifyEnhetsAuthFail() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), any(Boolean.class))).thenReturn(false);
        service.verifyEnhetsAuth("<doesnt-exist>");
    }

    @Test
    public void testFilterFragaSvarWithEnhetsIdAsParam() {

        WebCertUser webCertUser = createUser();

        List<FragaSvar> queryResults = new ArrayList<>();
        queryResults.add(buildFragaSvar(1L, MAY, null));
        queryResults.add(buildFragaSvar(2L, MAY, null));

        when(fragasvarRepositoryMock.filterFragaSvar(any(Filter.class))).thenReturn(queryResults);
        when(fragasvarRepositoryMock.filterCountFragaSvar(any(Filter.class))).thenReturn(queryResults.size());

        Filter params = new Filter();
        params.setEnhetsIds(Collections.singletonList(webCertUser.getValdVardenhet().getId()));

        QueryFragaSvarResponse response = service.filterFragaSvar(params);

        verify(fragasvarRepositoryMock).filterFragaSvar(any(Filter.class));
        verify(fragasvarRepositoryMock).filterCountFragaSvar(any(Filter.class));

        assertNotNull(response);
        assertEquals(2, response.getResults().size());
    }

    @Test
    public void testFilterFragaSvarWithNoEnhetsIdAsParam() {
        List<FragaSvar> queryResults = new ArrayList<>();
        queryResults.add(buildFragaSvar(1L, MAY, null));
        queryResults.add(buildFragaSvar(2L, MAY, null));

        when(fragasvarRepositoryMock.filterFragaSvar(any(Filter.class))).thenReturn(queryResults);
        when(fragasvarRepositoryMock.filterCountFragaSvar(any(Filter.class))).thenReturn(queryResults.size());

        QueryFragaSvarResponse response = service.filterFragaSvar(new Filter());

        verify(fragasvarRepositoryMock).filterFragaSvar(any(Filter.class));
        verify(fragasvarRepositoryMock).filterCountFragaSvar(any(Filter.class));

        assertNotNull(response);
        assertEquals(2, response.getResults().size());
    }

    @Test
    public void testGetMDByEnhetsIdOK() {
        String enhetsId = "enhet";
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(new Object[]{"HSA-1_ID", "NAMN1"});
        queryResult.add(new Object[]{"HSA-2_ID", "NAMN2"});
        queryResult.add(new Object[]{"HSA-3_ID", "NAMN3"});
        queryResult.add(new Object[]{"HSA-4_ID", "NAMN4"});

        when(fragasvarRepositoryMock.findDistinctFragaSvarHsaIdByEnhet(anyList())).thenReturn(queryResult);
        List<Lakare> result = service.getFragaSvarHsaIdByEnhet(enhetsId);
        ArgumentCaptor<String> capture = ArgumentCaptor.forClass(String.class);
        verify(webCertUserService).isAuthorizedForUnit(capture.capture(), eq(false));

        verify(fragasvarRepositoryMock).findDistinctFragaSvarHsaIdByEnhet(anyList());
        assertEquals(enhetsId, capture.getValue());
        assertEquals(4, result.size());
    }

    @Test(expected = WebCertServiceException.class)
    public void intygWithoutFragaSvarDoesNotAcceptFraga() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.getIntygsReferens().setIntygsTyp("ts-bas");

        when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, "ts-bas")).thenReturn(false);

        service.processIncomingQuestion(fragaSvar);
        fail("Processing should have thrown an exception");
    }

    @Test
    public void testCloseAllNonClosedQuestions() {
        FragaSvar fragaSvarFromWc = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvarFromWc.setFrageStallare(FrageStallare.WEBCERT.getKod());
        fragaSvarFromWc.setStatus(Status.ANSWERED);
        FragaSvar fragaSvarFromFk = buildFragaSvar(2L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvarFromFk.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        FragaSvar fragaSvarAlreadyClosed = buildFragaSvar(2L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvarAlreadyClosed.setStatus(Status.CLOSED);
        final String intygId = "intygId";
        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId(intygId))
            .thenReturn(Arrays.asList(fragaSvarFromWc, fragaSvarFromFk, fragaSvarAlreadyClosed));
        when(fragasvarRepositoryMock.save(any(FragaSvar.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(webCertUserService.getUser()).thenReturn(createUser());

        service.closeAllNonClosedQuestions(intygId);

        verify(fragasvarRepositoryMock).findByIntygsReferensIntygsId(intygId);
        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED);
        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED);

        ArgumentCaptor<EventCode> eventCaptor = ArgumentCaptor.forClass(EventCode.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(certificateEventService, times(2))
            .createCertificateEvent(anyString(), anyString(), eventCaptor.capture(), messageCaptor.capture());
        assertEquals(EventCode.HANFRFV, eventCaptor.getAllValues().get(0));
        assertEquals(NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED.name(), messageCaptor.getAllValues().get(0));
        assertEquals(EventCode.HANFRFM, eventCaptor.getAllValues().get(1));
        assertEquals(NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED.name(), messageCaptor.getAllValues().get(1));

        ArgumentCaptor<FragaSvar> fragaSvarCaptor = ArgumentCaptor.forClass(FragaSvar.class);
        verify(fragasvarRepositoryMock, times(2)).save(fragaSvarCaptor.capture());
        assertEquals(Status.CLOSED, fragaSvarCaptor.getAllValues().get(0).getStatus());
        assertEquals(Status.CLOSED, fragaSvarCaptor.getAllValues().get(1).getStatus());
        verify(arendeDraftService).delete(INTYG_ID, Long.toString(2L));
        verifyNoMoreInteractions(arendeDraftService);
    }

    @Test
    public void testCloseCompletionsAsHandled() {
        final String intygId = "intygId";
        FragaSvar fragaSvar1 = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar1.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
        fragaSvar1.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        FragaSvar fragaSvar2 = buildFragaSvar(2L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar2.setAmne(Amne.KONTAKT);
        fragaSvar2.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        FragaSvar fragaSvar3 = buildFragaSvar(3L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar3.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
        fragaSvar3.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        FragaSvar fragaSvar4 = buildFragaSvar(4L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar4.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
        fragaSvar4.setStatus(Status.CLOSED); // already closed
        fragaSvar4.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());

        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId(intygId))
            .thenReturn(Arrays.asList(fragaSvar1, fragaSvar2, fragaSvar3, fragaSvar4));
        ArgumentCaptor<FragaSvar> fsCapture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.save(fsCapture.capture())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(webCertUserService.getUser()).thenReturn(createUser());

        service.closeCompletionsAsHandled(intygId);

        verify(fragasvarRepositoryMock).findByIntygsReferensIntygsId(intygId);
        verify(fragasvarRepositoryMock, times(2)).save(any(FragaSvar.class));
        assertEquals(fragaSvar1.getInternReferens(), fsCapture.getAllValues().get(0).getInternReferens());
        assertEquals(Status.CLOSED, fsCapture.getAllValues().get(0).getStatus());
        assertEquals(fragaSvar3.getInternReferens(), fsCapture.getAllValues().get(1).getInternReferens());
        assertEquals(Status.CLOSED, fsCapture.getAllValues().get(1).getStatus());
        verify(notificationServiceMock, times(2)).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED);
        verify(certificateEventService, times(2)).createCertificateEvent(anyString(), anyString(), eq(EventCode.HANFRFM),
            eq(NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED.name()));
        verify(arendeDraftService, times(2)).delete(eq(INTYG_ID), anyString());
        verifyNoMoreInteractions(arendeDraftService);
    }

    @Test
    public void testCloseCompletionsAsHandledNoMatches() {
        final String intygId = "intygId";

        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId(intygId)).thenReturn(new ArrayList<>());

        service.closeCompletionsAsHandled(intygId);

        verify(fragasvarRepositoryMock).findByIntygsReferensIntygsId(intygId);
        verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
        verifyNoInteractions(certificateEventService);
        verifyNoInteractions(notificationServiceMock);
        verifyNoInteractions(arendeDraftService);
    }

    private WebCertUser createUser() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);
        return buildUserOfRole(role);
    }

    private WebCertUser buildUserOfRole(Role role) {
        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setOrigin("NORMAL");
        user.setHsaId(HSA_ID);
        user.setNamn("test userman");

        Feature feature = new Feature();
        feature.setName(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR);
        feature.setGlobal(true);
        feature.setIntygstyper(ImmutableList.of("fk7263"));

        user.setFeatures(ImmutableMap.of(
            AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, feature));

        Vardenhet vardenhet = new Vardenhet("enhet", "Enhet");

        Vardgivare vardgivare = new Vardgivare("vardgivare", "Vardgivaren");
        vardgivare.getVardenheter().add(vardenhet);

        user.setVardgivare(Collections.singletonList(vardgivare));
        user.setValdVardenhet(vardenhet);

        return user;
    }

    private ArendeDraft buildArendeDraft(String intygId, String questionId, String text) {
        ArendeDraft draft = new ArendeDraft();
        draft.setIntygId(intygId);
        draft.setQuestionId(questionId);
        draft.setText(text);
        return draft;
    }
}
