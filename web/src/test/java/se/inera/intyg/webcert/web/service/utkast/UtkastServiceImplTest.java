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
package se.inera.intyg.webcert.web.service.utkast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.OptimisticLockException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.mapper.Mapper;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateDraftResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessage;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessageType;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserDetails;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.util.DefaultTypeAheadProvider;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.StatisticsGroupByUtil;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveDraftResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.UpdatePatientOnDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;
import se.inera.intyg.webcert.web.service.utkast.util.UtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;
import se.riv.clinicalprocess.healthcond.certificate.v33.Forifyllnad;

@RunWith(MockitoJUnitRunner.class)
public class UtkastServiceImplTest extends AuthoritiesConfigurationTestSetup {

    private static final long UTKAST_VERSION = 1;
    private static final long INTYG_VERSION = 2;

    private static final String INTYG_ID = "abc123";
    private static final String INTYG_ID_COPY = "def456";
    private static final String INTYG_TYPE = "fk7263";
    private static final String INTYG_TYPE2 = "lisjp";
    private static final String INTYG_TYPE_VERSION = "1.0";
    private static final String INTYG_JSON = "A bit of text representing json";

    private static final String UTKAST_ENHETS_ID = "hsa123";

    private static final String USER_REFERENCE = "some-ref";

    private static final String REFERENS = "referens";

    private static final String PERSON_ID = "19121212-1212";
    private static final Personnummer PERSONNUMMER = createPnr(PERSON_ID);

    @Mock
    private UtkastRepository utkastRepository;
    @Mock
    private IntygModuleRegistry moduleRegistry;
    @Mock
    private LogService logService;
    @Mock
    private LogRequestFactory logRequestFactory;
    @Mock
    private WebCertUserService userService;
    @Mock
    private IntygService intygService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CertificateEventService certificateEventService;
    @Mock
    private MonitoringLogService monitoringService;
    @Mock
    private AuthoritiesHelper authoritiesHelper;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private ReferensService referensService;
    @Mock
    private StatisticsGroupByUtil statisticsGroupByUtil;
    @Mock
    private UtkastServiceHelper utkastServiceHelper;
    @Mock
    private DraftAccessServiceHelper draftAccessServiceHelper;
    @Mock
    private ModuleApi moduleApi;
    @Spy
    private HashUtility hashUtility;
    @Mock
    private PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    @Mock
    private DefaultTypeAheadProvider defaultTypeAheadProvider;
    @Mock
    private CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @Spy
    private CreateIntygsIdStrategy mockIdStrategy = new CreateIntygsIdStrategy() {
        @Override
        public String createId() {
            return INTYG_ID_COPY;
        }
    };

    @InjectMocks
    private UtkastService utkastService = new UtkastServiceImpl();

    private Utkast utkast;
    private Utkast lockedUtkast;
    private Utkast revokedLockedUtkast;
    private Utkast signedUtkast;

    private HoSPersonal hoSPerson;
    private Patient defaultPatient;

