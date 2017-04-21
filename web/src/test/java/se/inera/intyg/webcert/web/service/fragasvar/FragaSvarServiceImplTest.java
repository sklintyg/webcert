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
package se.inera.intyg.webcert.web.service.fragasvar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.util.ReflectionUtils.setStaticFinalAttribute;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswer.rivtabp20.v1.SendMedicalCertificateAnswerResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.feature.ModuleFeature;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationEvent;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.FragaSvarView;

@RunWith(MockitoJUnitRunner.class)
public class FragaSvarServiceImplTest extends AuthoritiesConfigurationTestSetup {

    private static final Personnummer PATIENT_ID = new Personnummer("19121212-1212");
    private static final String INTYG_ID = "<intygsId>";

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
    private WebcertFeatureService webcertFeatureServiceMock;
    @Mock
    private NotificationService notificationServiceMock;
    @Mock
    private Logger loggerMock;
    @Mock
    private MonitoringLogService monitoringServiceMock;
    @Mock
    private UtkastRepository utkastRepository;
    @Mock
    private ArendeDraftService arendeDraftService;

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
        when(webcertFeatureServiceMock.isModuleFeatureActive(eq(ModuleFeature.HANTERA_FRAGOR.getName()), anyString())).thenReturn(true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFindByEnhetsIdSorting() {
        List<FragaSvar> unsortedList = new ArrayList<>();
        unsortedList.add(buildFragaSvar(1L, MAY, null));
        unsortedList.add(buildFragaSvar(2L, DECEMBER_YEAR_9999, null));
        unsortedList.add(buildFragaSvar(3L, null, JANUARY));
        unsortedList.add(buildFragaSvar(4L, null, AUGUST));
        when(fragasvarRepositoryMock.findByEnhetsId(any(List.class))).thenReturn(unsortedList);

        List<FragaSvar> result = service.getFragaSvar(Collections.singletonList("123"));

        assertEquals(4, result.size());

        assertEquals(2, (long) result.get(0).getInternReferens());
        assertEquals(4, (long) result.get(1).getInternReferens());
        assertEquals(1, (long) result.get(2).getInternReferens());
        assertEquals(3, (long) result.get(3).getInternReferens());
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
        intygsReferens.setPatientId(PATIENT_ID);
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

        IntygsReferens intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsId(INTYG_ID);
        intygsReferens.setIntygsTyp("fk7263");
        intygsReferens.setPatientId(PATIENT_ID);
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
        List<ArendeDraft> drafts = Arrays.asList(buildArendeDraft("intyg-1", Long.toString(1L), "text"));

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

    @Test
    public void testSaveFragaOK() throws IOException {

        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());

        // Setup when - given -then

        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), false))
                .thenReturn(getIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

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
        verify(monitoringServiceMock).logQuestionSent(anyString(), any(Long.class), anyString(), anyString(), any(Amne.class));

        assertEquals(Status.PENDING_EXTERNAL_ACTION, capture.getValue().getStatus());
        assertEquals(createUser().getValdVardenhet().getId(),
                capture.getValue().getVardperson().getEnhetsId());

        verify(arendeDraftService).delete(INTYG_ID, null);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNotSentToFK() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());

        // create mocked Utlatande from intygstjansten
        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), false))
                .thenReturn(
                        getIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());

        // test call
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                    fraga.getFrageText());
        } finally {
            verifyZeroInteractions(fragasvarRepositoryMock);
            verifyZeroInteractions(notificationServiceMock);
            verifyZeroInteractions(arendeDraftService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNoFrageText() throws IOException {
        FragaSvar fraga = buildFraga(1L, null, Amne.OVRIGT, LocalDateTime.now());
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                    fraga.getFrageText());
        } finally {
            verifyZeroInteractions(fragasvarRepositoryMock);
            verifyZeroInteractions(notificationServiceMock);
            verifyZeroInteractions(arendeDraftService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNoAmne() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", null, LocalDateTime.now());
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                    fraga.getFrageText());
        } finally {
            verifyZeroInteractions(fragasvarRepositoryMock);
            verifyZeroInteractions(notificationServiceMock);
            verifyZeroInteractions(arendeDraftService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNotAuthorizedForUnit() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());

        // create mocked Utlatande from intygstjansten
        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), false))
                .thenReturn(
                        getIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());

        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(false);
        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);

        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fraga);

        // test call
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                    fraga.getFrageText());
        } finally {
            verifyZeroInteractions(fragasvarRepositoryMock);
            verifyZeroInteractions(notificationServiceMock);
            verifyZeroInteractions(arendeDraftService);
        }

    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaOnRevokedCertificate() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());

        // create mocked Utlatande from intygstjansten
        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), false))
                .thenReturn(getRevokedIntygContentHolder());
        when(webCertUserService.getUser()).thenReturn(createUser());

        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(true))).thenReturn(true);

        // test call
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                    fraga.getFrageText());
        } finally {
            verifyZeroInteractions(fragasvarRepositoryMock);
            verifyZeroInteractions(notificationServiceMock);
            verifyZeroInteractions(arendeDraftService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaWsHTMLError() throws Exception {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());

        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), false))
                .thenReturn(getIntygContentHolder());

        // mock error with content type html
        SOAPFault soapFault = SOAPFactory.newInstance().createFault();
        soapFault.setFaultString("Response was of unexpected text/html ContentType.");

        when(webCertUserService.getUser()).thenReturn(createUser());

        when(sendQuestionToFKClientMock.sendMedicalCertificateQuestion(
                any(AttributedURIType.class),
                any(SendMedicalCertificateQuestionType.class))).thenThrow(new SOAPFaultException(soapFault));

        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                    fraga.getFrageText());
        } finally {
            verifyZeroInteractions(fragasvarRepositoryMock);
            verifyZeroInteractions(notificationServiceMock);
            verifyZeroInteractions(arendeDraftService);
        }
    }

    @Test
    public void testSetVidareBefordradOK() {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());
        // set it to false initially
        fraga.setVidarebefordrad(false);

        when(fragasvarRepositoryMock.findOne(any(Long.class))).thenReturn(fraga);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fraga);

        // test call
        service.setDispatchState(fraga.getInternReferens(), true);

        verify(fragasvarRepositoryMock).findOne(any(Long.class));
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        verifyZeroInteractions(notificationServiceMock);

        assertEquals(true, capture.getValue().getVidarebefordrad());
    }

    @Test
    public void testSaveSvarOK() throws IOException {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());

        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp(),
                false))
                        .thenReturn(getIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
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

        verify(fragasvarRepositoryMock).findOne(1L);
        verify(webCertUserService).getUser();
        verify(webCertUserService).isAuthorizedForUnit(anyString(), eq(false));
        verify(fragasvarRepositoryMock).save(fragaSvar);
        verify(sendAnswerToFKClientMock).sendMedicalCertificateAnswer(any(AttributedURIType.class),
                any(SendMedicalCertificateAnswerType.class));
        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
        verify(monitoringServiceMock).logAnswerSent(anyString(), any(Long.class), anyString(), anyString(), any(Amne.class));

        assertEquals("svarsText", result.getSvarsText());
        assertEquals(Status.CLOSED, result.getStatus());
        assertNotNull(result.getSvarSkickadDatum());
        verify(arendeDraftService).delete(INTYG_ID, Long.toString(1L));
    }

    @Test(expected = WebCertServiceException.class)
    public void testExceptionThrownWhenIntygIsUnsentToFK() throws IOException {

        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());
        String intygsId = fraga.getIntygsReferens().getIntygsId();
        // Setup when - given -then

        when(intygServiceMock.fetchIntygData(intygsId, fraga.getIntygsReferens().getIntygsTyp(), false))
                .thenReturn(getUnsentIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

        // test call
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                    fraga.getFrageText());
        } finally {
            verifyZeroInteractions(sendQuestionToFKClientMock);
            verifyZeroInteractions(notificationServiceMock);
            verifyZeroInteractions(fragasvarRepositoryMock);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testExceptionThrownWhenIntygIsRevoked() throws IOException {

        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, LocalDateTime.now());
        String intygsId = fraga.getIntygsReferens().getIntygsId();
        // Setup when - given -then

        when(intygServiceMock.fetchIntygData(intygsId, fraga.getIntygsReferens().getIntygsTyp(), false))
                .thenReturn(getRevokedIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

        // test call
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                    fraga.getFrageText());
        } finally {
            verifyZeroInteractions(sendQuestionToFKClientMock);
            verifyZeroInteractions(notificationServiceMock);
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
                .setRelations(Collections.emptyList())
                .setReplacedByRelation(null)
                .setComplementedByRelation(null)
                .setDeceased(false)
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
                .setRelations(Collections.emptyList())
                .setReplacedByRelation(null)
                .setComplementedByRelation(null)
                .setDeceased(false)
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
                .setRelations(Collections.emptyList())
                .setReplacedByRelation(null)
                .setComplementedByRelation(null)
                .setDeceased(false)
                .build();
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarWsError() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());

        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp(),
                false))
                        .thenReturn(getIntygContentHolder());
        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);
        when(fragasvarRepositoryMock.save(fragaSvar)).thenReturn(fragaSvar);

        // mock ws error response
        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.failResult("some error"));
        when(
                sendAnswerToFKClientMock.sendMedicalCertificateAnswer(any(AttributedURIType.class),
                        any(SendMedicalCertificateAnswerType.class))).thenReturn(wsResponse);

        try {
            service.saveSvar(1L, "svarsText");
        } finally {
            verifyZeroInteractions(notificationServiceMock);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarWsHTMLError() throws Exception {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());

        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp(),
                false))
                        .thenReturn(getIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
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
            verifyZeroInteractions(notificationServiceMock);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarWrongStateForAnswering() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setStatus(Status.ANSWERED);

        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp(),
                false))
                        .thenReturn(getIntygContentHolder());

        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

        try {
            service.saveSvar(1L, "svarsText");
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyZeroInteractions(notificationServiceMock);
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
    public void testSaveSvarForKompletteringAndNotDoctor() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);

        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp(),
                false))
                        .thenReturn(getIntygContentHolder());
        WebCertUser nonDoctor = createUser();
        when(webCertUserService.getUser()).thenReturn(nonDoctor);

        try {
            service.saveSvar(1L, "svarsText");
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyZeroInteractions(notificationServiceMock);
            verifyZeroInteractions(arendeDraftService);
        }
    }

    @Test
    public void testSaveSvarForKompletteringAuthorized() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);

        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp(),
                false))
                        .thenReturn(getIntygContentHolder());

        WebCertUser webcertUser = createUser();
        Privilege privilege = new Privilege();
        privilege.setRequestOrigins(new ArrayList<>());
        webcertUser.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA, privilege);
        when(webCertUserService.getUser()).thenReturn(webcertUser);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);
        when(fragasvarRepositoryMock.save(fragaSvar)).thenReturn(fragaSvar);
        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.okResult());
        when(sendAnswerToFKClientMock.sendMedicalCertificateAnswer(any(AttributedURIType.class),
                any(SendMedicalCertificateAnswerType.class))).thenReturn(wsResponse);

        FragaSvar result = service.saveSvar(1L, "svarsText");

        verify(fragasvarRepositoryMock).findOne(1L);
        verify(webCertUserService).getUser();
        verify(webCertUserService).isAuthorizedForUnit(anyString(), eq(false));
        verify(fragasvarRepositoryMock).save(fragaSvar);
        verify(sendAnswerToFKClientMock).sendMedicalCertificateAnswer(any(AttributedURIType.class),
                any(SendMedicalCertificateAnswerType.class));
        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
        verify(monitoringServiceMock).logAnswerSent(anyString(), any(Long.class), anyString(), anyString(), any(Amne.class));
        verify(arendeDraftService).delete(INTYG_ID, Long.toString(1L));
        verifyNoMoreInteractions(arendeDraftService);

        assertEquals("svarsText", result.getSvarsText());
        assertEquals(Status.CLOSED, result.getStatus());
        assertNotNull(result.getSvarSkickadDatum());
    }

    @Test
    public void testSaveSvarForKompletteringClosesAllCompletionsAsHandled() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
        fragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        FragaSvar komplt1 = buildFragaSvar(2L, LocalDateTime.now(), LocalDateTime.now());
        komplt1.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
        komplt1.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        FragaSvar komplt2 = buildFragaSvar(3L, LocalDateTime.now(), LocalDateTime.now());
        komplt2.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
        komplt2.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        FragaSvar otherSubject = buildFragaSvar(4L, LocalDateTime.now(), LocalDateTime.now());
        otherSubject.setAmne(Amne.KONTAKT);
        otherSubject.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());

        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp(),
                false))
                        .thenReturn(getIntygContentHolder());
        WebCertUser webcertUser = createUser();
        Privilege privilege = new Privilege();
        privilege.setRequestOrigins(new ArrayList<>());
        webcertUser.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA, privilege);
        when(webCertUserService.getUser()).thenReturn(webcertUser);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);
        when(fragasvarRepositoryMock.save(fragaSvar)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(komplt1)).thenReturn(komplt1);
        when(fragasvarRepositoryMock.save(komplt2)).thenReturn(komplt2);
        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.okResult());
        when(sendAnswerToFKClientMock.sendMedicalCertificateAnswer(any(AttributedURIType.class),
                any(SendMedicalCertificateAnswerType.class))).thenReturn(wsResponse);
        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId(INTYG_ID))
                .thenReturn(Arrays.asList(fragaSvar, komplt1, otherSubject, komplt2));

        FragaSvar result = service.saveSvar(1L, "svarsText");

        verify(sendAnswerToFKClientMock).sendMedicalCertificateAnswer(any(AttributedURIType.class),
                any(SendMedicalCertificateAnswerType.class));
        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.NEW_ANSWER_FROM_CARE);
        verify(monitoringServiceMock).logAnswerSent(anyString(), any(Long.class), anyString(), anyString(), any(Amne.class));

        ArgumentCaptor<FragaSvar> fragaSvarCaptor = ArgumentCaptor.forClass(FragaSvar.class);
        verify(fragasvarRepositoryMock, times(3)).save(fragaSvarCaptor.capture());
        verify(arendeDraftService, times(3)).delete(eq(INTYG_ID), anyString());

        for (FragaSvar fs : fragaSvarCaptor.getAllValues()) {
            assertEquals(Status.CLOSED, fs.getStatus());
            assertNotEquals(otherSubject.getInternReferens(), fs.getInternReferens());
            assertTrue(Arrays.asList(fragaSvar.getInternReferens(), komplt1.getInternReferens(), komplt2.getInternReferens())
                    .contains(fs.getInternReferens()));
        }
        assertEquals("svarsText", result.getSvarsText());
        assertEquals(Status.CLOSED, result.getStatus());
        assertNotNull(result.getSvarSkickadDatum());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarForPaminnelse() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setAmne(Amne.PAMINNELSE);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp(),
                false))
                        .thenReturn(getIntygContentHolder());
        when(webCertUserService.getUser()).thenReturn(createUser());

        try {
            service.saveSvar(1L, "svarsText");
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyZeroInteractions(notificationServiceMock);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarNotAuthorizedForunit() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp(),
                false))
                        .thenReturn(getIntygContentHolder());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(false);

        try {
            service.saveSvar(1L, "svarsText");
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyZeroInteractions(notificationServiceMock);
            verifyZeroInteractions(arendeDraftService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveMissingSvarsText() {
        try {
            service.saveSvar(1L, null);
        } finally {
            verifyZeroInteractions(fragasvarRepositoryMock);
            verifyZeroInteractions(notificationServiceMock);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveMissingIntygsId() {
        try {
            service.saveSvar(null, "svarsText");
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyZeroInteractions(notificationServiceMock);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarIntygNotFound() {
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(null);
        try {
            service.saveSvar(1L, "svarsText");
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyZeroInteractions(notificationServiceMock);
        }

    }

    @Test
    public void testCloseQuestionToFKAsHandledOK() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setFrageStallare(FrageStallare.WEBCERT.getKod());
        fragaSvar.setFrageText("Fråga till FK");
        fragaSvar.setStatus(Status.PENDING_EXTERNAL_ACTION);

        ArgumentCaptor<FragaSvar> fsCapture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(fsCapture.capture())).thenReturn(fragaSvar);

        service.closeQuestionAsHandled(1L);

        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_HANDLED);
        verify(fragasvarRepositoryMock).findOne(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        assertEquals(Status.CLOSED, fsCapture.getValue().getStatus());
        verifyZeroInteractions(arendeDraftService);
    }

    @Test
    public void testCloseQuestionFromFKAsHandledOK() {
        // This is a question from FK that we have no intention to answer so we close it
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        fragaSvar.setFrageText("Fråga från FK till WC");
        fragaSvar.setStatus(Status.PENDING_INTERNAL_ACTION);

        ArgumentCaptor<FragaSvar> fsCapture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(fsCapture.capture())).thenReturn(fragaSvar);

        service.closeQuestionAsHandled(1L);

        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED);
        verify(fragasvarRepositoryMock).findOne(1L);
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
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(fsCapture.capture())).thenReturn(fragaSvar);

        service.closeQuestionAsHandled(1L);

        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED);
        verify(fragasvarRepositoryMock).findOne(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));

        assertEquals(Status.CLOSED, fsCapture.getValue().getStatus());
        verifyZeroInteractions(arendeDraftService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testCloseAshandledNotFound() {
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(null);
        try {
            service.closeQuestionAsHandled(1L);
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyZeroInteractions(notificationServiceMock);
        }
    }

    @Test
    public void testOpenAsUnhandledFromFK() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        fragaSvar.setFrageText("Fråga till WC från FK");
        fragaSvar.setStatus(Status.CLOSED);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);

        service.openQuestionAsUnhandled(1L);

        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_UNHANDLED);
        verify(fragasvarRepositoryMock).findOne(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        assertEquals(Status.PENDING_INTERNAL_ACTION, capture.getValue().getStatus());
    }

    @Test
    public void testOpenAsUnhandledToFKNoAnsw() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setFrageStallare(FrageStallare.WEBCERT.getKod());
        fragaSvar.setFrageText("Fråga till FK från WC");
        fragaSvar.setStatus(Status.CLOSED);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);

        service.openQuestionAsUnhandled(1L);

        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_UNHANDLED);
        verify(fragasvarRepositoryMock).findOne(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
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
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);

        service.openQuestionAsUnhandled(1L);

        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED);
        verify(fragasvarRepositoryMock).findOne(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        assertEquals(Status.ANSWERED, capture.getValue().getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void testOpenAsUnhandledNotFound() {
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(null);
        try {
            service.openQuestionAsUnhandled(1L);
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyZeroInteractions(notificationServiceMock);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testOpenAsUnhandledInvalidState() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        fragaSvar.setFrageText("Fråga från FK");
        fragaSvar.setSvarsText("Svar till FK från WC");
        fragaSvar.setStatus(Status.CLOSED);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);

        try {
            service.openQuestionAsUnhandled(1L);
        } finally {
            verify(fragasvarRepositoryMock, never()).save(any(FragaSvar.class));
            verifyZeroInteractions(notificationServiceMock);
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
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(true))).thenReturn(true);

        List<FragaSvar> queryResults = new ArrayList<>();
        queryResults.add(buildFragaSvar(1L, MAY, null));
        queryResults.add(buildFragaSvar(2L, MAY, null));

        when(fragasvarRepositoryMock.filterFragaSvar(any(Filter.class))).thenReturn(queryResults);
        when(fragasvarRepositoryMock.filterCountFragaSvar(any(Filter.class))).thenReturn(queryResults.size());

        Filter params = new Filter();
        params.setEnhetsIds(Arrays.asList(webCertUser.getValdVardenhet().getId()));

        QueryFragaSvarResponse response = service.filterFragaSvar(params);

        verify(fragasvarRepositoryMock).filterFragaSvar(any(Filter.class));
        verify(fragasvarRepositoryMock).filterCountFragaSvar(any(Filter.class));

        assertNotNull(response);
        assertEquals(2, response.getResults().size());
    }

    @Test
    public void testFilterFragaSvarWithNoEnhetsIdAsParam() {

        when(webCertUserService.getUser()).thenReturn(createUser());

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
        queryResult.add(new Object[] { "HSA-1_ID", "NAMN1" });
        queryResult.add(new Object[] { "HSA-2_ID", "NAMN2" });
        queryResult.add(new Object[] { "HSA-3_ID", "NAMN3" });
        queryResult.add(new Object[] { "HSA-4_ID", "NAMN4" });

        when(fragasvarRepositoryMock.findDistinctFragaSvarHsaIdByEnhet(Matchers.anyListOf(String.class))).thenReturn(queryResult);
        List<Lakare> result = service.getFragaSvarHsaIdByEnhet(enhetsId);
        ArgumentCaptor<String> capture = ArgumentCaptor.forClass(String.class);
        verify(webCertUserService).isAuthorizedForUnit(capture.capture(), eq(false));

        verify(fragasvarRepositoryMock).findDistinctFragaSvarHsaIdByEnhet(Matchers.anyListOf(String.class));
        assertEquals(enhetsId, capture.getValue());
        assertEquals(4, result.size());
    }

    @Test
    public void testGetNbrOfUnhandledFragaSvarForCareUnits() {

        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(new Object[] { "HSA1", 2L });
        queryResult.add(new Object[] { "HSA2", 4L });

        when(fragasvarRepositoryMock.countUnhandledGroupedByEnhetIdsAndIntygstyper(Mockito.anyListOf(String.class),
                Mockito.anySetOf(String.class)))
                        .thenReturn(queryResult);

        Map<String, Long> res = service.getNbrOfUnhandledFragaSvarForCareUnits(Arrays.asList("HSA1", "HSA2"),
                Stream.of("fk7263").collect(Collectors.toSet()));

        verify(fragasvarRepositoryMock).countUnhandledGroupedByEnhetIdsAndIntygstyper(Mockito.anyListOf(String.class),
                Mockito.anySetOf(String.class));

        assertNotNull(res);
        assertEquals(2, res.size());
    }

    @Test(expected = WebCertServiceException.class)
    public void intygWithoutFragaSvarDoesNotAcceptFraga() {
        FragaSvar fragaSvar = buildFragaSvar(1L, LocalDateTime.now(), LocalDateTime.now());
        fragaSvar.getIntygsReferens().setIntygsTyp("ts-bas");

        when(webcertFeatureServiceMock.isModuleFeatureActive(ModuleFeature.HANTERA_FRAGOR.getName(), "ts-bas")).thenReturn(false);

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
        when(fragasvarRepositoryMock.save(any(FragaSvar.class))).thenAnswer(invocation -> (FragaSvar) invocation.getArguments()[0]);

        service.closeAllNonClosedQuestions(intygId);

        verify(fragasvarRepositoryMock).findByIntygsReferensIntygsId(intygId);
        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED);
        verify(notificationServiceMock).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED);
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
        when(fragasvarRepositoryMock.save(fsCapture.capture())).thenAnswer(invocation -> (FragaSvar) invocation.getArguments()[0]);

        service.closeCompletionsAsHandled(intygId);

        verify(fragasvarRepositoryMock).findByIntygsReferensIntygsId(intygId);
        verify(fragasvarRepositoryMock, times(2)).save(any(FragaSvar.class));
        assertEquals(fragaSvar1.getInternReferens(), fsCapture.getAllValues().get(0).getInternReferens());
        assertEquals(Status.CLOSED, fsCapture.getAllValues().get(0).getStatus());
        assertEquals(fragaSvar3.getInternReferens(), fsCapture.getAllValues().get(1).getInternReferens());
        assertEquals(Status.CLOSED, fsCapture.getAllValues().get(1).getStatus());
        verify(notificationServiceMock, times(2)).sendNotificationForQAs(INTYG_ID, NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED);
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
        verifyZeroInteractions(notificationServiceMock);
        verifyZeroInteractions(arendeDraftService);
    }

    private WebCertUser createUser() {

        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));
        user.setHsaId("testuser");
        user.setNamn("test userman");

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
