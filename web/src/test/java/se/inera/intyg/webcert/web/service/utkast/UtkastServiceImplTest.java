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
package se.inera.intyg.webcert.web.service.utkast;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateDraftResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessage;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessageType;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserDetails;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
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
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

import javax.persistence.OptimisticLockException;
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
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtkastServiceImplTest extends AuthoritiesConfigurationTestSetup {

    private static final long UTKAST_VERSION = 1;
    private static final long INTYG_VERSION = 2;

    private static final String INTYG_ID = "abc123";
    private static final String INTYG_COPY_ID = "def456";
    private static final String INTYG_JSON = "A bit of text representing json";
    private static final String INTYG_TYPE = "fk7263";
    private static final String INTYG_TYPE_VERSION = "1.0";

    private static final String INTYG_TYPE2 = "lisjp";
    private static final String UTKAST_ENHETS_ID = "hsa123";

    private static final String USER_REFERENCE = "some-ref";

    private static final String REFERENS = "referens";

    private static final String PERSON_ID = "19121212-1212";
    private static final Personnummer PERSONNUMMER = createPnr(PERSON_ID);

    @Mock
    private UtkastRepository mockUtkastRepository;
    @Mock
    private IntygModuleRegistry moduleRegistry;
    @Mock
    private LogService logService;
    @Mock
    private WebCertUserService userService;
    @Mock
    private IntygService intygService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private MonitoringLogService mockMonitoringService;
    @Mock
    private AuthoritiesHelper authoritiesHelper;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private ReferensService referensService;
    @Mock
    private StatisticsGroupByUtil statisticsGroupByUtil;

    @Spy
    private CreateIntygsIdStrategy mockIdStrategy = new CreateIntygsIdStrategy() {
        @Override
        public String createId() {
            return INTYG_COPY_ID;
        }
    };

    @InjectMocks
    private UtkastService draftService = new UtkastServiceImpl();

    @Mock
    private ModuleApi mockModuleApi;

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

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hoSPerson.getPersonId());
        vardperson.setNamn(hoSPerson.getFullstandigtNamn());

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
        when(moduleRegistry.resolveVersionFromUtlatandeJson(anyString())).thenReturn(INTYG_TYPE_VERSION);

    }

    @Test
    public void testReferensGetsPersistedWhenSupplied() throws ModuleNotFoundException, IOException, ModuleException {
        CreateNewDraftRequest request = buildCreateNewDraftRequest();
        request.setReferens(REFERENS);

        setupReferensMocks();

        Utkast res = draftService.createNewDraft(request);
        assertNotNull(res.getSkapad());
        verify(referensService).saveReferens(INTYG_ID, REFERENS);

    }

    @Test
    public void testEmptyReferensNotPersisted() throws ModuleNotFoundException, IOException, ModuleException {
        CreateNewDraftRequest request = buildCreateNewDraftRequest();
        request.setReferens("");

        setupReferensMocks();

        Utkast res = draftService.createNewDraft(request);
        assertNotNull(res.getSkapad());
        verify(referensService, times(0)).saveReferens(INTYG_ID, REFERENS);
    }

    @Test
    public void testNullReferensNotPersisted() throws ModuleNotFoundException, IOException, ModuleException {
        CreateNewDraftRequest request = buildCreateNewDraftRequest();
        request.setReferens(null);

        setupReferensMocks();

        Utkast res = draftService.createNewDraft(request);
        assertNotNull(res.getSkapad());
        verify(referensService, times(0)).saveReferens(INTYG_ID, REFERENS);
    }

    private void setupReferensMocks() throws ModuleNotFoundException, ModuleException, IOException {
        ValidationMessage valMsg = new ValidationMessage("a.category", "a.field.somewhere", ValidationMessageType.OTHER,
                "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Collections.singletonList(valMsg));
        Utlatande utlatande = mock(Utlatande.class);
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(mockModuleApi);
        when(mockUtkastRepository.save(any(Utkast.class))).then(invocation -> invocation.getArguments()[0]);
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

    @Test
    public void testDeleteDraftThatIsUnsigned() {
        WebCertUser user = createUser();

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);

        draftService.deleteUnsignedDraft(INTYG_ID, utkast.getVersion());

        verify(mockUtkastRepository).findOne(INTYG_ID);
        verify(mockUtkastRepository).delete(utkast);

        // Assert notification message
        verify(notificationService).sendNotificationForDraftDeleted(any(Utkast.class));

        // Assert pdl log
        verify(logService).logDeleteIntyg(any(LogRequest.class));

        verify(mockMonitoringService).logUtkastDeleted(INTYG_ID, INTYG_TYPE);
    }

    @Test
    public void testDeleteDraftWrongVersion() {
        WebCertUser user = createUser();

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);

        try {
            draftService.deleteUnsignedDraft(INTYG_ID, utkast.getVersion() - 1);
            Assert.fail("OptimisticLockException expected");
        } catch (OptimisticLockException e) {
            // Expected
        }

        verify(mockUtkastRepository).findOne(INTYG_ID);
        verifyNoMoreInteractions(mockUtkastRepository);

        // Assert notification message
        verifyZeroInteractions(notificationService);

        // Assert pdl log
        verifyZeroInteractions(logService);

        verifyZeroInteractions(mockMonitoringService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testDeleteDraftThatIsSigned() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);
        draftService.deleteUnsignedDraft(INTYG_ID, signedUtkast.getVersion());
    }

    @Test(expected = WebCertServiceException.class)
    public void testDeleteDraftThatDoesNotExist() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(null);
        draftService.deleteUnsignedDraft(INTYG_ID, 0);
    }

    @Test(expected = OptimisticLockException.class)
    public void testDeleteDraftThatIsSignedWrongVersion() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);
        draftService.deleteUnsignedDraft(INTYG_ID, signedUtkast.getVersion() - 1);
    }

    @Test(expected = WebCertServiceException.class)
    public void testDeleteDraftThatIsLocked() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(lockedUtkast);
        draftService.deleteUnsignedDraft(INTYG_ID, lockedUtkast.getVersion());

        // Assert notification message
        verifyZeroInteractions(notificationService);

        // Assert pdl log
        verifyZeroInteractions(logService);

        verifyZeroInteractions(mockMonitoringService);
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

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(anyString())).thenReturn(validationResponse);
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(mockModuleApi.shouldNotify(any(String.class), any(String.class))).thenReturn(true);
        when(userService.getUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        SaveDraftResponse res = draftService.saveDraft(INTYG_ID, UTKAST_VERSION, INTYG_JSON, true);

        verify(mockUtkastRepository).save(any(Utkast.class));

        // Assert notification message
        verify(notificationService).sendNotificationForDraftChanged(any(Utkast.class));

        // Assert pdl log
        verify(logService).logUpdateIntyg(any(LogRequest.class));

        verify(mockMonitoringService).logUtkastEdited(INTYG_ID, INTYG_TYPE);

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

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(anyString())).thenReturn(validationResponse);
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(mockModuleApi.shouldNotify(any(String.class), any(String.class))).thenReturn(true);
        when(userService.getUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        SaveDraftResponse res = draftService.saveDraft(INTYG_ID, UTKAST_VERSION, INTYG_JSON, false);

        verify(mockUtkastRepository).save(any(Utkast.class));

        // Assert notification message
        verify(notificationService).sendNotificationForDraftChanged(any(Utkast.class));

        // Assert that no logs are called
        verifyZeroInteractions(logService);
        verifyZeroInteractions(mockMonitoringService);

        assertNotNull("An DraftValidation should be returned", res);
        assertEquals("Validation should fail", UtkastStatus.DRAFT_INCOMPLETE, res.getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveDraftThatIsSigned() {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);

        draftService.saveDraft(INTYG_ID, INTYG_VERSION, INTYG_JSON, false);

        verify(mockUtkastRepository).findOne(INTYG_ID);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveDraftThatIsLocked() {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(lockedUtkast);

        draftService.saveDraft(INTYG_ID, INTYG_VERSION, INTYG_JSON, false);

        verify(mockUtkastRepository).findOne(INTYG_ID);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = WebCertServiceException.class)
    public void testSaveDraftWithExceptionInModule() throws Exception {
        WebCertUser user = createUser();
        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();
        grunddata.setSkapadAv(new HoSPersonal());
        grunddata.setPatient(defaultPatient);
        when(utlatande.getGrundData()).thenReturn(grunddata);

        when(userService.getUser()).thenReturn(user);
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(mockModuleApi);
        when(mockModuleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(mockModuleApi.validateDraft(anyString())).thenThrow(ModuleException.class);

        draftService.saveDraft(INTYG_ID, UTKAST_VERSION, INTYG_JSON, false);
    }

    @Test
    public void testValidateDraft() throws Exception {
        ValidationMessage valMsg = new ValidationMessage("a", "field.somewhere", ValidationMessageType.OTHER, "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Collections.singletonList(valMsg));

        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(INTYG_JSON)).thenReturn(validationResponse);

        DraftValidation res = draftService.validateDraft(INTYG_ID, INTYG_TYPE, INTYG_JSON);

        assertNotNull(res);
        assertFalse(res.isDraftValid());
        assertEquals(1, res.getMessages().size());

        verify(mockModuleApi).validateDraft(INTYG_JSON);
    }

    @Test
    public void testNotifyDraft() {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);

        draftService.setNotifiedOnDraft(INTYG_ID, utkast.getVersion(), true);

        assertTrue(utkast.getVidarebefordrad());
    }

    @Test(expected = WebCertServiceException.class)
    public void testNotifyDraftThatDoesNotExist() {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(null);

        draftService.setNotifiedOnDraft(INTYG_ID, 0, true);
    }

    @Test(expected = OptimisticLockException.class)
    public void testNotifyDraftWrongVersion() {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);

        draftService.setNotifiedOnDraft(INTYG_ID, utkast.getVersion() - 1, true);
    }

    @Test(expected = WebCertServiceException.class)
    public void testNotifyDraftThatIsSigned() {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);

        draftService.setNotifiedOnDraft(INTYG_ID, 0, true);
    }

    @Test(expected = WebCertServiceException.class)
    public void testNotifyDraftThatIsLocked() {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(lockedUtkast);

        draftService.setNotifiedOnDraft(INTYG_ID, 0, true);
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

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(anyString())).thenReturn(validationResponse);
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        draftService.saveDraft(INTYG_ID, UTKAST_VERSION, INTYG_JSON, false);

        verify(mockUtkastRepository).save(any(Utkast.class));
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

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(mockModuleApi);
        when(mockModuleApi.updateBeforeSave(anyString(), any(Patient.class))).thenReturn("{}");
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        draftService.updatePatientOnDraft(request);

        verify(mockUtkastRepository).save(any(Utkast.class));
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

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(mockModuleApi);
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(userService.getUser()).thenReturn(user);

        draftService.updatePatientOnDraft(request);

        verify(mockUtkastRepository, never()).save(any(Utkast.class));
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

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(mockModuleApi);
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(userService.getUser()).thenReturn(user);

        draftService.updatePatientOnDraft(request);

        verify(mockUtkastRepository, never()).save(any(Utkast.class));
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

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);

        draftService.updatePatientOnDraft(request);

        verifyNoMoreInteractions(mockUtkastRepository, notificationService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testUpdatePatientOnDraftThatIsLocked() {
        UpdatePatientOnDraftRequest request = new UpdatePatientOnDraftRequest(defaultPatient.getPersonId(), lockedUtkast.getIntygsId(),
                lockedUtkast.getVersion());

        WebCertUser user = createUser();

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(lockedUtkast);
        when(userService.getUser()).thenReturn(user);

        draftService.updatePatientOnDraft(request);

        verify(mockUtkastRepository, never()).save(any(Utkast.class));
        verify(utkast, never()).setPatientPersonnummer(any(Personnummer.class));

        // Assert notification message
        verifyZeroInteractions(notificationService);

        // Assert pdl log
        verifyZeroInteractions(logService);

        verifyZeroInteractions(mockMonitoringService);
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

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(anyString())).thenReturn(validationResponse);
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        draftService.saveDraft(INTYG_ID, UTKAST_VERSION, INTYG_JSON, false);

        verify(mockUtkastRepository).save(any(Utkast.class));
        verify(utkast, times(0)).setPatientFornamn(null);
        verify(utkast, times(0)).setPatientEfternamn("Tolvansson");
        verify(utkast).setPatientPersonnummer(any(Personnummer.class));
    }

    @Test
    public void testValidateValidDraftWithWarningsIncludesWarningsInResponse() throws ModuleException, ModuleNotFoundException {
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(anyString())).thenReturn(buildValidationResponse());
        DraftValidation validationResult = draftService.validateDraft(INTYG_ID, INTYG_TYPE, utkast.getModel());
        assertEquals(1, validationResult.getWarnings().size());
        assertEquals(0, validationResult.getMessages().size());
    }

    @Test
    public void testSetKlarForSigneraStatusMessageSent() {
        WebCertUser user = createUser();
        when(userService.getUser()).thenReturn(user);
        when(mockUtkastRepository.findByIntygsIdAndIntygsTyp(INTYG_ID, "luae_fs")).thenReturn(utkast);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(authoritiesHelper.getIntygstyperForPrivilege(any(UserDetails.class), anyString()))
                .thenReturn(new HashSet<>(Arrays.asList("lisjp", "luse", "luae_fs", "luae_na")));

        draftService.setKlarForSigneraAndSendStatusMessage(INTYG_ID, "luae_fs");

        verify(notificationService).sendNotificationForDraftReadyToSign(utkast);
        verify(mockMonitoringService).logUtkastMarkedAsReadyToSignNotificationSent(INTYG_ID, "luae_fs");
        verify(mockUtkastRepository).save(utkast);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSetKlarForSigneraStatusMessageSentThrowsExceptionForLakare() {
        draftService.setKlarForSigneraAndSendStatusMessage(INTYG_ID, INTYG_TYPE);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSetKlarForSigneraStatusMessageSentThrowsExceptionForInvalidIntygsTyp() {
        when(authoritiesHelper.getIntygstyperForPrivilege(any(), any()))
                .thenReturn(new HashSet<>(Arrays.asList("lisjp", "luse", "luae_fs", "luae_na")));
        draftService.setKlarForSigneraAndSendStatusMessage(INTYG_ID, INTYG_TYPE);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSetKlarForSigneraStatusMessageSentThatIsSigned() {
        WebCertUser user = createUser();
        when(userService.getUser()).thenReturn(user);
        when(authoritiesHelper.getIntygstyperForPrivilege(any(UserDetails.class), anyString()))
                .thenReturn(new HashSet<>(Arrays.asList("lisjp", "luse", "luae_fs", "luae_na")));
        when(mockUtkastRepository.findByIntygsIdAndIntygsTyp(INTYG_ID, "luae_fs")).thenReturn(signedUtkast);

        draftService.setKlarForSigneraAndSendStatusMessage(INTYG_ID, "luae_fs");

        // Assert notification message
        verifyZeroInteractions(notificationService);

        verifyZeroInteractions(mockMonitoringService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSetKlarForSigneraStatusMessageSentThatIsLocked() {
        WebCertUser user = createUser();
        when(userService.getUser()).thenReturn(user);
        when(authoritiesHelper.getIntygstyperForPrivilege(any(UserDetails.class), anyString()))
                .thenReturn(new HashSet<>(Arrays.asList("lisjp", "luse", "luae_fs", "luae_na")));
        when(mockUtkastRepository.findByIntygsIdAndIntygsTyp(INTYG_ID, "luae_fs")).thenReturn(lockedUtkast);

        draftService.setKlarForSigneraAndSendStatusMessage(INTYG_ID, "luae_fs");

        // Assert notification message
        verifyZeroInteractions(notificationService);

        verifyZeroInteractions(mockMonitoringService);
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
        when(mockUtkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(PERSONNUMMER.getPersonnummerWithDash(), activeModules))
                .thenReturn(Arrays.asList(db1, db2, doi));

        Map<String, Map<String, PreviousIntyg>> res = draftService.checkIfPersonHasExistingIntyg(PERSONNUMMER, createUser());

        assertNotNull(res.get("utkast"));
        assertTrue(res.get("utkast").get("db").isSameVardgivare());
        assertEquals(res.get("utkast").get("db").getLatestIntygsId(), "db2");
        assertFalse(res.get("utkast").get("doi").isSameVardgivare());

        verify(mockUtkastRepository).findAllByPatientPersonnummerAndIntygsTypIn(eq(PERSONNUMMER.getPersonnummerWithDash()),
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
        when(mockUtkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(PERSONNUMMER.getPersonnummerWithDash(), activeModules))
                .thenReturn(Arrays.asList(db1, db2));

        Map<String, Map<String, PreviousIntyg>> res = draftService.checkIfPersonHasExistingIntyg(PERSONNUMMER, createUser());

        assertNotNull(res.get("utkast"));
        assertTrue(res.get("utkast").get("db").isSameVardgivare());
        assertEquals(res.get("utkast").get("db").getLatestIntygsId(), "db1");

        verify(mockUtkastRepository).findAllByPatientPersonnummerAndIntygsTypIn(eq(PERSONNUMMER.getPersonnummerWithDash()),
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
        when(mockUtkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(PERSONNUMMER.getPersonnummerWithDash(), activeModules))
                .thenReturn(Arrays.asList(db1, db2, doi));

        Map<String, Map<String, PreviousIntyg>> res = draftService.checkIfPersonHasExistingIntyg(PERSONNUMMER, createUser());

        assertNotNull(res.get("intyg"));
        assertTrue(res.get("intyg").get("db").isSameVardgivare());
        assertEquals(res.get("intyg").get("db").getLatestIntygsId(), "db2");
        assertFalse(res.get("intyg").get("doi").isSameVardgivare());

        verify(mockUtkastRepository).findAllByPatientPersonnummerAndIntygsTypIn(eq(PERSONNUMMER.getPersonnummerWithDash()),
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
        when(mockUtkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(PERSONNUMMER.getPersonnummerWithDash(), activeModules))
                .thenReturn(Arrays.asList(db1, db2));

        Map<String, Map<String, PreviousIntyg>> res = draftService.checkIfPersonHasExistingIntyg(PERSONNUMMER, createUser());

        assertNotNull(res.get("intyg"));
        assertTrue(res.get("intyg").get("db").isSameVardgivare());
        assertEquals(res.get("intyg").get("db").getLatestIntygsId(), "db2");

        verify(mockUtkastRepository).findAllByPatientPersonnummerAndIntygsTypIn(eq(PERSONNUMMER.getPersonnummerWithDash()),
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
        when(mockUtkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(PERSONNUMMER.getPersonnummerWithDash(), activeModules))
                .thenReturn(Arrays.asList(db1, db2));

        Map<String, Map<String, PreviousIntyg>> res = draftService.checkIfPersonHasExistingIntyg(PERSONNUMMER, createUser());

        assertNotNull(res.get("intyg"));
        assertTrue(res.get("intyg").get("db").isSameVardgivare());
        assertEquals(res.get("intyg").get("db").getLatestIntygsId(), "db1");

        verify(mockUtkastRepository).findAllByPatientPersonnummerAndIntygsTypIn(eq(PERSONNUMMER.getPersonnummerWithDash()),
                eq(activeModules));
    }

    @Test
    public void testNumberOfUnsignedDraftsByCareUnits() {
        List<GroupableItem> queryResult = new ArrayList<>();

        when(userService.getUser()).thenReturn(createUser());
        when(mockUtkastRepository.getIntygWithStatusesByEnhetsId(anyList(), anySet(), anySet())).thenReturn(queryResult);

        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("HSA1", 2L);

        when(statisticsGroupByUtil.toSekretessFilteredMap(queryResult)).thenReturn(resultMap);

        Map<String, Long> result = draftService.getNbrOfUnsignedDraftsByCareUnits(Arrays.asList("HSA1", "HSA2"));

        verify(mockUtkastRepository, times(1)).getIntygWithStatusesByEnhetsId(anyList(), anySet(), anySet());
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

        when(mockUtkastRepository.findDraftsByNotLockedOrSignedAndSkapadBefore(any())).thenReturn(utkastList);

        int changed = draftService.lockOldDrafts(lockedAfterDay, today);

        assertEquals(2 , changed);
        verify(mockUtkastRepository, times(2)).save(any(Utkast.class));
        verify(mockUtkastRepository, times(2)).removeRelationsToDraft(anyString());

        assertNull(utkast1.getRelationIntygsId());
        assertNull(utkast1.getRelationKod());
        assertEquals(UtkastStatus.DRAFT_LOCKED, utkast1.getStatus());

        assertEquals(UtkastStatus.DRAFT_LOCKED, utkast2.getStatus());
    }

    @Test
    public void testRevokeLockedDraft() {
        WebCertUser user = createUser();
        when(userService.getUser()).thenReturn(user);
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(lockedUtkast);
        when(userService.isAuthorizedForUnit(any(), any(), eq(false))).thenReturn(true);

        String reason = "reason";
        String revokeMessage = "revokeMessage";

        draftService.revokeLockedDraft(INTYG_ID, INTYG_TYPE, revokeMessage, reason);

        // Assert notification message
        verify(notificationService).sendNotificationForDraftRevoked(any(Utkast.class));
        verify(mockUtkastRepository, times(1)).save(lockedUtkast);
        verify(mockMonitoringService).logUtkastRevoked(INTYG_ID, user.getHsaId(), reason, revokeMessage);
        verify(logService).logRevokeIntyg(any());
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeLockedDraftWrongUnit() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(lockedUtkast);
        when(userService.isAuthorizedForUnit(any(), any(), eq(false))).thenReturn(false);

        String reason = "";

        draftService.revokeLockedDraft(INTYG_ID, INTYG_TYPE, "", reason);
        verifyZeroInteractions(mockMonitoringService);
        verifyZeroInteractions(logService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeLockedDraftNull() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(null);

        draftService.revokeLockedDraft(INTYG_ID, INTYG_TYPE, "", "");
        verifyZeroInteractions(mockMonitoringService);
        verifyZeroInteractions(logService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeLockedDraftNotLocked() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);

        draftService.revokeLockedDraft(INTYG_ID, INTYG_TYPE, "", "");
        verifyZeroInteractions(mockMonitoringService);
        verifyZeroInteractions(logService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeLockedDraftSigned() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);

        draftService.revokeLockedDraft(INTYG_ID, INTYG_TYPE, "", "");
        verifyZeroInteractions(mockMonitoringService);
        verifyZeroInteractions(logService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeLockedDraftTypeMissMatch() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(lockedUtkast);

        draftService.revokeLockedDraft(INTYG_ID, INTYG_TYPE2, "", "");
        verifyZeroInteractions(mockMonitoringService);
        verifyZeroInteractions(logService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeLockedDraftAlreadyRevoked() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(lockedUtkast);

        draftService.revokeLockedDraft(INTYG_ID, INTYG_TYPE2, "", "");
        verifyZeroInteractions(mockMonitoringService);
        verifyZeroInteractions(logService);
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
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));

        user.setHsaId("hsaId");
        user.setNamn("namn");
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

        vardgivare.setVardenheter(Arrays.asList(vardenhet));
        user.setVardgivare(Arrays.asList(vardgivare));

        user.setParameters(new IntegrationParameters(USER_REFERENCE, "", "", "", "", "", "", "", "", false, false, false, true));

        return user;
    }

}
