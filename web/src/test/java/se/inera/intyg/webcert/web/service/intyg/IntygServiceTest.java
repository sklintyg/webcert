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
package se.inera.intyg.webcert.web.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.ws.WebServiceException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoType;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacadeException;
import se.inera.intyg.webcert.web.service.intyg.decorator.IntygRelationHelper;
import se.inera.intyg.webcert.web.service.intyg.decorator.UtkastIntygDecorator;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsResponse;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.FragorOchSvarCreator;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.IntygsStatus;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class IntygServiceTest {

    private static final String HSA_ID = "HSA-123";
    private static final String CREATED_BY_NAME = "Läkare Läkarsson";
    private static final String SENAST_SPARAD_NAME = "Spara Sparasson";
    private static final String CERTIFICATE_ID = "123";
    private static final String CERTIFICATE_TYPE = "fk7263";
    private static final String CERTIFICATE_TYPE_VERSION_1_0 = "1.0";
    private static final String LOGICAL_ADDRESS = "<logicalAddress>";
    private static final String USER_REFERENCE = "some-ref";
    private static final String REFERENCE = "reference";
    private static final String PERSON_ID = "19121212-1212";

    private static final Personnummer PERSNR = Personnummer.createPersonnummer(PERSON_ID).orElse(null);
    @Mock
    SelectableVardenhet vardgivare;
    @Mock
    WebCertUser user;
    @Mock
    IntegrationParameters intParam;
    private ListCertificatesForCareResponseType listResponse;
    private VardpersonReferens vardpersonReferens;
    private String json;
    @Mock
    private IntygModuleRegistry moduleRegistry;
    @Mock
    private ModuleApi moduleApi;
    @Mock
    private AuthoritiesHelper authoritiesHelper;
    @Mock
    private ListCertificatesForCareResponderInterface listCertificatesForCareResponder;
    @Mock
    private IntygRelationHelper intygRelationHelper;
    @Mock
    private IntygModuleFacade moduleFacade;
    @Mock
    private UtkastRepository utkastRepository;
    @Mock
    private CertificateEventService certificateEventService;
    @Mock
    private LogService logservice;
    @Mock
    private LogRequestFactory logRequestFactory;
    @Mock
    private WebCertUser webcertUser;
    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private MonitoringLogService mockMonitoringService;
    @Mock
    private CertificateRelationService certificateRelationService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CertificateSenderService certificateSenderService;
    @Mock
    private ArendeService arendeService;
    @Mock
    private FragorOchSvarCreator fragorOchSvarCreator;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private UtkastIntygDecorator utkastIntygDecorator;
    @Mock
    private ReferensService referensService;
    @Spy
    private ObjectMapper objectMapper = new CustomObjectMapper();
    @Mock
    private GetCertificateTypeInfoResponderInterface getCertificateTypeInfoService;
    @Mock
    private CertificateAccessServiceHelper certificateAccessServiceHelper;
    @Mock
    private IntygTextsService intygTextsService;
    @Mock
    private PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    @Mock
    private CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @InjectMocks
    private IntygDraftsConverter intygConverter = new IntygDraftsConverter();

    @InjectMocks
    private IntygServiceImpl intygService;

    @Before
    public void setupIntygstjanstResponse() throws Exception {
        vardpersonReferens = new VardpersonReferens();
        vardpersonReferens.setHsaId(HSA_ID);
        vardpersonReferens.setNamn(CREATED_BY_NAME);

        json = Files.readString(Path.of(ClassLoader.getSystemResource("IntygServiceTest/utlatande.json").toURI()));
        Fk7263Utlatande utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);

        CertificateMetaData metaData = new CertificateMetaData();
        metaData.setStatus(new ArrayList<>());

        CertificateResponse certificateResponse = new CertificateResponse(json, utlatande, metaData, false);
        when(moduleFacade.getCertificate(any(String.class), any(String.class), anyString())).thenReturn(certificateResponse);
        when(moduleFacade.getUtlatandeFromInternalModel(anyString(), anyString(), any())).thenReturn(utlatande);
    }

    @Before
    public void setupIntygstjanstListResponse() throws Exception {
        ClassPathResource response = new ClassPathResource("IntygServiceTest/response-list-certificates.xml");

        JAXBContext context = JAXBContext.newInstance(ListCertificatesForCareResponseType.class);
        listResponse = context.createUnmarshaller()
            .unmarshal(new StreamSource(response.getInputStream()), ListCertificatesForCareResponseType.class)
            .getValue();

        when(intygRelationHelper.getRelationsForIntyg(anyString())).thenReturn(new Relations());
        doNothing().when(intygRelationHelper).decorateIntygListWithRelations(anyList());
        when(certificateRelationService.getRelations(anyString())).thenReturn(new Relations());
    }

    @Before
    public void setupDefaultAuthorization() {
        Set<String> set = new HashSet<>();
        set.add("fk7263");

        when(webCertUserService.getUser()).thenReturn(webcertUser);
        when(webcertUser.getOrigin()).thenReturn(UserOriginType.NORMAL.name());
        when(webcertUser.getHsaId()).thenReturn((HSA_ID));
        when(webcertUser.getParameters())
            .thenReturn(new IntegrationParameters(USER_REFERENCE, "", "", "", "", "", "", "", "", false, false, false, true, null));
        when(webCertUserService.isAuthorizedForUnit(any(String.class), any(String.class), eq(true))).thenReturn(true);
        when(authoritiesHelper.getIntygstyperForPrivilege(any(WebCertUser.class), anyString())).thenReturn(set);
    }

    @Before
    public void setupLogicalAddress() {
        intygService.setLogicalAddress(LOGICAL_ADDRESS);
    }

    @Before
    public void IntygServiceConverter() throws Exception {
        when(moduleRegistry.getModuleApi(or(isNull(), anyString()), or(isNull(), anyString()))).thenReturn(moduleApi);
        json = Files.readString(Path.of(ClassLoader.getSystemResource("IntygServiceTest/utlatande.json").toURI()));
        Fk7263Utlatande utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(moduleApi.updateBeforeViewing(anyString(), any(Patient.class), any())).thenAnswer(invocation -> invocation.getArgument(0));

        // use reflection to set IntygDraftsConverter in IntygService
        Field field = IntygServiceImpl.class.getDeclaredField("intygConverter");
        field.setAccessible(true);
        field.set(intygService, intygConverter);

        when(moduleRegistry.getModuleIdFromExternalId(anyString()))
            .thenAnswer(invocation -> ((String) invocation.getArguments()[0]).toLowerCase());
    }

    @Before
    public void setupPUService() {
        when(patientDetailsResolver.getPersonFromPUService(any(Personnummer.class)))
            .thenReturn(getPersonSvar(false));
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString()))
            .thenReturn(buildPatient(false, false));
    }

    @Before
    public void setupPatientDetailsResolver() {
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
    }

    @Before
    public void setupReferensService() {
        when(referensService.getReferensForIntygsId(anyString())).thenReturn(REFERENCE);
    }

    @Before
    public void setupLookForIntygTypeInfo() throws IOException {
        // For when finding it in utkast
        when(utkastRepository.findById(anyString())).thenReturn(Optional.of(getIntyg(CERTIFICATE_ID,
            LocalDateTime.now(), null)));
        // For when finding it via lookup in IT
        GetCertificateTypeInfoResponseType typeInfo = new GetCertificateTypeInfoResponseType();
        TypAvIntyg typAvIntyg = new TypAvIntyg();
        typAvIntyg.setCode(CERTIFICATE_TYPE);
        typeInfo.setTyp(typAvIntyg);
        typeInfo.setTypVersion(CERTIFICATE_TYPE_VERSION_1_0);
        when(getCertificateTypeInfoService.getCertificateTypeInfo(anyString(), any(GetCertificateTypeInfoType.class))).thenReturn(typeInfo);
        when(intygTextsService.isLatestMajorVersion(any(String.class), any(String.class))).thenReturn(true);
    }

    @Before
    public void setupPdlLogging() {
        when(logRequestFactory.createLogRequestFromUtlatande(any(Utlatande.class))).thenReturn(LogRequest.builder().build());
        when(logRequestFactory.createLogRequestFromUtlatande(any(Utlatande.class), anyString())).thenReturn(LogRequest.builder().build());
        when(logRequestFactory.createLogRequestFromUtlatande(any(Utlatande.class), anyBoolean())).thenReturn(LogRequest.builder().build());
    }

    private void setupUserAndVardgivare() {
        when(webCertUserService.getUser()).thenReturn(user);
        when(user.getOrigin()).thenReturn(UserOriginType.DJUPINTEGRATION.name());
        when(user.getValdVardgivare()).thenReturn(vardgivare);
        when(vardgivare.getId()).thenReturn("VardgivarId");
        when(user.getParameters()).thenReturn(intParam);
    }

    @Test
    public void testCheckSjfSameEnhetSjf() {
        setupUserAndVardgivare();
        when(intParam.isSjf()).thenReturn(true);
        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
        verify(logRequestFactory).createLogRequestFromUtlatande(any(Utlatande.class), eq(false));
    }

    @Test
    public void testCheckSjfSameEnhet() {
        setupUserAndVardgivare();
        when(intParam.isSjf()).thenReturn(false);
        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
        verify(logRequestFactory).createLogRequestFromUtlatande(any(Utlatande.class), eq(false));
    }

    @Test
    public void testCheckSjfDifferentEnhetIsSjf() {
        setupUserAndVardgivare();
        when(vardgivare.getId()).thenReturn("12345");
        when(intParam.isSjf()).thenReturn(true);
        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
        verify(logRequestFactory).createLogRequestFromUtlatande(any(Utlatande.class), eq(true));
    }

    @Test
    public void testCheckSjfDifferentEnhetNotSjf() {
        setupUserAndVardgivare();
        when(vardgivare.getId()).thenReturn("12345");
        when(intParam.isSjf()).thenReturn(false);
        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
        verify(logRequestFactory).createLogRequestFromUtlatande(any(Utlatande.class), eq(false));
    }


    @Test
    public void testFetchIntyg() throws Exception {

        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);

        // ensure that correctcall is made to intygstjanst
        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
        verify(patientDetailsResolver).isAvliden(any(Personnummer.class));

        assertEquals(CERTIFICATE_ID, Objects.requireNonNull(intygData.getUtlatande()).getId());
        assertEquals(PERSON_ID, intygData.getUtlatande().getGrundData().getPatient().getPersonId().getPersonnummerWithDash());
        assertFalse(intygData.isDeceased());
    }

    @Test(expected = WebCertServiceException.class)
    public void testFetchIntygWithFailingIntygstjanst() throws IntygModuleFacadeException {

        when(moduleFacade.getCertificate(any(String.class), any(String.class), anyString())).thenThrow(new IntygModuleFacadeException(""));

        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
    }

    @Test
    public void testFetchIntygData() throws Exception {

        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);

        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
        verify(utkastRepository).findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE);
        verify(intygRelationHelper).getRelationsForIntyg(CERTIFICATE_ID);
    }

    @Test
    public void testFetchIntygDataUtkastNotFound() throws Exception {

        IntygContentHolder res = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        assertNull(res.getCreated());

        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
        verify(utkastRepository).findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE);
        verify(utkastRepository).findById(CERTIFICATE_ID);
        verify(intygRelationHelper).getRelationsForIntyg(CERTIFICATE_ID);
        verifyNoMoreInteractions(moduleFacade, utkastRepository, logservice, mockMonitoringService, intygRelationHelper);
    }

    @Test
    public void testFetchIntygDataWithCreatedTimestamp() throws Exception {

        final LocalDateTime timestamp = LocalDateTime.of(2010, 11, 12, 13, 14, 15);
        Utkast utkast = new Utkast();
        utkast.setSkapad(timestamp);
        when(utkastRepository.findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE)).thenReturn(utkast);

        IntygContentHolder res = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        assertEquals(timestamp, res.getCreated());

        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
        verify(utkastRepository).findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE);
        verify(utkastRepository).findById(CERTIFICATE_ID);
        verify(intygRelationHelper).getRelationsForIntyg(CERTIFICATE_ID);
        verifyNoMoreInteractions(moduleFacade, utkastRepository, logservice, mockMonitoringService, intygRelationHelper);
    }

    @Test
    public void testFetchIntygDataWithRelation() throws Exception {
        setupUserAndVardgivare();
        IntygContentHolder res = intygService.fetchIntygDataWithRelations(CERTIFICATE_ID, CERTIFICATE_TYPE);

        assertNotNull(res);
        assertNotNull(res.getRelations());

        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
        verify(logservice).logReadIntyg(any(LogRequest.class));
        verify(mockMonitoringService).logIntygRead(CERTIFICATE_ID, CERTIFICATE_TYPE);
        verify(intygRelationHelper).getRelationsForIntyg(CERTIFICATE_ID);
    }

    @Test
    public void testFetchIntygDataWithRelationNotFoundInIT() throws Exception {
        setupUserAndVardgivare();
        when(moduleFacade.getCertificate(any(String.class), any(String.class), anyString())).thenThrow(new IntygModuleFacadeException(""));
        when(utkastRepository.findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE))
            .thenReturn(getIntyg(CERTIFICATE_ID, null, null));

        IntygContentHolder res = intygService.fetchIntygDataWithRelations(CERTIFICATE_ID, CERTIFICATE_TYPE);

        assertNotNull(res);
        assertNotNull(res.getRelations());

        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
        verify(utkastRepository).findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE);
        verify(logservice).logReadIntyg(any(LogRequest.class));
        verify(mockMonitoringService).logIntygRead(CERTIFICATE_ID, CERTIFICATE_TYPE);
        verify(certificateRelationService).getRelations(CERTIFICATE_ID);
    }

    @Test
    public void testFetchIntygDataWithRelationITUnavailable() throws Exception {
        setupUserAndVardgivare();
        when(moduleFacade.getCertificate(any(String.class), any(String.class), anyString())).thenThrow(new WebServiceException(""));
        when(utkastRepository.findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE))
            .thenReturn(getIntyg(CERTIFICATE_ID, null, null));

        IntygContentHolder res = intygService.fetchIntygDataWithRelations(CERTIFICATE_ID, CERTIFICATE_TYPE);

        assertNotNull(res);
        assertNotNull(res.getRelations());

        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
        verify(utkastRepository).findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE);
        verify(logservice).logReadIntyg(any(LogRequest.class));
        verify(mockMonitoringService).logIntygRead(CERTIFICATE_ID, CERTIFICATE_TYPE);
        verify(certificateRelationService).getRelations(CERTIFICATE_ID);
    }

    @Test
    public void testFetchIntygDataForInternalUse() throws Exception {
        IntygContentHolder res = intygService.fetchIntygDataForInternalUse(CERTIFICATE_ID, true);

        assertNotNull(res);
        assertNotNull(res.getRelations());

        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
        verify(intygRelationHelper).getRelationsForIntyg(CERTIFICATE_ID);
        verifyNoInteractions(logservice, mockMonitoringService);
    }

    @Test
    public void testListIntyg() {
        final String enhetsId = "enhet-1";

        // setup intygstjansten WS mock to return intyg information
        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class)))
            .thenReturn(listResponse);

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(Collections.singletonList(enhetsId),
            PERSNR);

        ArgumentCaptor<ListCertificatesForCareType> argument = ArgumentCaptor.forClass(ListCertificatesForCareType.class);

        verify(listCertificatesForCareResponder).listCertificatesForCare(eq(LOGICAL_ADDRESS), argument.capture());

        assertEquals(2, intygItemListResponse.getLeft().size());

        ListIntygEntry meta = intygItemListResponse.getLeft().getFirst();

        assertEquals("1", meta.getIntygId());
        assertEquals("fk7263", meta.getIntygType());
        assertEquals(CertificateState.SENT.name(), meta.getStatus());
        assertEquals(PERSNR, meta.getPatientId());
        assertEquals(1, argument.getValue().getEnhetsId().size());
        assertNotNull(argument.getValue().getEnhetsId().getFirst().getRoot());
        assertEquals(enhetsId, argument.getValue().getEnhetsId().getFirst().getExtension());
        assertNotNull(argument.getValue().getPersonId().getRoot());
        assertEquals("191212121212", argument.getValue().getPersonId().getExtension());
    }

    @Test
    public void testListIntygFromIT() {
        final String enhetsId = "enhet-1";

        // setup intygstjansten WS mock to return intyg information
        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class)))
            .thenReturn(listResponse);

        final var intygItemListResponse = intygService.listIntygFromIT(Collections.singletonList(enhetsId), PERSNR);

        ArgumentCaptor<ListCertificatesForCareType> argument = ArgumentCaptor.forClass(ListCertificatesForCareType.class);

        verify(listCertificatesForCareResponder).listCertificatesForCare(eq(LOGICAL_ADDRESS), argument.capture());

        assertEquals(2, intygItemListResponse.size());

        ListIntygEntry meta = intygItemListResponse.getFirst();

        assertEquals("1", meta.getIntygId());
        assertEquals("fk7263", meta.getIntygType());
        assertEquals(CertificateState.SENT.name(), meta.getStatus());
        assertEquals(PERSNR, meta.getPatientId());
        assertEquals(1, argument.getValue().getEnhetsId().size());
        assertNotNull(argument.getValue().getEnhetsId().getFirst().getRoot());
        assertEquals(enhetsId, argument.getValue().getEnhetsId().getFirst().getExtension());
        assertNotNull(argument.getValue().getPersonId().getRoot());
        assertEquals("191212121212", argument.getValue().getPersonId().getExtension());
    }

    @Test
    public void testListIntygTakesStatusFromWebcertWhenNecessary() throws IOException {
        final String enhetsId = "enhet-1";

        listResponse.getIntygsLista().getIntyg().forEach(it -> {
            it.getStatus().clear();

            switch (it.getIntygsId().getExtension()) {
                case "1":
                    se.riv.clinicalprocess.healthcond.certificate.types.v3.Statuskod kod = new se.riv.clinicalprocess.healthcond.certificate.types.v3.Statuskod();
                    kod.setCode("RECEIV");
                    kod.setCodeSystem("9871cd17-8755-4ed9-b894-ff3729e775a4");
                    kod.setDisplayName("RECEIVED");

                    IntygsStatus status1 = new IntygsStatus();
                    status1.setStatus(kod);
                    it.getStatus().add(status1);
                    break;
                case "2":
                    se.riv.clinicalprocess.healthcond.certificate.types.v3.Statuskod kod2 = new se.riv.clinicalprocess.healthcond.certificate.types.v3.Statuskod();
                    kod2.setCode("SENTTO");
                    kod2.setCodeSystem("9871cd17-8755-4ed9-b894-ff3729e775a4");
                    kod2.setDisplayName("SENT");

                    IntygsStatus status2 = new IntygsStatus();
                    status2.setStatus(kod2);
                    it.getStatus().add(status2);
                    break;
                default:
                    throw new IllegalStateException("Unknown status: " + it.getIntygsId().getExtension());
            }
        });

        // setup intygstjansten WS mock to return intyg information
        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class)))
            .thenReturn(listResponse);

        Utkast one = getDraft("1");
        one.setStatus(UtkastStatus.SIGNED);
        one.setSenastSparadAv(vardpersonReferens);
        one.setSenastSparadDatum(LocalDateTime.now());
        one.setSkickadTillMottagare("FK");
        one.setSkickadTillMottagareDatum(LocalDateTime.now().minusDays(1));

        Utkast two = getDraft("2");
        two.setSenastSparadAv(vardpersonReferens);
        two.setStatus(UtkastStatus.SIGNED);
        two.setSenastSparadDatum(LocalDateTime.now());
        two.setSkickadTillMottagare("FK");
        two.setSkickadTillMottagareDatum(LocalDateTime.now().minusDays(1));
        two.setAterkalladDatum(LocalDateTime.now());

        List<Utkast> drafts = Arrays.asList(one, two);

        when(utkastRepository.findDraftsByPatientAndEnhetAndStatus(any(), any(), any(), any()))
            .thenReturn(drafts);

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(Collections.singletonList(enhetsId),
            PERSNR);

        assertEquals("SENT", intygItemListResponse.getLeft().get(0).getStatus());
        assertEquals("CANCELLED", intygItemListResponse.getLeft().get(1).getStatus());
    }

    @Test
    public void testListIntygWithIntygstjanstUnavailable() throws IOException {

        // setup intygstjansten WS mock to throw WebServiceException
        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class)))
            .thenThrow(
                WebServiceException.class);
        when(utkastRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet())).thenReturn(
            buildDraftList(false, null, null));

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"),
            PERSNR);
        assertNotNull(intygItemListResponse);
        assertEquals(1, intygItemListResponse.getLeft().size());

        // Assert pdl log not performed, e.g. listing is not a PDL loggable op.
        verifyNoInteractions(logservice);
    }

    @Test
    public void testListIntygFiltersList() {
        // no intygstyper for user
        when(authoritiesHelper.getIntygstyperForPrivilege(any(WebCertUser.class), anyString())).thenReturn(new HashSet<>());
        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class)))
            .thenReturn(listResponse);

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"),
            PERSNR);

        assertTrue(intygItemListResponse.getLeft().isEmpty());
    }

    @Test
    public void testListIntygFiltersNoMatch() {
        Set<String> set = new HashSet<>();
        set.add("luse");

        when(authoritiesHelper.getIntygstyperForPrivilege(any(WebCertUser.class), anyString())).thenReturn(set);
        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class)))
            .thenReturn(listResponse);

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"),
            PERSNR);

        assertTrue(intygItemListResponse.getLeft().isEmpty());
    }

    @Test
    public void testListIntygFiltersMatch() {
        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class)))
            .thenReturn(listResponse);

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"),
            PERSNR);

        assertEquals(2, intygItemListResponse.getLeft().size());
    }

    @Test
    public void testListIntygFiltersSekretessmarkering() throws JAXBException, IOException {
        Set<String> set = new HashSet<>();
        set.add("fk7263");
        set.add("ts-bas");
        set.add("doi");

        ClassPathResource response = new ClassPathResource("IntygServiceTest/response-list-certificates-with-sekretess.xml");

        JAXBContext context = JAXBContext.newInstance(ListCertificatesForCareResponseType.class);
        ListCertificatesForCareResponseType listResponse2 = context.createUnmarshaller()
            .unmarshal(new StreamSource(response.getInputStream()), ListCertificatesForCareResponseType.class)
            .getValue();

        when(patientDetailsResolver.getSekretessStatus(any())).thenReturn(SekretessStatus.TRUE);

        when(authoritiesHelper.getIntygstyperForPrivilege(any(WebCertUser.class), anyString())).thenReturn(set);

        when(authoritiesHelper.getIntygstyperAllowedForSekretessmarkering()).thenReturn(Sets.newHashSet("fk7263"));

        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class)))
            .thenReturn(listResponse2);

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"),
            PERSNR);

        assertEquals(2, intygItemListResponse.getLeft().size());
    }

    @Test
    public void testFetchIntygDataWhenIntygstjanstIsUnavailable() throws Exception {
        final LocalDateTime timestamp = LocalDateTime.of(2010, 11, 12, 13, 14, 15);
        Utkast utkast = getIntyg(CERTIFICATE_ID, null, null);
        utkast.setIntygsTyp(CERTIFICATE_TYPE);
        utkast.setSkapad(timestamp);
        when(moduleFacade.getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0))
            .thenThrow(WebServiceException.class);
        when(utkastRepository.findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE)).thenReturn(utkast);
        IntygContentHolder intygContentHolder = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        assertEquals(1, Objects.requireNonNull(intygContentHolder.getStatuses()).size());
        assertNotNull(intygContentHolder.getUtlatande());
        assertEquals(timestamp, intygContentHolder.getCreated());

        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
        verify(moduleFacade, times(2)).getUtlatandeFromInternalModel(eq(CERTIFICATE_TYPE), anyString(), any());
        verify(utkastRepository).findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE);
        verify(utkastRepository).findById(CERTIFICATE_ID);
        verifyNoMoreInteractions(moduleFacade, logservice, utkastRepository);
    }

    @Test
    public void testFetchIntygDataHasSentStatusWhenIntygstjanstIsUnavailableAndDraftHadSentDate() throws Exception {
        when(moduleFacade.getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0))
            .thenThrow(WebServiceException.class);
        when(utkastRepository.findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE))
            .thenReturn(getIntyg(CERTIFICATE_ID, LocalDateTime.now(), null));
        IntygContentHolder intygContentHolder = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        assertEquals(2, Objects.requireNonNull(intygContentHolder.getStatuses()).size());
        assertEquals(CertificateState.SENT, intygContentHolder.getStatuses().getFirst().getType());
        assertNotNull(intygContentHolder.getUtlatande());

        // ensure that correct call is made to moduleFacade
        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
    }

    @Test
    public void testFetchIntygDataHasSentAndRevokedStatusesWhenIntygstjanstIsUnavailableAndDraftHadSentDateAndRevokedDate()
        throws Exception {
        when(moduleFacade.getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0))
            .thenThrow(WebServiceException.class);
        when(utkastRepository.findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE))
            .thenReturn(getIntyg(CERTIFICATE_ID, LocalDateTime.now(), LocalDateTime.now()));
        IntygContentHolder intygContentHolder = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        assertEquals(3, Objects.requireNonNull(intygContentHolder.getStatuses()).size());
        assertEquals(CertificateState.SENT, intygContentHolder.getStatuses().get(0).getType());
        assertEquals(CertificateState.CANCELLED, intygContentHolder.getStatuses().get(1).getType());
        assertEquals(CertificateState.RECEIVED, intygContentHolder.getStatuses().get(2).getType());
        assertNotNull(intygContentHolder.getUtlatande());

        // ensure that correct call is made to moduleFacade
        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
    }

    @Test(expected = WebCertServiceException.class)
    public void testFetchIntygDataFailsWhenIntygstjanstIsUnavailableAndUtkastIsNotFound() throws Exception {
        when(moduleFacade.getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0))
            .thenThrow(WebServiceException.class);
        when(utkastRepository.findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE)).thenReturn(null);
        try {
            intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
        } catch (Exception e) {
            // ensure that correct call is made to moduleFacade
            verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
            verify(utkastRepository).findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE);
            // Assert pdl log
            verifyNoInteractions(logservice);
            throw e;
        }
    }

    @Test
    public void testDraftAddedToListResponseIfUnique() throws Exception {
        when(utkastRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet()))
            .thenReturn(buildDraftList(true, null, null));
        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class)))
            .thenReturn(listResponse);

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"),
            PERSNR);

        assertEquals(3, intygItemListResponse.getLeft().size());
        verify(utkastRepository).findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet());
    }

    @Test
    public void testDraftNotAddedToListResponseIfNotUnique() throws Exception {
        when(utkastRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet()))
            .thenReturn(buildDraftList(false, null, null));

        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class)))
            .thenReturn(listResponse);

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"),
            PERSNR);
        assertEquals("Dr. Who", intygItemListResponse.getLeft().getFirst().getUpdatedSignedBy());
        assertEquals(2, intygItemListResponse.getLeft().size());
        verify(utkastRepository).findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet());
    }

    @Test
    public void testDraftAddedWithSkapadAvNameIfMatching() throws Exception {
        when(utkastRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet())).thenReturn(
            buildDraftList(true, vardpersonReferens, null));

        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class)))
            .thenReturn(listResponse);

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"),
            PERSNR);
        assertEquals(3, intygItemListResponse.getLeft().size());

        // Se till att posten vi lade till från "drafts" har fått namnet från Utkastet, inte signaturen där HsaId står.
        assertEquals(CREATED_BY_NAME, intygItemListResponse.getLeft().get(2).getUpdatedSignedBy());
        verify(utkastRepository).findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet());
    }

    @Test
    public void testDraftAddedWithSenastSparadAvNameIfMatching() throws Exception {
        vardpersonReferens.setNamn(SENAST_SPARAD_NAME);
        when(utkastRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet())).thenReturn(
            buildDraftList(true, null, vardpersonReferens));

        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class)))
            .thenReturn(
                listResponse);

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"),
            PERSNR);
        assertEquals(3, intygItemListResponse.getLeft().size());

        // Se till att posten vi lade till från "drafts" har fått namnet från Utkastet, inte signaturen där HsaId står.
        assertEquals(SENAST_SPARAD_NAME, intygItemListResponse.getLeft().get(2).getUpdatedSignedBy());
        verify(utkastRepository).findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet());
    }

    @Test
    public void testDraftAddedWithHsaIdIfNoneMatching() throws Exception {
        vardpersonReferens.setNamn(SENAST_SPARAD_NAME);
        when(utkastRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet())).thenReturn(
            buildDraftList(true, null, null));

        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class)))
            .thenReturn(
                listResponse);

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"),
            PERSNR);
        assertEquals(3, intygItemListResponse.getLeft().size());

        // Se till att posten vi lade till från "drafts" har fått namnet från Utkastet, inte signaturen där HsaId står.
        assertEquals(HSA_ID, intygItemListResponse.getLeft().get(2).getUpdatedSignedBy());
        verify(utkastRepository).findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet());
    }

    @Test
    public void testFetchUtkastAsPdfFromWebCert() throws IOException, IntygModuleFacadeException {
        setupUserAndVardgivare();
        when(utkastRepository.findById(CERTIFICATE_ID)).thenReturn(Optional.of(getDraft(CERTIFICATE_ID, UtkastStatus.DRAFT_INCOMPLETE)));
        when(moduleFacade.convertFromInternalToPdfDocument(anyString(), anyString(), anyList(), any(UtkastStatus.class), anyBoolean()))
            .thenReturn(buildPdfDocument());

        final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
        when(certificateAnalyticsMessageFactory.certificatePrinted(any(Utlatande.class))).thenReturn(analyticsMessage);

        IntygPdf intygPdf = intygService.fetchIntygAsPdf(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        assertNotNull(intygPdf);

        verify(utkastRepository, times(1)).findById(anyString());
        verify(logservice).logPrintIntygAsPDF(any(LogRequest.class));
        verifyNoMoreInteractions(logservice);
        verify(moduleFacade, times(0)).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
        verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);
    }

    @Test(expected = WebCertServiceException.class)
    public void testFetchRevokedIntygAsPdfFromIntygstjansten() throws IOException, IntygModuleFacadeException {
        // Return a signed utkast, to make it fetch intyg from IT
        when(utkastRepository.findById(CERTIFICATE_ID)).thenReturn(
            Optional.of(getIntyg(CERTIFICATE_ID, LocalDateTime.now(), LocalDateTime.now())));

        CertificateMetaData metaData = buildCertificateMetaData();

        Fk7263Utlatande utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);
        utlatande.setId(CERTIFICATE_ID);
        utlatande.setTyp(CERTIFICATE_TYPE);
        utlatande.getGrundData().getPatient().setPersonId(PERSNR);

        final Status status = new Status();
        status.setType(CertificateState.CANCELLED);
        status.setTimestamp(LocalDateTime.of(2016, 1, 1, 1, 1, 1, 1));
        metaData.setStatus(Lists.newArrayList(status));

        CertificateResponse certificateResponse = new CertificateResponse(json, utlatande, metaData, true);
        // Return a revoked intyg.
        when(moduleFacade.getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0)).thenReturn(certificateResponse);

        try {
            intygService.fetchIntygAsPdf(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        } catch (Exception e) {
            verify(utkastRepository, times(2)).findById(CERTIFICATE_ID);
            verifyNoInteractions(logservice);
            verify(moduleFacade, times(1)).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
            throw e;
        }
    }

    @Test
    public void testFetchLockedDraftAsPdfFromWebCert() throws IOException, IntygModuleFacadeException {
        setupUserAndVardgivare();
        when(utkastRepository.findById(CERTIFICATE_ID)).thenReturn(Optional.of(getDraft(CERTIFICATE_ID, UtkastStatus.DRAFT_LOCKED)));
        when(moduleFacade.convertFromInternalToPdfDocument(anyString(), anyString(), anyList(), any(UtkastStatus.class), anyBoolean()))
            .thenReturn(buildPdfDocument());

        final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
        when(certificateAnalyticsMessageFactory.certificatePrinted(any(Utlatande.class))).thenReturn(analyticsMessage);

        IntygPdf pdf = intygService.fetchIntygAsPdf(CERTIFICATE_ID, CERTIFICATE_TYPE, false);

        assertNotNull(pdf);
        verify(utkastRepository, times(1)).findById(CERTIFICATE_ID);
        verify(logservice).logPrintIntygAsPDF(any(LogRequest.class));
        verifyNoMoreInteractions(logservice);
        verify(moduleFacade, times(0)).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
        verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);
    }

    @Test
    public void testFetchIntygAsPdfFromIntygstjansten() throws IntygModuleFacadeException {
        setupUserAndVardgivare();
        when(utkastRepository.findById(CERTIFICATE_ID)).thenReturn(Optional.empty());
        when(moduleFacade.convertFromInternalToPdfDocument(anyString(), anyString(), anyList(), any(UtkastStatus.class), anyBoolean()))
            .thenReturn(buildPdfDocument());

        final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
        when(certificateAnalyticsMessageFactory.certificatePrinted(any(Utlatande.class))).thenReturn(analyticsMessage);

        IntygPdf intygPdf = intygService.fetchIntygAsPdf(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        assertNotNull(intygPdf);

        verify(logservice).logPrintIntygAsPDF(any(LogRequest.class));
        verify(utkastRepository, times(2)).findById(anyString());
        verify(getCertificateTypeInfoService, times(1)).getCertificateTypeInfo(anyString(), any(GetCertificateTypeInfoType.class));
        verify(moduleFacade, times(1)).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
        verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);
    }

    @Test(expected = WebCertServiceException.class)
    public void testFetchIntygAsPdfNoIntygFound() throws IntygModuleFacadeException {
        when(utkastRepository.findById(CERTIFICATE_ID)).thenReturn(Optional.empty());
        when(utkastRepository.findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE)).thenReturn(null);
        when(moduleFacade.getCertificate(anyString(), anyString(), anyString())).thenThrow(IntygModuleFacadeException.class);

        try {
            intygService.fetchIntygAsPdf(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        } catch (Exception e) {
            verify(moduleFacade, times(1)).getCertificate(anyString(), anyString(), anyString());
            verify(utkastRepository, times(2)).findById(CERTIFICATE_ID);
            verify(utkastRepository, times(1)).findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE);
            verifyNoInteractions(logservice);
            throw e;
        }
    }

    @Test
    public void testLoggingFetchIntygAsPdfWithDraft() throws IOException, IntygModuleFacadeException {
        final Utkast draft = getDraft(CERTIFICATE_ID);
        setupUserAndVardgivare();
        when(utkastRepository.findById(CERTIFICATE_ID)).thenReturn(Optional.of(draft));

        Fk7263Utlatande utlatande = objectMapper.readValue(draft.getModel(), Fk7263Utlatande.class);

        when(moduleFacade.getUtlatandeFromInternalModel(anyString(), anyString(), any())).thenReturn(utlatande);
        when(moduleFacade.convertFromInternalToPdfDocument(anyString(), anyString(), anyList(), any(UtkastStatus.class), anyBoolean()))
            .thenReturn(buildPdfDocument());

        IntygPdf intygPdf = intygService.fetchIntygAsPdf(CERTIFICATE_ID, CERTIFICATE_TYPE, false);

        assertNotNull(intygPdf);

        verify(utkastRepository).findById(anyString());
        verify(logservice).logPrintIntygAsDraft(any(LogRequest.class));
        verifyNoMoreInteractions(logservice);
        verify(moduleFacade, times(0)).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION_1_0);
    }

    @Test
    public void testLoggingFetchIntygAsPdfWithSJF() throws IOException, IntygModuleFacadeException {
        setupUserAndVardgivare();
        // Set up user
        IntegrationParameters parameters = new IntegrationParameters(null, null, null, null, null, null, null, null, null, true, false,
            false, false, null);
        when(webcertUser.getOrigin()).thenReturn(UserOriginType.DJUPINTEGRATION.name());
        when(webcertUser.getParameters()).thenReturn(parameters);

        final Utkast draft = getDraft(CERTIFICATE_ID);
        when(utkastRepository.findById(CERTIFICATE_ID)).thenReturn(Optional.of(draft));

        Fk7263Utlatande utlatande = objectMapper.readValue(draft.getModel(), Fk7263Utlatande.class);

        when(moduleFacade.getUtlatandeFromInternalModel(anyString(), anyString(), any())).thenReturn(utlatande);
        when(moduleFacade.convertFromInternalToPdfDocument(anyString(), anyString(), anyList(), any(UtkastStatus.class), anyBoolean()))
            .thenReturn(buildPdfDocument());
        intygService.fetchIntygAsPdf(CERTIFICATE_ID, CERTIFICATE_TYPE, false);

        // Verify that the isAuthorized check wasn't run (since SJF=true and DJUPINTEGRATION)
        verify(webCertUserService, times(0)).isAuthorizedForUnit(anyString(), anyString(), anyBoolean());
    }

    @Test
    public void testHandleSignedCompletion() throws Exception {
        final String intygId = "123";
        final String intygTyp = "intygTyp";
        final String intygTypVersion = "intygTypVersion";
        final String relationIntygId = "relationIntygId";
        final String recipient = new Fk7263EntryPoint().getDefaultRecipient();

        final Personnummer personnummer = PERSNR;

        Fk7263Utlatande utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);
        utlatande.setId(intygId);
        utlatande.setTyp(intygTyp);
        utlatande.getGrundData().getPatient().setPersonId(personnummer);

        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setIntygsTyp(intygTyp);
        utkast.setIntygTypeVersion(intygTypVersion);
        utkast.setPatientPersonnummer(personnummer);
        utkast.setRelationKod(RelationKod.KOMPLT);
        utkast.setRelationIntygsId(relationIntygId);
        utkast.setModel(json);

        when(utkastRepository.findById(intygId)).thenReturn(Optional.of(utkast));
        when(certificateRelationService.getNewestRelationOfType(intygId, RelationKod.ERSATT,
            Collections.singletonList(UtkastStatus.SIGNED)))
            .thenReturn(Optional.empty());
        when(moduleRegistry.getModuleEntryPoint(intygTyp)).thenReturn(new Fk7263EntryPoint());

        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setSignatur(
            new Signatur(LocalDateTime.of(2011, 11, 11, 11, 11, 11, 11), "Signe Signatur", intygId, "data", "hash", "signatur"));

        CertificateMetaData metaData = buildCertificateMetaData();

        final Status status = new Status();
        status.setType(CertificateState.RECEIVED);
        status.setTimestamp(LocalDateTime.of(2016, 1, 1, 1, 1, 1, 1));
        metaData.setStatus(Lists.newArrayList(status));

        intygService.handleAfterSigned(utkast);

        verify(certificateSenderService).sendCertificate(eq(intygId), any(), anyString(), eq(recipient), eq(true));
        verify(mockMonitoringService).logIntygSent(intygId, Fk7263EntryPoint.MODULE_ID, recipient);
        verify(logservice).logSendIntygToRecipient(any(LogRequest.class));
        verify(arendeService).closeCompletionsAsHandled(relationIntygId, intygTyp);
        verify(notificationService).sendNotificationForIntygSent(intygId);
        verify(certificateEventService, times(1))
            .createCertificateEvent(anyString(), anyString(), eq(EventCode.SKICKAT), anyString());

        ArgumentCaptor<Utkast> utkastCaptor = ArgumentCaptor.forClass(Utkast.class);
        verify(utkastRepository).save(utkastCaptor.capture());
        assertNotNull(utkastCaptor.getValue().getSkickadTillMottagareDatum());
        assertEquals(recipient, utkastCaptor.getValue().getSkickadTillMottagare());
    }

    @Test
    public void testHandleSignedWithSigneraSkickaDirekt() throws Exception {
        final String intygId = "123";
        final String intygTyp = "intygTyp";
        final String intygTypVersion = "intygTypVersion";
        final String relationIntygId = "relationIntygId";
        final String recipient = new Fk7263EntryPoint().getDefaultRecipient();

        final Personnummer personnummer = PERSNR;

        Fk7263Utlatande utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);
        utlatande.setId(intygId);
        utlatande.setTyp(intygTyp);
        utlatande.getGrundData().getPatient().setPersonId(personnummer);

        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setIntygsTyp(intygTyp);
        utkast.setIntygTypeVersion(intygTypVersion);
        utkast.setPatientPersonnummer(personnummer);
        utkast.setModel(json);
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setSignatur(
            new Signatur(LocalDateTime.of(2011, 11, 11, 11, 11, 11, 11), "Signe Signatur", intygId, "data", "hash", "signatur"));

        CertificateMetaData metaData = buildCertificateMetaData();

        final Status status = new Status();
        status.setType(CertificateState.RECEIVED);
        status.setTimestamp(LocalDateTime.of(2016, 1, 1, 1, 1, 1, 1));
        metaData.setStatus(Lists.newArrayList(status));

        when(utkastRepository.findById(intygId)).thenReturn(Optional.of(utkast));
        when(certificateRelationService.getNewestRelationOfType(intygId, RelationKod.ERSATT,
            Collections.singletonList(UtkastStatus.SIGNED)))
            .thenReturn(Optional.empty());
        when(moduleRegistry.getModuleEntryPoint(intygTyp)).thenReturn(new Fk7263EntryPoint());
        when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, intygTyp)).thenReturn(true);

        intygService.handleAfterSigned(utkast);

        verify(certificateSenderService).sendCertificate(eq(intygId), any(), anyString(), eq(recipient), eq(true));
        verify(mockMonitoringService).logIntygSent(intygId, Fk7263EntryPoint.MODULE_ID, recipient);
        verify(logservice).logSendIntygToRecipient(any(LogRequest.class));
        verify(arendeService, never()).closeCompletionsAsHandled(relationIntygId, intygTyp);
        verify(notificationService).sendNotificationForIntygSent(intygId);
        verify(certificateEventService)
            .createCertificateEvent(anyString(), anyString(), eq(EventCode.SKICKAT), anyString());
        ArgumentCaptor<Utkast> utkastCaptor = ArgumentCaptor.forClass(Utkast.class);
        verify(utkastRepository).save(utkastCaptor.capture());
        assertNotNull(utkastCaptor.getValue().getSkickadTillMottagareDatum());
        assertEquals(recipient, utkastCaptor.getValue().getSkickadTillMottagare());
    }

    @Test
    public void testGetIssuingVardenhetsIdForUtkast() {
        String issuingVardenhetHsaId = intygService.getIssuingVardenhetHsaId(CERTIFICATE_ID, CERTIFICATE_TYPE);
        assertEquals("VardenhetY", issuingVardenhetHsaId);
    }

    @Test
    public void testIsRevoked() {
        boolean revoked = intygService.isRevoked(CERTIFICATE_ID, CERTIFICATE_TYPE);
        assertFalse(revoked);
        verify(mockMonitoringService).logIntygRevokeStatusRead(CERTIFICATE_ID, CERTIFICATE_TYPE);
    }

    @Test
    public void shallReturnEmptyListIfNotificationsIsEmpty() {
        assertEquals(Collections.emptyList(), intygService.listCertificatesForCareWithQA(
            Collections.emptyList())
        );
    }

    @Test
    public void testListCertificatesForCareWithQAOk() throws Exception {
        final String intygType = "intygType";
        final String intygId = "intygId";

        final LocalDateTime localDateTime = LocalDateTime.of(2017, Month.JANUARY, 1, 1, 1);

        Handelse handelse = new Handelse();
        handelse.setTimestamp(localDateTime);
        handelse.setCode(HandelsekodEnum.SKAPAT);
        handelse.setIntygsId(intygId);

        Fk7263Utlatande utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);

        ArendeCount sent = new ArendeCount(1, 2, 3, 4);
        ArendeCount received = new ArendeCount(5, 6, 7, 8);

        when(moduleRegistry.listAllModules()).thenReturn(
            Collections.singletonList(new IntygModule(intygType, "", "", "", "", "", "", "", "", intygType)));
        when(utkastRepository.findAllById(any())).thenReturn(Collections.singletonList(getDraft(intygId)));
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(fragorOchSvarCreator.createArenden(eq(intygId), anyString())).thenReturn(Pair.of(sent, received));

        List<IntygWithNotificationsResponse> res = intygService.listCertificatesForCareWithQA(
            Collections.singletonList(handelse));

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(1, res.getFirst().getNotifications().size());
        assertEquals(HandelsekodEnum.SKAPAT, res.getFirst().getNotifications().getFirst().getCode());
        assertEquals(localDateTime, res.getFirst().getNotifications().getFirst().getTimestamp());
        assertEquals(1, res.getFirst().getSentQuestions().getTotalt());
        assertEquals(2, res.getFirst().getSentQuestions().getEjBesvarade());
        assertEquals(3, res.getFirst().getSentQuestions().getBesvarade());
        assertEquals(4, res.getFirst().getSentQuestions().getHanterade());
        assertEquals(5, res.getFirst().getReceivedQuestions().getTotalt());
        assertEquals(6, res.getFirst().getReceivedQuestions().getEjBesvarade());
        assertEquals(7, res.getFirst().getReceivedQuestions().getBesvarade());
        assertEquals(8, res.getFirst().getReceivedQuestions().getHanterade());
        assertEquals(REFERENCE, res.getFirst().getRef());
    }

    @Test
    public void testListCertificatesForCareWithQADeletedDraft() {
        final String intygType = "intygType";
        final String intygId = "intygId";

        final LocalDateTime localDateTime = LocalDateTime.of(2017, Month.JANUARY, 1, 1, 1);

        Handelse handelse = new Handelse();
        handelse.setTimestamp(localDateTime);
        handelse.setCode(HandelsekodEnum.SKAPAT);
        handelse.setIntygsId(intygId);

        when(moduleRegistry.listAllModules()).thenReturn(
            Collections.singletonList(new IntygModule(intygType, "", "", "", "", "", "", "", "", intygType)));
        when(utkastRepository.findAllById(any())).thenReturn(Collections.emptyList());

        List<IntygWithNotificationsResponse> res = intygService.listCertificatesForCareWithQA(
            Collections.singletonList(handelse));

        assertNotNull(res);
        assertEquals(0, res.size());
    }

    @Test
    public void testListCertificatesForCareWithQAOkWithTimestamp() throws Exception {
        final String intygType = "intygType";
        final String intygId = "intygId";

        final LocalDateTime localDateTime = LocalDateTime.of(2017, Month.JANUARY, 1, 1, 1);

        Handelse handelse = new Handelse();
        handelse.setTimestamp(localDateTime);
        handelse.setCode(HandelsekodEnum.SKAPAT);
        handelse.setIntygsId(intygId);

        Fk7263Utlatande utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);

        ArendeCount sent = new ArendeCount(1, 2, 3, 4);
        ArendeCount received = new ArendeCount(5, 6, 7, 8);

        final var draftList = new ArrayList<Utkast>();
        draftList.add(getDraft(intygId));

        when(moduleRegistry.listAllModules()).thenReturn(
            Collections.singletonList(new IntygModule(intygType, "", "", "", "", "", "", "", "", intygType)));
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(fragorOchSvarCreator.createArenden(eq(intygId), anyString())).thenReturn(Pair.of(sent, received));
        doReturn(draftList).when(utkastRepository).findAllById(any());

        List<IntygWithNotificationsResponse> res = intygService.listCertificatesForCareWithQA(Collections.singletonList(handelse));

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(1, res.getFirst().getNotifications().size());
        assertEquals(HandelsekodEnum.SKAPAT, res.getFirst().getNotifications().getFirst().getCode());
        assertEquals(localDateTime, res.getFirst().getNotifications().getFirst().getTimestamp());
        assertEquals(1, res.getFirst().getSentQuestions().getTotalt());
        assertEquals(2, res.getFirst().getSentQuestions().getEjBesvarade());
        assertEquals(3, res.getFirst().getSentQuestions().getBesvarade());
        assertEquals(4, res.getFirst().getSentQuestions().getHanterade());
        assertEquals(5, res.getFirst().getReceivedQuestions().getTotalt());
        assertEquals(6, res.getFirst().getReceivedQuestions().getEjBesvarade());
        assertEquals(7, res.getFirst().getReceivedQuestions().getBesvarade());
        assertEquals(8, res.getFirst().getReceivedQuestions().getHanterade());
        assertEquals(REFERENCE, res.getFirst().getRef());
    }

    @Test
    public void testListCertificatesForCareWithQAOkWithTimestampMissingDraft() throws Exception {
        final String intygType = "intygType";

        final LocalDateTime localDateTime = LocalDateTime.of(2017, Month.JANUARY, 1, 1, 1);

        Handelse handelse = new Handelse();
        handelse.setTimestamp(localDateTime);
        handelse.setCode(HandelsekodEnum.NYFRFV);
        handelse.setIntygsId(CERTIFICATE_ID);

        Fk7263Utlatande utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);

        ArendeCount sent = new ArendeCount(1, 2, 3, 4);
        ArendeCount received = new ArendeCount(5, 6, 7, 8);

        when(moduleRegistry.listAllModules()).thenReturn(
            Collections.singletonList(new IntygModule(intygType, "", "", "", "", "", "", "", "", intygType)));
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(fragorOchSvarCreator.createArenden(eq(CERTIFICATE_ID), anyString())).thenReturn(Pair.of(sent, received));
        doReturn(Collections.emptyList()).when(utkastRepository).findAllById(any());

        List<IntygWithNotificationsResponse> res = intygService.listCertificatesForCareWithQA(Collections.singletonList(handelse));

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(1, res.getFirst().getNotifications().size());
        assertEquals(HandelsekodEnum.NYFRFV, res.getFirst().getNotifications().getFirst().getCode());
        assertEquals(localDateTime, res.getFirst().getNotifications().getFirst().getTimestamp());
        assertEquals(1, res.getFirst().getSentQuestions().getTotalt());
        assertEquals(2, res.getFirst().getSentQuestions().getEjBesvarade());
        assertEquals(3, res.getFirst().getSentQuestions().getBesvarade());
        assertEquals(4, res.getFirst().getSentQuestions().getHanterade());
        assertEquals(5, res.getFirst().getReceivedQuestions().getTotalt());
        assertEquals(6, res.getFirst().getReceivedQuestions().getEjBesvarade());
        assertEquals(7, res.getFirst().getReceivedQuestions().getBesvarade());
        assertEquals(8, res.getFirst().getReceivedQuestions().getHanterade());
        assertEquals("", res.getFirst().getRef());
    }

    @Test
    public void testListCertificatesForCareWithQANoNotifications() {
        final String intygId = "intygId";

        when(notificationService.getNotifications(intygId)).thenReturn(Collections.emptyList());

        List<IntygWithNotificationsResponse> res = intygService.listCertificatesForCareWithQA(
            Collections.emptyList());

        assertNotNull(res);
        assertEquals(0, res.size());
    }

    @Test
    public void testListCertificatesForCareWithQAVardgivare() throws Exception {
        final String intygType = "intygType";
        final String intygId = "intygId";

        final LocalDateTime localDateTime = LocalDateTime.of(2017, Month.JANUARY, 1, 1, 1);

        Handelse handelse = new Handelse();
        handelse.setTimestamp(localDateTime);
        handelse.setCode(HandelsekodEnum.SKAPAT);
        handelse.setIntygsId(intygId);

        Fk7263Utlatande utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);

        ArendeCount sent = new ArendeCount(1, 2, 3, 4);
        ArendeCount received = new ArendeCount(5, 6, 7, 8);

        when(moduleRegistry.listAllModules()).thenReturn(
            Collections.singletonList(new IntygModule(intygType, "", "", "", "", "", "", "", "", intygType)));
        when(utkastRepository.findAllById(any())).thenReturn(Collections.singletonList(getDraft(intygId)));
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(fragorOchSvarCreator.createArenden(eq(intygId), anyString())).thenReturn(Pair.of(sent, received));

        List<IntygWithNotificationsResponse> res = intygService.listCertificatesForCareWithQA(
            Collections.singletonList(handelse));

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(1, res.getFirst().getNotifications().size());
        assertEquals(HandelsekodEnum.SKAPAT, res.getFirst().getNotifications().getFirst().getCode());
        assertEquals(localDateTime, res.getFirst().getNotifications().getFirst().getTimestamp());
        assertEquals(1, res.getFirst().getSentQuestions().getTotalt());
        assertEquals(2, res.getFirst().getSentQuestions().getEjBesvarade());
        assertEquals(3, res.getFirst().getSentQuestions().getBesvarade());
        assertEquals(4, res.getFirst().getSentQuestions().getHanterade());
        assertEquals(5, res.getFirst().getReceivedQuestions().getTotalt());
        assertEquals(6, res.getFirst().getReceivedQuestions().getEjBesvarade());
        assertEquals(7, res.getFirst().getReceivedQuestions().getBesvarade());
        assertEquals(8, res.getFirst().getReceivedQuestions().getHanterade());
    }

    @Test
    public void testListCertificatesForCareWithQANoNotificationsTrim() throws Exception {
        final String vardgivarId = "vardgivarId";
        final String intygType = "intygType";
        final String intygId = "intygId";

        when(moduleRegistry.listAllModules())
            .thenReturn(Collections.singletonList(new IntygModule(intygType, "", "", "", "", "", "", "", "", intygType)));
        when(utkastRepository.findDraftsByPatientAndVardgivareAndStatus(PERSON_ID, vardgivarId,
            Arrays.asList(UtkastStatus.values()), Collections.singleton(intygType)))
            .thenReturn(Collections.singletonList(getDraft(intygId)));
        when(notificationService.getNotifications(intygId)).thenReturn(Collections.emptyList());

        List<IntygWithNotificationsResponse> res = intygService.listCertificatesForCareWithQA(
            Collections.emptyList());

        assertNotNull(res);
        assertEquals(0, res.size());
    }

    @Test
    public void testDeceasedIsSetForDeadPatientNormal() {
        when(patientDetailsResolver.isAvliden(any(Personnummer.class))).thenReturn(true);
        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        assertTrue(intygData.isDeceased());
    }

    @Test
    public void testDeceasedIsNotSetForAlivePatientDjupintegration() {
        when(webcertUser.getOrigin()).thenReturn(UserOriginType.DJUPINTEGRATION.name());
        when(webcertUser.getParameters())
            .thenReturn(new IntegrationParameters("", "", "", "", "", "", "", "", "", false, false, false, true, null));
        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        assertFalse(intygData.isDeceased());
    }

    @Test
    public void testDeceasedIsNotSetForDeadPatientDjupintegration() {
        when(webcertUser.getOrigin()).thenReturn(UserOriginType.DJUPINTEGRATION.name());
        when(webcertUser.getParameters())
            .thenReturn(new IntegrationParameters("", "", "", "", "", "", "", "", "", false, true, false, true, null));
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString()))
            .thenReturn(buildPatient(false, true));
        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        assertFalse(intygData.isDeceased());
    }

    @Test
    public void testThatCompletePatientAddressIsUsed() throws Exception {
        // Given
        String postadress = "ttipafpinu-postadress";
        String postort = "ttipafpinu-postort";
        String postnummer = "ttipafpinu-postnummer";
        Patient patientWithIncompleteAddress = buildPatient(false, false);
        patientWithIncompleteAddress.setPostadress(postadress);
        patientWithIncompleteAddress.setPostort(postort);
        patientWithIncompleteAddress.setPostnummer(postnummer);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString()))
            .thenReturn(patientWithIncompleteAddress);

        // When
        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);

        // Then
        ArgumentCaptor<Patient> argumentCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(moduleApi).updateBeforeViewing(anyString(), argumentCaptor.capture(), any());
        assertEquals(postadress, argumentCaptor.getValue().getPostadress());
        assertEquals(postort, argumentCaptor.getValue().getPostort());
        assertEquals(postnummer, argumentCaptor.getValue().getPostnummer());
    }

    @Test
    public void testThatIncompletePatientAddressIsNotUsed() throws Exception {
        // Given
        String postadress = "ttipafpinu-postadress";
        String postort = null;
        String postnummer = null;
        Patient patientWithIncompleteAddress = buildPatient(false, false);
        patientWithIncompleteAddress.setPostadress(postadress);
        patientWithIncompleteAddress.setPostort(postort);
        patientWithIncompleteAddress.setPostnummer(postnummer);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString()))
            .thenReturn(patientWithIncompleteAddress);

        // When
        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);

        // Then
        ArgumentCaptor<Patient> argumentCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(moduleApi).updateBeforeViewing(anyString(), argumentCaptor.capture(), any());
        assertNotEquals(postadress, argumentCaptor.getValue().getPostadress());
    }

    @Test
    public void testThatCompletePatientAddressIsUsedWhenIntygtjanstIsUnavailable() throws Exception {
        // Given
        when(moduleFacade.getCertificate(anyString(), anyString(), anyString())).thenThrow(new WebServiceException());
        when(utkastRepository.findByIntygsIdAndIntygsTyp(anyString(), anyString())).thenReturn(getIntyg(CERTIFICATE_ID,
            LocalDateTime.now(), null));

        String postadress = "ttipafpinuwiiu-postadress";
        String postort = "ttipafpinuwiiu-postort";
        String postnummer = "ttipafpinuwiiu-postnummer";
        Patient patientWithIncompleteAddress = buildPatient(false, false);
        patientWithIncompleteAddress.setPostadress(postadress);
        patientWithIncompleteAddress.setPostort(postort);
        patientWithIncompleteAddress.setPostnummer(postnummer);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString()))
            .thenReturn(patientWithIncompleteAddress);

        // When
        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);

        // Then
        ArgumentCaptor<Patient> argumentCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(moduleApi).updateBeforeViewing(anyString(), argumentCaptor.capture(), any());
        assertEquals(postadress, argumentCaptor.getValue().getPostadress());
        assertEquals(postort, argumentCaptor.getValue().getPostort());
        assertEquals(postnummer, argumentCaptor.getValue().getPostnummer());
    }

    @Test
    public void testThatIncompletePatientAddressIsNotUsedWhenIntygtjanstIsUnavailable() throws Exception {
        // Given
        when(moduleFacade.getCertificate(anyString(), anyString(), anyString())).thenThrow(new WebServiceException());
        when(utkastRepository.findByIntygsIdAndIntygsTyp(anyString(), anyString())).thenReturn(getIntyg(CERTIFICATE_ID,
            LocalDateTime.now(), null));

        String postadress = "ttipafpinuwiiu-postadress";
        String postort = "";
        String postnummer = "";
        Patient patientWithIncompleteAddress = buildPatient(false, false);
        patientWithIncompleteAddress.setPostadress(postadress);
        patientWithIncompleteAddress.setPostort(postort);
        patientWithIncompleteAddress.setPostnummer(postnummer);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString()))
            .thenReturn(patientWithIncompleteAddress);

        // When
        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);

        // Then
        ArgumentCaptor<Patient> argumentCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(moduleApi).updateBeforeViewing(anyString(), argumentCaptor.capture(), any());
        assertNotEquals(postadress, argumentCaptor.getValue().getPostadress());
    }

    @Test
    public void shouldSetIsLatestMajorTextVersionWhenCertificateFromIntygstjanst() {
        when(intygTextsService.isLatestMajorVersion(any(String.class), any(String.class))).thenReturn(false);

        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);

        assertFalse(intygData.isLatestMajorTextVersion());
    }

    @Test
    public void shouldSetIsLatestMajorTextVersionWhenCertificateFromWebcert() throws IntygModuleFacadeException, IOException {
        when(intygTextsService.isLatestMajorVersion(any(String.class), any(String.class))).thenReturn(false);
        when(moduleFacade.getCertificate(any(String.class), any(String.class), anyString())).thenThrow(new IntygModuleFacadeException(""));
        when(utkastRepository.findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE)).thenReturn(getIntyg(CERTIFICATE_ID,
            LocalDateTime.now(), null));

        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);

        assertFalse(intygData.isLatestMajorTextVersion());
    }

    private IntygPdf buildPdfDocument() {
        return new IntygPdf("fake".getBytes(), "fakepdf.pdf");
    }

    private List<Utkast> buildDraftList(boolean unique, VardpersonReferens skapadAv, VardpersonReferens senastSparadAv) throws IOException {
        List<Utkast> draftList = new ArrayList<>();
        Utkast draft = getIntyg(unique ? "LONG-UNIQUE-ID" : "1", LocalDateTime.now(), null);
        draft.setSkapadAv(skapadAv);
        draft.setSenastSparadAv(senastSparadAv);
        draftList.add(draft);
        return draftList;
    }

    private Utkast getIntyg(String intygsId, LocalDateTime sendDate, LocalDateTime revokeDate) throws IOException {
        String modelJson = IOUtils.toString(new ClassPathResource(
            "FragaSvarServiceImplTest/utlatande.json").getInputStream(), StandardCharsets.UTF_8);

        Utkast utkast = new Utkast();
        utkast.setModel(modelJson);
        utkast.setIntygsId(intygsId);
        utkast.setIntygsTyp(CERTIFICATE_TYPE);
        utkast.setIntygTypeVersion(CERTIFICATE_TYPE_VERSION_1_0);
        utkast.setSkickadTillMottagareDatum(sendDate);
        utkast.setAterkalladDatum(revokeDate);
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setPatientPersonnummer(PERSNR);

        Signatur signatur = new Signatur(LocalDateTime.now(), HSA_ID, CERTIFICATE_ID, "", "", "");
        utkast.setSignatur(signatur);

        return utkast;
    }

    private Utkast getDraft(String intygsId) throws IOException {
        return getDraft(intygsId, UtkastStatus.DRAFT_INCOMPLETE);
    }

    private Utkast getDraft(String intygsId, UtkastStatus utkastStatus) throws IOException {
        Utkast utkast = new Utkast();
        String modelJson = IOUtils.toString(new ClassPathResource(
            "IntygServiceTest/utkast-utlatande.json").getInputStream(), StandardCharsets.UTF_8);
        utkast.setModel(modelJson);
        utkast.setIntygsId(intygsId);
        utkast.setIntygsTyp(CERTIFICATE_TYPE);
        utkast.setPatientPersonnummer(PERSNR);
        utkast.setStatus(utkastStatus);

        return utkast;
    }

    private Patient buildPatient(boolean sekretessMarkering, boolean avliden) {
        Patient patient = new Patient();
        patient.setPersonId(PERSNR);
        patient.setFornamn("fornamn");
        patient.setMellannamn("mellannamn");
        patient.setEfternamn("efternamn");
        patient.setSekretessmarkering(sekretessMarkering);
        patient.setAvliden(avliden);

        return patient;

    }

    protected CertificateMetaData buildCertificateMetaData() {
        CertificateMetaData metaData = new CertificateMetaData();
        metaData.setStatus(new ArrayList<>());
        Status statusSigned = new Status(CertificateState.RECEIVED, "FKASSA", LocalDateTime.now());
        metaData.getStatus().add(statusSigned);
        metaData.setSignDate(LocalDateTime.now());
        return metaData;
    }

    private PersonSvar getPersonSvar(boolean deceased) {
        return PersonSvar.found(
            new Person(PERSNR, false, deceased, "fornamn", "mellannamn", "efternamn", "postadress",
                "postnummer", "postort"));
    }

}