    private static Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse personnummer: " + personId));
    }

    @Before
    public void setup() throws ModuleNotFoundException {
        hoSPerson = new HoSPersonal();
        hoSPerson.setPersonId("AAA");
        hoSPerson.setFullstandigtNamn("Dr Dengroth");
        hoSPerson.getBefattningar().add("Befattning");
        hoSPerson.getSpecialiteter().add("Ortoped");

        defaultPatient = new Patient();
        defaultPatient.setPersonId(PERSONNUMMER);
        defaultPatient.setFornamn("fornamn");
        defaultPatient.setMellannamn("mellannamn");
        defaultPatient.setPostadress("pa1");
        defaultPatient.setPostnummer("0000");
        defaultPatient.setPostort("ort");

        se.inera.intyg.common.support.model.common.internal.Vardgivare vardgivare = new se.inera.intyg.common.support.model.common.internal.Vardgivare();
        vardgivare.setVardgivarid("SE234234");
        vardgivare.setVardgivarnamn("Vårdgivaren");

        se.inera.intyg.common.support.model.common.internal.Vardenhet vardenhet = new se.inera.intyg.common.support.model.common.internal.Vardenhet();
        vardenhet.setArbetsplatsKod("00000");
        vardenhet.setEnhetsnamn("Vårdenheten");
        vardenhet.setEnhetsid("SE234897348");
        vardenhet.setPostadress("Sjukvägen 1");
        vardenhet.setPostnummer("12345");
        vardenhet.setPostort("Testberga");
        vardenhet.setTelefonnummer("0123-456789");
        vardenhet.setEpost("ingen@ingen.se");
        vardenhet.setVardgivare(vardgivare);

        VardpersonReferens vardperson = setupVardperson(hoSPerson);

        hoSPerson.setVardenhet(vardenhet);

        utkast = createUtkast(INTYG_ID, UTKAST_VERSION, INTYG_TYPE, UtkastStatus.DRAFT_INCOMPLETE, null, INTYG_JSON, vardperson,
            PERSONNUMMER);
        signedUtkast = createUtkast(INTYG_ID, INTYG_VERSION, INTYG_TYPE, UtkastStatus.SIGNED, LocalDateTime.parse("2018-04-23T00:00:00"),
            INTYG_JSON, vardperson, PERSONNUMMER);
        lockedUtkast = createUtkast(INTYG_ID, INTYG_VERSION, INTYG_TYPE, UtkastStatus.DRAFT_LOCKED, null,
            INTYG_JSON, vardperson, PERSONNUMMER);

        revokedLockedUtkast = createUtkast(INTYG_ID, INTYG_VERSION, INTYG_TYPE, UtkastStatus.DRAFT_LOCKED, null,
            INTYG_JSON, vardperson, PERSONNUMMER);
        revokedLockedUtkast.setAterkalladDatum(LocalDateTime.now());
        when(moduleRegistry.resolveVersionFromUtlatandeJson(anyString(), anyString())).thenReturn(INTYG_TYPE_VERSION);

        when(logRequestFactory.createLogRequestFromUtkast(any(Utkast.class))).thenReturn(LogRequest.builder().build());
        ReflectionTestUtils.setField(hashUtility, "salt", "salt");
    }

    private VardpersonReferens setupVardperson(HoSPersonal hoSPerson) {
        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hoSPerson.getPersonId());
        vardperson.setNamn(hoSPerson.getFullstandigtNamn());
        return vardperson;
    }

    @Test
    public void testDraftStatusIncomplete() throws IOException, ModuleNotFoundException, ModuleException {
        CreateNewDraftRequest request = buildCreateNewDraftRequest();
        request.setReferens(REFERENS);
        setupReferensMocks();
        Utkast res = utkastService.createNewDraft(request);
        assertFalse(request.getForifyllnad().isPresent());
        assertEquals(res.getStatus(), UtkastStatus.DRAFT_INCOMPLETE);
        verify(certificateEventService)
            .createCertificateEvent(anyString(), anyString(), eq(EventCode.SKAPAT));
    }

    @Test
    public void testDraftStatusPrefilledIncomplete() throws ModuleNotFoundException, IOException, ModuleException {
        CreateNewDraftRequest request = setupForifyllnadUtkast(ValidationStatus.INVALID);
        Utkast res = utkastService.createNewDraft(request);
        assertTrue(request.getForifyllnad().isPresent());
        assertEquals(res.getStatus(), UtkastStatus.DRAFT_INCOMPLETE);
        verify(certificateEventService)
            .createCertificateEvent(anyString(), anyString(), eq(EventCode.SKAPAT));
    }

    @Test
    public void testDraftStatusPrefilledComplete() throws ModuleNotFoundException, IOException, ModuleException {
        CreateNewDraftRequest request = setupForifyllnadUtkast(ValidationStatus.VALID);
        Utkast res = utkastService.createNewDraft(request);
        assertTrue(request.getForifyllnad().isPresent());
        assertEquals(res.getStatus(), UtkastStatus.DRAFT_COMPLETE);

        InOrder inOrder = inOrder(certificateEventService);
        inOrder.verify(certificateEventService)
            .createCertificateEvent(anyString(), anyString(), eq(EventCode.SKAPAT));
        inOrder.verify(certificateEventService)
            .createCertificateEvent(anyString(), anyString(), eq(EventCode.KFSIGN));
    }

    private CreateNewDraftRequest setupForifyllnadUtkast(ValidationStatus status)
        throws IOException, ModuleNotFoundException, ModuleException {
        CreateNewDraftRequest request = buildCreateNewDraftRequest();
        request.setReferens(REFERENS);

        Optional<Forifyllnad> optional = Optional.of(new Forifyllnad());
        request.setForifyllnad(optional);

        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(status, Collections.emptyList());
        when(moduleApi.validateDraft(anyString(), any())).thenReturn(validationResponse);
        when(utkastRepository.save(any(Utkast.class))).then(invocation -> invocation.getArguments()[0]);

        return request;
    }

    @Test
    public void testReferensGetsPersistedWhenSupplied() throws ModuleNotFoundException, IOException, ModuleException {
        CreateNewDraftRequest request = buildCreateNewDraftRequest();
        request.setReferens(REFERENS);

        setupReferensMocks();

        Utkast res = utkastService.createNewDraft(request);
        assertNotNull(res.getSkapad());
        verify(referensService).saveReferens(INTYG_ID, REFERENS);

    }

    @Test
    public void testEmptyReferensNotPersisted() throws ModuleNotFoundException, IOException, ModuleException {
        CreateNewDraftRequest request = buildCreateNewDraftRequest();
        request.setReferens("");

        setupReferensMocks();

        Utkast res = utkastService.createNewDraft(request);
        assertNotNull(res.getSkapad());
        verify(referensService, times(0)).saveReferens(INTYG_ID, REFERENS);
    }

    @Test
    public void testNullReferensNotPersisted() throws ModuleNotFoundException, IOException, ModuleException {
        CreateNewDraftRequest request = buildCreateNewDraftRequest();
        request.setReferens(null);

        setupReferensMocks();

        Utkast res = utkastService.createNewDraft(request);
        assertNotNull(res.getSkapad());
        verify(referensService, times(0)).saveReferens(INTYG_ID, REFERENS);
    }

    @Test
    public void testDeleteDraftThatIsUnsigned() {
        final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
        when(certificateAnalyticsMessageFactory.draftDeleted(any(Utkast.class))).thenReturn(analyticsMessage);
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));

        utkastService.deleteUnsignedDraft(INTYG_ID, utkast.getVersion());

        verify(utkastRepository).findById(INTYG_ID);
        verify(utkastRepository).delete(utkast);

        // Assert notification message
        verify(notificationService).sendNotificationForDraftDeleted(any(Utkast.class));

        // Assert pdl log
        verify(logService).logDeleteIntyg(any(LogRequest.class));

        verify(monitoringService).logUtkastDeleted(INTYG_ID, INTYG_TYPE);
        verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);
    }

    @Test
    public void testDeleteDraftWrongVersion() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));
        final var version = utkast.getVersion() - 1;

        try {
            utkastService.deleteUnsignedDraft(INTYG_ID, version);
            Assert.fail("OptimisticLockException expected");
        } catch (OptimisticLockException e) {
            // Expected
        }

        verify(utkastRepository).findById(INTYG_ID);
        verifyNoMoreInteractions(utkastRepository);

        // Assert notification message
        verifyNoInteractions(notificationService);

        // Assert pdl log
        verifyNoInteractions(logService);

        verifyNoInteractions(monitoringService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testDeleteDraftThatIsSigned() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(signedUtkast));
        utkastService.deleteUnsignedDraft(INTYG_ID, signedUtkast.getVersion());
    }

    @Test(expected = WebCertServiceException.class)
    public void testDeleteDraftThatDoesNotExist() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.empty());
        utkastService.deleteUnsignedDraft(INTYG_ID, 0);
    }

    @Test(expected = OptimisticLockException.class)
    public void testDeleteDraftThatIsSignedWrongVersion() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(signedUtkast));
        utkastService.deleteUnsignedDraft(INTYG_ID, signedUtkast.getVersion() - 1);
    }

    @Test(expected = WebCertServiceException.class)
    public void testDeleteDraftThatIsLocked() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(lockedUtkast));
        utkastService.deleteUnsignedDraft(INTYG_ID, lockedUtkast.getVersion());

        // Assert notification message
        verifyNoInteractions(notificationService);

        // Assert pdl log
        verifyNoInteractions(logService);

        verifyNoInteractions(monitoringService);
    }

    @Test
    public void testSaveDraftDraftFirstSave() throws Exception {
        ValidationMessage valMsg = new ValidationMessage("a.category", "a.field.somewhere", ValidationMessageType.OTHER,
            "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Collections.singletonList(valMsg));
        WebCertUser user = createUser();
        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();
        grunddata.setSkapadAv(new HoSPersonal());
        grunddata.setPatient(defaultPatient);
        when(utlatande.getGrundData()).thenReturn(grunddata);

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(moduleApi);
        when(moduleApi.validateDraft(anyString(), any())).thenReturn(validationResponse);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(utkastRepository.save(utkast)).thenReturn(utkast);
        when(moduleApi.shouldNotify(any(String.class), any(String.class))).thenReturn(true);
        when(userService.getUser()).thenReturn(user);
        when(moduleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
        when(certificateAnalyticsMessageFactory.draftUpdated(utkast)).thenReturn(analyticsMessage);

        SaveDraftResponse res = utkastService.saveDraft(INTYG_ID, UTKAST_VERSION, INTYG_JSON, true);

        verify(utkastRepository).save(any(Utkast.class));

        // Assert notification message
        verify(notificationService).sendNotificationForDraftChanged(any(Utkast.class));

        // Assert pdl log
        verify(logService).logUpdateIntyg(any(LogRequest.class));

        verify(monitoringService).logUtkastEdited(INTYG_ID, INTYG_TYPE);

        verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);

        assertNotNull("An DraftValidation should be returned", res);
        assertEquals("Validation should fail", UtkastStatus.DRAFT_INCOMPLETE, res.getStatus());
    }

    @Test
    public void testSaveDraftSecondSave() throws Exception {
        ValidationMessage valMsg = new ValidationMessage("a.category", "a.field.somewhere", ValidationMessageType.OTHER,
            "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Collections.singletonList(valMsg));
        WebCertUser user = createUser();
        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();
        grunddata.setSkapadAv(new HoSPersonal());
        grunddata.setPatient(defaultPatient);
        when(utlatande.getGrundData()).thenReturn(grunddata);

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(moduleApi);
        when(moduleApi.validateDraft(anyString(), any())).thenReturn(validationResponse);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(utkastRepository.save(utkast)).thenReturn(utkast);
        when(moduleApi.shouldNotify(any(String.class), any(String.class))).thenReturn(true);
        when(userService.getUser()).thenReturn(user);
        when(moduleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        SaveDraftResponse res = utkastService.saveDraft(INTYG_ID, UTKAST_VERSION, INTYG_JSON, false);

        verify(utkastRepository).save(any(Utkast.class));

        // Assert notification message
        verify(notificationService).sendNotificationForDraftChanged(any(Utkast.class));

        // Assert that no logs are called
        verifyNoInteractions(logService);
        verifyNoInteractions(monitoringService);

        assertNotNull("An DraftValidation should be returned", res);
        assertEquals("Validation should fail", UtkastStatus.DRAFT_INCOMPLETE, res.getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveDraftThatIsSigned() {

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(signedUtkast));

        utkastService.saveDraft(INTYG_ID, INTYG_VERSION, INTYG_JSON, false);

        verify(utkastRepository).findById(INTYG_ID);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveDraftThatIsLocked() {

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(lockedUtkast));

        utkastService.saveDraft(INTYG_ID, INTYG_VERSION, INTYG_JSON, false);

        verify(utkastRepository).findById(INTYG_ID);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveDraftWithExceptionInModule() throws Exception {
        WebCertUser user = createUser();
        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();
        grunddata.setSkapadAv(new HoSPersonal());
        grunddata.setPatient(defaultPatient);
        when(utlatande.getGrundData()).thenReturn(grunddata);

        when(userService.getUser()).thenReturn(user);
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(moduleApi);
        when(moduleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(moduleApi.validateDraft(anyString(), any())).thenThrow(ModuleException.class);

        utkastService.saveDraft(INTYG_ID, UTKAST_VERSION, INTYG_JSON, false);
    }

    @Test
    public void testValidateDraft() throws Exception {
        ValidationMessage valMsg = new ValidationMessage("a", "field.somewhere", ValidationMessageType.OTHER, "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Collections.singletonList(valMsg));

        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(moduleApi);
        when(moduleApi.validateDraft(eq(INTYG_JSON), any())).thenReturn(validationResponse);

        DraftValidation res = utkastService.validateDraft(INTYG_ID, INTYG_TYPE, INTYG_JSON);

        assertNotNull(res);
        assertFalse(res.isDraftValid());
        assertEquals(1, res.getMessages().size());

        verify(moduleApi).validateDraft(INTYG_JSON, defaultTypeAheadProvider);
    }

    @Test
    public void testNotifyDraft() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));
        when(utkastRepository.save(utkast)).thenReturn(utkast);

        utkastService.setNotifiedOnDraft(INTYG_ID, utkast.getVersion(), true);

        assertTrue(utkast.getVidarebefordrad());
    }

    @Test(expected = WebCertServiceException.class)
    public void testNotifyDraftThatDoesNotExist() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.empty());
        utkastService.setNotifiedOnDraft(INTYG_ID, 0, true);
    }

    @Test(expected = OptimisticLockException.class)
    public void testNotifyDraftWrongVersion() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));
        utkastService.setNotifiedOnDraft(INTYG_ID, utkast.getVersion() - 1, true);
    }

    @Test(expected = WebCertServiceException.class)
    public void testNotifyDraftThatIsSigned() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(signedUtkast));
        utkastService.setNotifiedOnDraft(INTYG_ID, 0, true);
    }

    @Test(expected = WebCertServiceException.class)
    public void testNotifyDraftThatIsLocked() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(lockedUtkast));
        utkastService.setNotifiedOnDraft(INTYG_ID, 0, true);
    }

    @Test
    public void testSaveUpdatesChangedPatientName() throws Exception {
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.VALID, Collections.emptyList());

        WebCertUser user = createUser();
        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();
        grunddata.setSkapadAv(new HoSPersonal());
        grunddata.setPatient(buildPatient(PERSON_ID, "Tolvan", "Tolvansson"));
        when(utlatande.getGrundData()).thenReturn(grunddata);

        utkast.setPatientFornamn("Inte Tolvan");
        utkast.setPatientEfternamn("Inte Tolvansson");

        // Make a spy out of the utkast so we can verify invocations on the setters with proper names further down.
        utkast = spy(utkast);

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(moduleApi);
        when(moduleApi.validateDraft(anyString(), any())).thenReturn(validationResponse);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(utkastRepository.save(utkast)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);
        when(moduleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        utkastService.saveDraft(INTYG_ID, UTKAST_VERSION, INTYG_JSON, false);

        verify(utkastRepository).save(any(Utkast.class));
        verify(utkast).setPatientFornamn("Tolvan");
        verify(utkast).setPatientEfternamn("Tolvansson");
        verify(utkast).setPatientPersonnummer(any(Personnummer.class));
    }

    @Test
    public void testUpdatePatientOnDraft() throws Exception {
        utkast.setEnhetsId(UTKAST_ENHETS_ID);

        Patient newPatient = getUpdatedPatient();

        UpdatePatientOnDraftRequest request = new UpdatePatientOnDraftRequest(newPatient.getPersonId(), utkast.getIntygsId(),
            utkast.getVersion());

        WebCertUser user = createUser();
        Utlatande utlatande = mock(Utlatande.class);

        GrundData grunddata = new GrundData();
        grunddata.setPatient(defaultPatient);
        grunddata.setSkapadAv(new HoSPersonal());
        String expectedPatientId = defaultPatient.getPersonId().getPersonnummer();

        when(utlatande.getGrundData()).thenReturn(grunddata);

        // Make a spy out of the utkast so we can verify invocations on the setters with proper names further down.
        utkast = spy(utkast);

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(moduleApi);
        when(moduleApi.updateBeforeSave(anyString(), any(Patient.class))).thenReturn("{}");
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(utkastRepository.save(utkast)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);
        when(moduleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        utkastService.updatePatientOnDraft(request);

        verify(utkastRepository).save(any(Utkast.class));
        verify(notificationService).sendNotificationForDraftChanged(any(Utkast.class));
        verify(utkast).setPatientPersonnummer(any(Personnummer.class));
        assertEquals(expectedPatientId, user.getParameters().getBeforeAlternateSsn());
    }

    @Test
    public void testUpdatePatientOnDraftEmptyPatientId() throws Exception {
        utkast.setEnhetsId(UTKAST_ENHETS_ID);

        UpdatePatientOnDraftRequest request = new UpdatePatientOnDraftRequest(null, utkast.getIntygsId(), utkast.getVersion());

        WebCertUser user = createUser();
        Utlatande utlatande = mock(Utlatande.class);

        GrundData grunddata = new GrundData();
        grunddata.setPatient(defaultPatient);
        grunddata.setSkapadAv(new HoSPersonal());

        when(utlatande.getGrundData()).thenReturn(grunddata);

        // Make a spy out of the utkast so we can verify invocations on the setters with proper names further down.
        utkast = spy(utkast);

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(userService.getUser()).thenReturn(user);

        utkastService.updatePatientOnDraft(request);

        verify(utkastRepository, never()).save(any(Utkast.class));
        verify(notificationService, never()).sendNotificationForDraftChanged(any(Utkast.class));
        verify(utkast, never()).setPatientPersonnummer(any(Personnummer.class));
        assertEquals(defaultPatient.getPersonId().getPersonnummer(), user.getParameters().getBeforeAlternateSsn());
    }

    @Test
    public void testUpdatePatientOnDraftSamePatientId() throws Exception {
        utkast.setEnhetsId(UTKAST_ENHETS_ID);

        UpdatePatientOnDraftRequest request = new UpdatePatientOnDraftRequest(defaultPatient.getPersonId(), utkast.getIntygsId(),
            utkast.getVersion());

        WebCertUser user = createUser();
        Utlatande utlatande = mock(Utlatande.class);

        GrundData grunddata = new GrundData();
        grunddata.setPatient(defaultPatient);
        grunddata.setSkapadAv(new HoSPersonal());

        when(utlatande.getGrundData()).thenReturn(grunddata);

        // Make a spy out of the utkast so we can verify invocations on the setters with proper names further down.
        utkast = spy(utkast);

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(userService.getUser()).thenReturn(user);

        utkastService.updatePatientOnDraft(request);

        verify(utkastRepository, never()).save(any(Utkast.class));
        verify(notificationService, never()).sendNotificationForDraftChanged(any(Utkast.class));
        verify(utkast, never()).setPatientPersonnummer(any(Personnummer.class));
        assertEquals(defaultPatient.getPersonId().getPersonnummer(), user.getParameters().getBeforeAlternateSsn());
    }

    @Test(expected = WebCertServiceException.class)
    public void testUpdatePatientOnDraftNoMedarbetaruppdragThrowsException() {
        utkast.setEnhetsId("<unknownenhet>");
        Patient newPatient = getUpdatedPatient();

        UpdatePatientOnDraftRequest request = new UpdatePatientOnDraftRequest(newPatient.getPersonId(), utkast.getIntygsId(),
            utkast.getVersion());

        WebCertUser user = createUser();
        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();

        grunddata.setPatient(defaultPatient);
        grunddata.setSkapadAv(new HoSPersonal());

        // Make a spy out of the utkast so we can verify invocations on the setters with proper names further down.
        utkast = spy(utkast);

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));
        when(userService.getUser()).thenReturn(user);

        utkastService.updatePatientOnDraft(request);

        verifyNoMoreInteractions(utkastRepository, notificationService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testUpdatePatientOnDraftThatIsLocked() {
        UpdatePatientOnDraftRequest request = new UpdatePatientOnDraftRequest(defaultPatient.getPersonId(), lockedUtkast.getIntygsId(),
            lockedUtkast.getVersion());

        WebCertUser user = createUser();

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(lockedUtkast));
        when(userService.getUser()).thenReturn(user);

        utkastService.updatePatientOnDraft(request);

        verify(utkastRepository, never()).save(any(Utkast.class));
        verify(utkast, never()).setPatientPersonnummer(any(Personnummer.class));

        // Assert notification message
        verifyNoInteractions(notificationService);

        // Assert pdl log
        verifyNoInteractions(logService);

        verifyNoInteractions(monitoringService);
    }

    @Test
    public void testSaveDoesNotUpdateOnEmptyFornamn() throws Exception {
        final String utkastFornamn = "fornamn";
        final String utkastEfternamn = "efternamn";
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.VALID, Collections.emptyList());

        WebCertUser user = createUser();
        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();
        grunddata.setSkapadAv(new HoSPersonal());
        grunddata.setPatient(buildPatient(PERSON_ID, null, "Tolvansson"));
        when(utlatande.getGrundData()).thenReturn(grunddata);

        utkast.setPatientFornamn(utkastFornamn);
        utkast.setPatientEfternamn(utkastEfternamn);

        // Make a spy out of the utkast so we can verify invocations on the setters with proper names further down.
        utkast = spy(utkast);

        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(moduleApi);
        when(moduleApi.validateDraft(anyString(), any())).thenReturn(validationResponse);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(utkastRepository.save(utkast)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);
        when(moduleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        utkastService.saveDraft(INTYG_ID, UTKAST_VERSION, INTYG_JSON, false);

        verify(utkastRepository).save(any(Utkast.class));
        verify(utkast, times(0)).setPatientFornamn(null);
        verify(utkast, times(0)).setPatientEfternamn("Tolvansson");
        verify(utkast).setPatientPersonnummer(any(Personnummer.class));
    }

    @Test
    public void testValidateValidDraftWithWarningsIncludesWarningsInResponse() throws ModuleException, ModuleNotFoundException {
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(moduleApi.validateDraft(anyString(), any())).thenReturn(buildValidationResponse());
        DraftValidation validationResult = utkastService.validateDraft(INTYG_ID, INTYG_TYPE, utkast.getModel());
        assertEquals(1, validationResult.getWarnings().size());
        assertEquals(0, validationResult.getMessages().size());
    }

    @Test
    public void testSetKlarForSigneraStatusMessageSent() {
        WebCertUser user = createUser();
        when(userService.getUser()).thenReturn(user);
        when(utkastRepository.findByIntygsIdAndIntygsTyp(INTYG_ID, "luae_fs")).thenReturn(utkast);
        when(utkastRepository.save(utkast)).thenReturn(utkast);
        when(authoritiesHelper.getIntygstyperForPrivilege(any(UserDetails.class), anyString()))
            .thenReturn(new HashSet<>(Arrays.asList("lisjp", "luse", "luae_fs", "luae_na")));

        final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
        when(certificateAnalyticsMessageFactory.draftReadyForSign(any(Utkast.class))).thenReturn(analyticsMessage);

        utkastService.setKlarForSigneraAndSendStatusMessage(INTYG_ID, "luae_fs");

        verify(notificationService).sendNotificationForDraftReadyToSign(utkast);
        verify(monitoringService).logUtkastMarkedAsReadyToSignNotificationSent(INTYG_ID, "luae_fs");
        verify(utkastRepository).save(utkast);
        verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSetKlarForSigneraStatusMessageSentThrowsExceptionForLakare() {
        utkastService.setKlarForSigneraAndSendStatusMessage(INTYG_ID, INTYG_TYPE);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSetKlarForSigneraStatusMessageSentThrowsExceptionForInvalidIntygsTyp() {
        when(authoritiesHelper.getIntygstyperForPrivilege(any(), any()))
            .thenReturn(new HashSet<>(Arrays.asList("lisjp", "luse", "luae_fs", "luae_na")));
        utkastService.setKlarForSigneraAndSendStatusMessage(INTYG_ID, INTYG_TYPE);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSetKlarForSigneraStatusMessageSentThatIsSigned() {
        WebCertUser user = createUser();
        when(userService.getUser()).thenReturn(user);
        when(authoritiesHelper.getIntygstyperForPrivilege(any(UserDetails.class), anyString()))
            .thenReturn(new HashSet<>(Arrays.asList("lisjp", "luse", "luae_fs", "luae_na")));
        when(utkastRepository.findByIntygsIdAndIntygsTyp(INTYG_ID, "luae_fs")).thenReturn(signedUtkast);

        utkastService.setKlarForSigneraAndSendStatusMessage(INTYG_ID, "luae_fs");

        // Assert notification message
        verifyNoInteractions(notificationService);

        verifyNoInteractions(monitoringService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSetKlarForSigneraStatusMessageSentThatIsLocked() {
        WebCertUser user = createUser();
        when(userService.getUser()).thenReturn(user);
        when(authoritiesHelper.getIntygstyperForPrivilege(any(UserDetails.class), anyString()))
            .thenReturn(new HashSet<>(Arrays.asList("lisjp", "luse", "luae_fs", "luae_na")));
        when(utkastRepository.findByIntygsIdAndIntygsTyp(INTYG_ID, "luae_fs")).thenReturn(lockedUtkast);

        utkastService.setKlarForSigneraAndSendStatusMessage(INTYG_ID, "luae_fs");

        // Assert notification message
        verifyNoInteractions(notificationService);

        verifyNoInteractions(monitoringService);
    }

    @Test
    public void testCheckIfPersonHasExistingUtkast() {
        final String personId = PERSON_ID;
        final Set activeModules = new HashSet<>(Arrays.asList("db", "doi"));
        final String vardgivareId = "vardgivarid";

        Utkast db1 = createUtkast("db1", 1L, "db", UtkastStatus.DRAFT_INCOMPLETE, null, "", null, PERSONNUMMER);
        db1.setVardgivarId("other");
        Utkast db2 = createUtkast("db2", 1L, "db", UtkastStatus.DRAFT_INCOMPLETE, null, "", null, PERSONNUMMER);
        db2.setVardgivarId(vardgivareId);
        Utkast doi = createUtkast("doi1", 1L, "doi", UtkastStatus.DRAFT_INCOMPLETE, null, "", null, PERSONNUMMER);
        doi.setVardgivarId("other");

        when(authoritiesHelper.getIntygstyperForFeature(any(), any(), any())).thenReturn(activeModules);
        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(PERSONNUMMER.getPersonnummerWithDash(), activeModules))
            .thenReturn(Arrays.asList(db1, db2, doi));

        Map<String, Map<String, PreviousIntyg>> res = utkastService.checkIfPersonHasExistingIntyg(PERSONNUMMER, createUser(), null);

        assertNotNull(res.get("utkast"));
        assertTrue(res.get("utkast").get("db").isSameVardgivare());
        assertEquals("db2", res.get("utkast").get("db").getLatestIntygsId());
        assertFalse(res.get("utkast").get("doi").isSameVardgivare());

        verify(utkastRepository).findAllByPatientPersonnummerAndIntygsTypIn(eq(PERSONNUMMER.getPersonnummerWithDash()),
            eq(activeModules));
    }

    @Test
    public void testCheckIfPersonHasExistingUtkastLatest() {
        final String personId = PERSON_ID;
        final Set activeModules = new HashSet<>(Arrays.asList("db", "doi"));
        final String vardgivareId = "vardgivarid";

        Utkast db1 = createUtkast("db1", 1L, "db", UtkastStatus.DRAFT_INCOMPLETE, null, "", null, PERSONNUMMER);
        db1.setVardgivarId(vardgivareId);
        db1.setSkapad(LocalDateTime.parse("2018-04-24T00:00:00"));
        Utkast db2 = createUtkast("db2", 1L, "db", UtkastStatus.DRAFT_INCOMPLETE, null, "", null, PERSONNUMMER);
        db2.setVardgivarId(vardgivareId);
        db2.setSkapad(LocalDateTime.parse("2018-04-23T00:00:00"));

        when(authoritiesHelper.getIntygstyperForFeature(any(), any(), any())).thenReturn(activeModules);
        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(PERSONNUMMER.getPersonnummerWithDash(), activeModules))
            .thenReturn(Arrays.asList(db1, db2));

        Map<String, Map<String, PreviousIntyg>> res = utkastService.checkIfPersonHasExistingIntyg(PERSONNUMMER, createUser(), null);

        assertNotNull(res.get("utkast"));
        assertTrue(res.get("utkast").get("db").isSameVardgivare());
        assertEquals("db1", res.get("utkast").get("db").getLatestIntygsId());

        verify(utkastRepository).findAllByPatientPersonnummerAndIntygsTypIn(eq(PERSONNUMMER.getPersonnummerWithDash()),
            eq(activeModules));
    }

    @Test
    public void testCheckIfPersonHasExistingIntyg() {
        final String personId = PERSON_ID;
        final Set activeModules = new HashSet<>(Arrays.asList("db", "doi"));
        final String vardgivareId = "vardgivarid";

        Utkast db1 = createUtkast("db1", 1L, "db", UtkastStatus.SIGNED, LocalDateTime.parse("2018-04-23T00:00:00"), "", null, PERSONNUMMER);
        db1.setVardgivarId("other");
        Utkast db2 = createUtkast("db2", 1L, "db", UtkastStatus.SIGNED, LocalDateTime.parse("2018-04-24T00:00:00"), "", null, PERSONNUMMER);
        db2.setVardgivarId(vardgivareId);
        Utkast doi = createUtkast("doi1", 1L, "doi", UtkastStatus.SIGNED, LocalDateTime.parse("2018-04-25T00:00:00"), "", null,
            PERSONNUMMER);
        doi.setVardgivarId("other");

        when(authoritiesHelper.getIntygstyperForFeature(any(), any(), any())).thenReturn(activeModules);
        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(PERSONNUMMER.getPersonnummerWithDash(), activeModules))
            .thenReturn(Arrays.asList(db1, db2, doi));

        Map<String, Map<String, PreviousIntyg>> res = utkastService.checkIfPersonHasExistingIntyg(PERSONNUMMER, createUser(), null);

        assertNotNull(res.get("intyg"));
        assertTrue(res.get("intyg").get("db").isSameVardgivare());
        assertEquals("db2", res.get("intyg").get("db").getLatestIntygsId());
        assertFalse(res.get("intyg").get("doi").isSameVardgivare());

        verify(utkastRepository).findAllByPatientPersonnummerAndIntygsTypIn(eq(PERSONNUMMER.getPersonnummerWithDash()),
            eq(activeModules));
    }

    @Test
    public void testCheckIfPersonHasExistingIntygReverseOrder() {
        final String personId = PERSON_ID;
        final Set activeModules = new HashSet<>(Arrays.asList("db", "doi"));
        final String vardgivareId = "vardgivarid";

        Utkast db1 = createUtkast("db1", 1L, "db", UtkastStatus.SIGNED, LocalDateTime.parse("2018-04-24T00:00:00"), "", null, PERSONNUMMER);
        db1.setVardgivarId("other");
        Utkast db2 = createUtkast("db2", 1L, "db", UtkastStatus.SIGNED, LocalDateTime.parse("2018-04-23T00:00:00"), "", null, PERSONNUMMER);
        db2.setVardgivarId(vardgivareId);

        when(authoritiesHelper.getIntygstyperForFeature(any(), any(), any())).thenReturn(activeModules);
        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(PERSONNUMMER.getPersonnummerWithDash(), activeModules))
            .thenReturn(Arrays.asList(db1, db2));

        Map<String, Map<String, PreviousIntyg>> res = utkastService.checkIfPersonHasExistingIntyg(PERSONNUMMER, createUser(), null);

        assertNotNull(res.get("intyg"));
        assertTrue(res.get("intyg").get("db").isSameVardgivare());
        assertEquals("db2", res.get("intyg").get("db").getLatestIntygsId());

        verify(utkastRepository).findAllByPatientPersonnummerAndIntygsTypIn(eq(PERSONNUMMER.getPersonnummerWithDash()),
            eq(activeModules));
    }

    @Test
    public void testCheckIfPersonHasExistingIntygReverseOrderSamevardgivare() {
        final String personId = PERSON_ID;
        final Set activeModules = new HashSet<>(Arrays.asList("db", "doi"));
        final String vardgivareId = "vardgivarid";

        Utkast db1 = createUtkast("db1", 1L, "db", UtkastStatus.SIGNED, LocalDateTime.parse("2018-04-24T00:00:00"), "", null, PERSONNUMMER);
        db1.setVardgivarId(vardgivareId);
        Utkast db2 = createUtkast("db2", 1L, "db", UtkastStatus.SIGNED, LocalDateTime.parse("2018-04-23T00:00:00"), "", null, PERSONNUMMER);
        db2.setVardgivarId(vardgivareId);

        when(authoritiesHelper.getIntygstyperForFeature(any(), any(), any())).thenReturn(activeModules);
        when(utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(PERSONNUMMER.getPersonnummerWithDash(), activeModules))
            .thenReturn(Arrays.asList(db1, db2));

        Map<String, Map<String, PreviousIntyg>> res = utkastService.checkIfPersonHasExistingIntyg(PERSONNUMMER, createUser(), null);

        assertNotNull(res.get("intyg"));
        assertTrue(res.get("intyg").get("db").isSameVardgivare());
        assertEquals("db1", res.get("intyg").get("db").getLatestIntygsId());

        verify(utkastRepository).findAllByPatientPersonnummerAndIntygsTypIn(eq(PERSONNUMMER.getPersonnummerWithDash()),
            eq(activeModules));
    }

    @Test
    public void testNumberOfUnsignedDraftsByCareUnits() {
        List<GroupableItem> queryResult = new ArrayList<>();

        when(userService.getUser()).thenReturn(createUser());
        when(utkastRepository.getIntygWithStatusesByEnhetsId(anyList(), anySet(), anySet())).thenReturn(queryResult);

        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("HSA1", 2L);

        when(statisticsGroupByUtil.toSekretessFilteredMap(queryResult)).thenReturn(resultMap);

        Map<String, Long> result = utkastService.getNbrOfUnsignedDraftsByCareUnits(Arrays.asList("HSA1", "HSA2"));

        verify(utkastRepository, times(1)).getIntygWithStatusesByEnhetsId(anyList(), anySet(), anySet());
        verify(statisticsGroupByUtil, times(1)).toSekretessFilteredMap(queryResult);

        assertEquals(1, result.size());
        assertEquals(2L, result.get("HSA1").longValue());

    }

    @Test
    public void testLockOldDrafts() {
        int lockedAfterDay = 14;

        Utkast utkast1 = new Utkast();
        utkast1.setIntygsId("id1");
        utkast1.setRelationIntygsId("id2");
        utkast1.setRelationKod(RelationKod.ERSATT);

        Utkast utkast2 = new Utkast();
        utkast2.setIntygsId("id2");

        LocalDate today = LocalDate.now();

        List<Utkast> utkastList = Arrays.asList(utkast1, utkast2);

        when(utkastRepository.findDraftsByNotLockedOrSignedAndSkapadBefore(any())).thenReturn(utkastList);

        int changed = utkastService.lockOldDrafts(lockedAfterDay, today);

        assertEquals(2, changed);
        verify(utkastRepository, times(2)).save(any(Utkast.class));
        verify(utkastRepository, times(2)).removeRelationsToDraft(anyString());

        assertNull(utkast1.getRelationIntygsId());
        assertNull(utkast1.getRelationKod());
        assertEquals(UtkastStatus.DRAFT_LOCKED, utkast1.getStatus());

        assertEquals(UtkastStatus.DRAFT_LOCKED, utkast2.getStatus());
    }

    @Test
    public void testRevokeLockedDraft() {
        WebCertUser user = createUser();
        when(userService.getUser()).thenReturn(user);
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(lockedUtkast));

        final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
        when(certificateAnalyticsMessageFactory.lockedDraftRevoked(any(Utkast.class))).thenReturn(analyticsMessage);

        String reason = "reason";
        String revokeMessage = "revokeMessage";

        utkastService.revokeLockedDraft(INTYG_ID, INTYG_TYPE, revokeMessage, reason);

        // Assert notification message
        verify(notificationService).sendNotificationForDraftRevoked(any(Utkast.class));
        verify(utkastRepository, times(1)).save(lockedUtkast);
        verify(monitoringService).logUtkastRevoked(INTYG_ID, user.getHsaId(), reason);
        verify(logService).logRevokeIntyg(any());
        verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeLockedDraftNull() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.empty());

        utkastService.revokeLockedDraft(INTYG_ID, INTYG_TYPE, "", "");
        verifyNoInteractions(monitoringService);
        verifyNoInteractions(logService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeLockedDraftNotLocked() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(utkast));

        utkastService.revokeLockedDraft(INTYG_ID, INTYG_TYPE, "", "");
        verifyNoInteractions(monitoringService);
        verifyNoInteractions(logService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeLockedDraftSigned() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(signedUtkast));

        utkastService.revokeLockedDraft(INTYG_ID, INTYG_TYPE, "", "");
        verifyNoInteractions(monitoringService);
        verifyNoInteractions(logService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeLockedDraftTypeMissMatch() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(lockedUtkast));

        utkastService.revokeLockedDraft(INTYG_ID, INTYG_TYPE2, "", "");
        verifyNoInteractions(monitoringService);
        verifyNoInteractions(logService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeLockedDraftAlreadyRevoked() {
        when(utkastRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(lockedUtkast));

        utkastService.revokeLockedDraft(INTYG_ID, INTYG_TYPE2, "", "");
        verifyNoInteractions(monitoringService);
        verifyNoInteractions(logService);
    }

    @Test
    public void testUpdateDraftFromCandidate() throws Exception {
        String fromIntygId = INTYG_ID;
        String fromIntygType = INTYG_TYPE2;
        String toIntygId = "ghi789";
        String toIntygType = "ag7804";

        VardpersonReferens vardperson = setupVardperson(hoSPerson);

        signedUtkast = createUtkast(fromIntygId, INTYG_VERSION, fromIntygType, UtkastStatus.SIGNED,
            LocalDateTime.parse("2018-04-23T00:00:00"), INTYG_JSON, vardperson, PERSONNUMMER);
        utkast = createUtkast(toIntygId, 0, toIntygType, UtkastStatus.DRAFT_INCOMPLETE,
            null, INTYG_JSON, vardperson, PERSONNUMMER);

        Utkast savedUtkast = utkast;
        savedUtkast.setVersion(utkast.getVersion() + 1);

        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();
        grunddata.setSkapadAv(new HoSPersonal());
        grunddata.setPatient(defaultPatient);

        Mapper mapper = mock(Mapper.class);

        when(utlatande.getGrundData()).thenReturn(grunddata);
        when(mapper.map(any(), any())).thenReturn(mapper);
        when(mapper.json()).thenReturn(INTYG_JSON);

        when(utkastRepository.findByIntygsIdAndIntygsTyp(toIntygId, toIntygType)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(toIntygType, INTYG_TYPE_VERSION)).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(utkastRepository.save(utkast)).thenReturn(savedUtkast);
        when(moduleApi.shouldNotify(any(String.class), any(String.class))).thenReturn(true);
        when(userService.getUser()).thenReturn(createUser());
        when(utkastServiceHelper.getUtlatandeForCandidateFromIT(fromIntygId, fromIntygType, true)).thenReturn(utlatande);
        when(moduleApi.getMapper()).thenReturn(Optional.of(mapper));
        when(moduleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        SaveDraftResponse res = utkastService.updateDraftFromCandidate(fromIntygId, fromIntygType, toIntygId, toIntygType);

        verify(utkastRepository).save(any(Utkast.class));

        // Assert notification message
        verify(notificationService).sendNotificationForDraftChanged(any(Utkast.class));

        // Assert pdl log
        verify(logService).logUpdateIntyg(any(LogRequest.class));

        verify(monitoringService).logUtkastEdited(toIntygId, toIntygType);

        assertNotNull("An DraftValidation should be returned", res);
        assertEquals("The status should still be incomplete", UtkastStatus.DRAFT_INCOMPLETE, res.getStatus());
        assertTrue("The saved draft version should be greater than zero", res.getVersion() > 0);
    }

    @Test
    public void shallReturnDraftIfItExists() {
        when(utkastRepository.findByIntygsIdAndIntygsTyp(INTYG_ID, INTYG_TYPE)).thenReturn(utkast);
        final var actualDraft = utkastService.getDraft(INTYG_ID, INTYG_TYPE, false);
        assertNotNull("Expected a draft but instead it is null", actualDraft);
    }

    @Test
    public void shallThrowDataNotFoundIfItDoesntExists() {
        when(utkastRepository.findByIntygsIdAndIntygsTyp(INTYG_ID, INTYG_TYPE)).thenReturn(null);

        try {
            utkastService.getDraft(INTYG_ID, INTYG_TYPE, false);
            fail("Should have thrown an exception");
        } catch (WebCertServiceException ex) {
            assertEquals(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Test
    public void shallPdlLogReadActivityByDefault() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var user = createUser();
        when(utkastRepository.findByIntygsIdAndIntygsTyp(INTYG_ID, INTYG_TYPE)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);
        when(logRequestFactory.createLogRequestFromUtkast(any(Utkast.class), eq(false))).thenReturn(expectedLogRequest);
        utkastService.getDraft(INTYG_ID, INTYG_TYPE);
        verify(logService).logReadIntyg(eq(expectedLogRequest));
    }

    @Test
    public void shallPdlLogReadActivityWithSjfWhenDifferentCareProvider() {
        final var expectedLogRequest = LogRequest.builder().build();
        final var user = createUser(true);
        utkast.setVardgivarId("Vardgivare2");
        when(utkastRepository.findByIntygsIdAndIntygsTyp(INTYG_ID, INTYG_TYPE)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);
        when(logRequestFactory.createLogRequestFromUtkast(any(Utkast.class), eq(true))).thenReturn(expectedLogRequest);
        utkastService.getDraft(INTYG_ID, INTYG_TYPE);
        verify(logService).logReadIntyg(eq(expectedLogRequest));
    }

    @Test
    public void shallMonitorLogWhenPdlLogging() {
        when(utkastRepository.getIntygsTyp(INTYG_ID)).thenReturn(INTYG_TYPE);
        when(utkastRepository.findByIntygsIdAndIntygsTyp(INTYG_ID, INTYG_TYPE)).thenReturn(utkast);
        utkastService.getDraft(INTYG_ID, true);
        verify(monitoringService).logUtkastRead(anyString(), anyString());
    }

    @Test
    public void shouldReturnNumberOfDeletedDrafts() {
        final var expectedDeletedDrafts = 2;

        final var utkast1 = createUtkast(INTYG_ID, UTKAST_VERSION, INTYG_TYPE, UtkastStatus.DRAFT_INCOMPLETE, null, INTYG_JSON,
            setupVardperson(hoSPerson),
            PERSONNUMMER);
        final var utkast2 = createUtkast(INTYG_ID, UTKAST_VERSION, INTYG_TYPE, UtkastStatus.DRAFT_INCOMPLETE, null, INTYG_JSON,
            setupVardperson(hoSPerson),
            PERSONNUMMER);

        when(utkastRepository.findStaleAndLockedDrafts(any(LocalDateTime.class),
            eq(List.of(UtkastStatus.DRAFT_LOCKED, UtkastStatus.DRAFT_INCOMPLETE, UtkastStatus.DRAFT_COMPLETE))))
            .thenReturn(List.of(utkast1, utkast2));

        final var resultDeletedDrafts = utkastService.deleteStaleAndLockedDrafts();

        assertEquals(expectedDeletedDrafts, resultDeletedDrafts);
    }


    private CreateNewDraftRequest buildCreateNewDraftRequest() {
        CreateNewDraftRequest request = new CreateNewDraftRequest();
        request.setHosPerson(hoSPerson);
        request.setIntygId(INTYG_ID);
        request.setIntygType(INTYG_TYPE);
        request.setIntygTypeVersion(INTYG_TYPE_VERSION);
        request.setPatient(defaultPatient);
        request.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        return request;
    }

    private ValidateDraftResponse buildValidationResponse() {
        return new ValidateDraftResponse(ValidationStatus.VALID, Collections.emptyList(),
            Collections.singletonList(new ValidationMessage("testcategory", "testfield", ValidationMessageType.WARN)));
    }

    private Patient buildPatient(String pnr, String fornamn, String efternamn) {
        Patient p = new Patient();
        p.setPersonId(createPnr(pnr));
        p.setFornamn(fornamn);
        p.setEfternamn(efternamn);
        return p;
    }

    private Utkast createUtkast(String intygId, long version, String type, UtkastStatus status, LocalDateTime signeringsDatum,
        String model,
        VardpersonReferens vardperson, Personnummer personnummer) {

        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setVersion(version);
        utkast.setIntygsTyp(type);
        utkast.setIntygTypeVersion(INTYG_TYPE_VERSION);
        utkast.setStatus(status);
        utkast.setModel(model);
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);
        utkast.setPatientPersonnummer(personnummer);

        if (status == UtkastStatus.SIGNED) {
            utkast.setSignatur(new Signatur(signeringsDatum, "", "", "", "", ""));
        }

        return utkast;
    }

    private WebCertUser createUser() {
        return createUser(false);
    }

    private WebCertUser createUser(boolean sjf) {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));

        user.setHsaId("hsaId");
        user.setNamn("namn");
        user.setOrigin("NORMAL");
        List<String> tmp = new ArrayList<>();
        tmp.add("Ortoped");
        user.setSpecialiseringar(tmp);
        user.setTitel("Befattning");
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setId("vardgivarid");
        vardgivare.setNamn("vardgivarnamn");
        user.setValdVardgivare(vardgivare);
        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setId(UTKAST_ENHETS_ID);
        vardenhet.setNamn("enhetnamn");
        user.setValdVardenhet(vardenhet);

        vardgivare.setVardenheter(Collections.singletonList(vardenhet));
        user.setVardgivare(Collections.singletonList(vardgivare));

        user.setParameters(new IntegrationParameters(USER_REFERENCE, "", "", "", "", "", "", "", "", sjf, false, false, true, null));

        return user;
    }

    private Patient getUpdatedPatient() {
        Patient newPatient = new Patient();
        newPatient.setEfternamn("updated lastName");
        newPatient.setMellannamn("updated middle-name");
        newPatient.setFornamn("updated firstName");
        newPatient.setFullstandigtNamn("updated full name");
        newPatient.setPersonId(createPnr("19121272-1212"));
        newPatient.setPostadress("updated postal address");
        newPatient.setPostnummer("1111111");
        newPatient.setPostort("updated post city");
        return newPatient;
    }

    private void setupReferensMocks() throws ModuleNotFoundException {
        ValidationMessage valMsg = new ValidationMessage("a.category", "a.field.somewhere", ValidationMessageType.OTHER,
            "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Collections.singletonList(valMsg));
        Utlatande utlatande = mock(Utlatande.class);
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(utkastRepository.save(any(Utkast.class))).then(invocation -> invocation.getArguments()[0]);
    }

}