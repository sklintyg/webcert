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
package se.inera.intyg.webcert.web.service.utkast;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModule;
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
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserDetails;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveDraftResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.UpdatePatientOnDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

import javax.persistence.OptimisticLockException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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

    private static final String INTYG_ID = "abc123";
    private static final String INTYG_COPY_ID = "def456";
    private static final String INTYG_JSON = "A bit of text representing json";
    private static final String INTYG_TYPE = "fk7263";

    private static final long UTKAST_VERSION = 1;
    private static final long INTYG_VERSION = 2;
    private static final String UTKAST_ENHETS_ID = "hsa123";

    private static final String USER_REFERENCE = "some-ref";

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
    private WebcertFeatureService featureService;

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
    private Utkast signedUtkast;
    private HoSPersonal hoSPerson;
    private Patient defaultPatient;

    @Before
    public void setup() {
        hoSPerson = new HoSPersonal();
        hoSPerson.setPersonId("AAA");
        hoSPerson.setFullstandigtNamn("Dr Dengroth");
        hoSPerson.getBefattningar().add("Befattning");
        hoSPerson.getSpecialiteter().add("Ortoped");

        defaultPatient = new Patient();
        defaultPatient.setPersonId(new Personnummer("19121212-1212"));
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

        utkast = createUtkast(INTYG_ID, UTKAST_VERSION, INTYG_TYPE, UtkastStatus.DRAFT_INCOMPLETE, INTYG_JSON, vardperson);
        signedUtkast = createUtkast(INTYG_ID, INTYG_VERSION, INTYG_TYPE, UtkastStatus.SIGNED, INTYG_JSON, vardperson);

    }

    @Test
    public void testDeleteDraftThatIsUnsigned() {
        WebCertUser user = createUser();

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);

        draftService.deleteUnsignedDraft(INTYG_ID, utkast.getVersion());

        verify(mockUtkastRepository).findOne(INTYG_ID);
        verify(mockUtkastRepository).delete(utkast);

        // Assert notification message
        verify(notificationService).sendNotificationForDraftDeleted(any(Utkast.class), anyString());

        // Assert pdl log
        verify(logService).logDeleteIntyg(any(LogRequest.class));

        verify(mockMonitoringService).logUtkastDeleted(INTYG_ID, INTYG_TYPE);
    }

    @Test
    public void testDeleteDraftWrongVersion() {
        WebCertUser user = createUser();

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);

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

    @Test
    public void testSaveDraftDraftFirstSave() throws Exception {
        ValidationMessage valMsg = new ValidationMessage("a.field.somewhere", ValidationMessageType.OTHER, "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Collections.singletonList(valMsg));
        WebCertUser user = createUser();
        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();
        grunddata.setSkapadAv(new HoSPersonal());
        grunddata.setPatient(defaultPatient);
        when(utlatande.getGrundData()).thenReturn(grunddata);

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(anyString())).thenReturn(validationResponse);
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(mockModuleApi.shouldNotify(any(String.class), any(String.class))).thenReturn(true);
        when(userService.getUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        SaveDraftResponse res = draftService.saveDraft(INTYG_ID, UTKAST_VERSION, INTYG_JSON, true);

        verify(mockUtkastRepository).save(any(Utkast.class));

        // Assert notification message
        verify(notificationService).sendNotificationForDraftChanged(any(Utkast.class), anyString());

        // Assert pdl log
        verify(logService).logUpdateIntyg(any(LogRequest.class));

        verify(mockMonitoringService).logUtkastEdited(INTYG_ID, INTYG_TYPE);

        assertNotNull("An DraftValidation should be returned", res);
        assertEquals("Validation should fail", UtkastStatus.DRAFT_INCOMPLETE, res.getStatus());
    }

    @Test
    public void testSaveDraftSecondSave() throws Exception {
        ValidationMessage valMsg = new ValidationMessage("a.field.somewhere", ValidationMessageType.OTHER, "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Collections.singletonList(valMsg));
        WebCertUser user = createUser();
        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();
        grunddata.setSkapadAv(new HoSPersonal());
        grunddata.setPatient(defaultPatient);
        when(utlatande.getGrundData()).thenReturn(grunddata);

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(anyString())).thenReturn(validationResponse);
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(mockModuleApi.shouldNotify(any(String.class), any(String.class))).thenReturn(true);
        when(userService.getUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        SaveDraftResponse res = draftService.saveDraft(INTYG_ID, UTKAST_VERSION, INTYG_JSON, false);

        verify(mockUtkastRepository).save(any(Utkast.class));

        // Assert notification message
        verify(notificationService).sendNotificationForDraftChanged(any(Utkast.class), anyString());

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
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(mockModuleApi.validateDraft(anyString())).thenThrow(ModuleException.class);

        draftService.saveDraft(INTYG_ID, UTKAST_VERSION, INTYG_JSON, false);
    }

    @Test
    public void testValidateDraft() throws Exception {
        ValidationMessage valMsg = new ValidationMessage("a.field.somewhere", ValidationMessageType.OTHER, "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Collections.singletonList(valMsg));

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
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

    @Test
    public void testSaveUpdatesChangedPatientName() throws Exception {
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.VALID, Collections.emptyList());

        WebCertUser user = createUser();
        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();
        grunddata.setSkapadAv(new HoSPersonal());
        grunddata.setPatient(buildPatient("19121212-1212", "Tolvan", "Tolvansson"));
        when(utlatande.getGrundData()).thenReturn(grunddata);

        utkast.setPatientFornamn("Inte Tolvan");
        utkast.setPatientEfternamn("Inte Tolvansson");

        // Make a spy out of the utkast so we can verify invocations on the setters with proper names further down.
        utkast = spy(utkast);

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
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
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.updateBeforeSave(anyString(), any(Patient.class))).thenReturn("{}");
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        draftService.updatePatientOnDraft(request);

        verify(mockUtkastRepository).save(any(Utkast.class));
        verify(notificationService).sendNotificationForDraftChanged(any(Utkast.class), anyString());
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
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.updateBeforeSave(anyString(), any(Patient.class))).thenReturn("{}");
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        draftService.updatePatientOnDraft(request);

        verify(mockUtkastRepository, never()).save(any(Utkast.class));
        verify(notificationService, never()).sendNotificationForDraftChanged(any(Utkast.class), anyString());
        verify(utkast, never()).setPatientPersonnummer(any(Personnummer.class));
        assertEquals("", user.getParameters().getBeforeAlternateSsn());
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
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.updateBeforeSave(anyString(), any(Patient.class))).thenReturn("{}");
        when(mockModuleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(userService.getUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(anyString(), any(HoSPersonal.class))).thenReturn("{}");

        draftService.updatePatientOnDraft(request);

        verify(mockUtkastRepository, never()).save(any(Utkast.class));
        verify(notificationService, never()).sendNotificationForDraftChanged(any(Utkast.class), anyString());
        verify(utkast, never()).setPatientPersonnummer(any(Personnummer.class));
        assertEquals("", user.getParameters().getBeforeAlternateSsn());
    }

    @Test(expected = WebCertServiceException.class)
    public void testUpdatePatientOnDraftNoMedarbetaruppdragThrowsException() throws Exception {
        utkast.setEnhetsId("<unknownenhet>");
        Patient newPatient = getUpdatedPatient();

        UpdatePatientOnDraftRequest request = new UpdatePatientOnDraftRequest(newPatient.getPersonId(), utkast.getIntygsId(),
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
        when(userService.getUser()).thenReturn(user);

        draftService.updatePatientOnDraft(request);

        verifyNoMoreInteractions(mockUtkastRepository, notificationService);

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
        grunddata.setPatient(buildPatient("19121212-1212", null, "Tolvansson"));
        when(utlatande.getGrundData()).thenReturn(grunddata);

        utkast.setPatientFornamn(utkastFornamn);
        utkast.setPatientEfternamn(utkastEfternamn);

        // Make a spy out of the utkast so we can verify invocations on the setters with proper names further down.
        utkast = spy(utkast);

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
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
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(anyString())).thenReturn(buildValidationResponse());
        DraftValidation validationResult = draftService.validateDraft(INTYG_ID, INTYG_TYPE, utkast.getModel());
        assertEquals(1, validationResult.getWarnings().size());
        assertEquals(0, validationResult.getMessages().size());
    }

    @Test
    public void testSetKlarForSigneraStatusMessageSent() {
        WebCertUser user = createUser();
        when(userService.hasAuthenticationContext()).thenReturn(true);
        when(userService.getUser()).thenReturn(user);
        when(mockUtkastRepository.findOneByIntygsIdAndIntygsTyp(INTYG_ID, "luae_fs")).thenReturn(utkast);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(authoritiesHelper.getIntygstyperForPrivilege(any(UserDetails.class), anyString()))
                .thenReturn(new HashSet<>(Arrays.asList("lisjp", "luse", "luae_fs", "luae_na")));

        draftService.setKlarForSigneraAndSendStatusMessage(INTYG_ID, "luae_fs");

        verify(notificationService).sendNotificationForDraftReadyToSign(utkast, USER_REFERENCE);
        verify(mockMonitoringService).logUtkastMarkedAsReadyToSignNotificationSent(INTYG_ID, "luae_fs");
        verify(mockUtkastRepository).save(utkast);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSetKlarForSigneraStatusMessageSentThrowsExceptionForLakare() {
        when(authoritiesHelper.getIntygstyperForPrivilege(any(UserDetails.class), anyString())).thenReturn(new HashSet<>());
        draftService.setKlarForSigneraAndSendStatusMessage(INTYG_ID, INTYG_TYPE);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSetKlarForSigneraStatusMessageSentThrowsExceptionForInvalidIntygsTyp() {
        when(authoritiesHelper.getIntygstyperForPrivilege(any(UserDetails.class), anyString()))
                .thenReturn(new HashSet<>(Arrays.asList("lisjp", "luse", "luae_fs", "luae_na")));
        draftService.setKlarForSigneraAndSendStatusMessage(INTYG_ID, INTYG_TYPE);
    }

    @Test
    public void testCheckIfPersonHasExistingIntyg() {
        final String personnummer = "191212121212";
        final Set activeModules = new HashSet<>(Arrays.asList("db", "doi"));
        final String vardgivareId = "vardgivarid";

        Utkast db1 = createUtkast("db1", 1L, "db", UtkastStatus.SIGNED, "", null);
        db1.setVardgivarId("other");
        Utkast db2 = createUtkast("db2", 1L, "db", UtkastStatus.SIGNED, "", null);
        db2.setVardgivarId(vardgivareId);
        Utkast doi = createUtkast("doi1", 1L, "doi", UtkastStatus.SIGNED, "", null);
        doi.setVardgivarId("other");
        when(moduleRegistry.listAllModules()).thenReturn(
                Arrays.asList("lisjp", "db", "doi").stream()
                        .map(a -> new IntygModule(a, null, null, null, null, null, null, null)).collect(
                        Collectors.toList()));
        when(authoritiesHelper.getIntygstyperForModuleFeature(any(), any(), any())).thenReturn(activeModules);
        when(mockUtkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer, activeModules))
                .thenReturn(Arrays.asList(db1, db2, doi));

        Map<String, Map<String, Boolean>> res = draftService.checkIfPersonHasExistingIntyg(new Personnummer(personnummer), createUser());

        assertNotNull(res.get("intyg"));
        assertTrue(res.get("intyg").get("db"));
        assertFalse(res.get("intyg").get("doi"));

        verify(mockUtkastRepository).findAllByPatientPersonnummerAndIntygsTypIn(eq(personnummer), eq(activeModules));
    }

    private Patient getUpdatedPatient() {
        Patient newPatient = new Patient();
        newPatient.setEfternamn("updated lastName");
        newPatient.setMellannamn("updated middle-name");
        newPatient.setFornamn("updated firstName");
        newPatient.setFullstandigtNamn("updated full name");
        newPatient.setPersonId(new Personnummer("19121272-1212"));
        newPatient.setPostadress("updated postal address");
        newPatient.setPostnummer("1111111");
        newPatient.setPostort("updated post city");
        return newPatient;
    }

    private ValidateDraftResponse buildValidationResponse() {
        return new ValidateDraftResponse(ValidationStatus.VALID, Collections.emptyList(),
                Collections.singletonList(new ValidationMessage("testfield", ValidationMessageType.WARN)));
    }

    private Patient buildPatient(String pnr, String fornamn, String efternamn) {
        Patient p = new Patient();
        p.setPersonId(new Personnummer(pnr));
        p.setFornamn(fornamn);
        p.setEfternamn(efternamn);
        return p;
    }

    private Utkast createUtkast(String intygId, long version, String type, UtkastStatus status, String model,
            VardpersonReferens vardperson) {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setVersion(version);
        utkast.setIntygsTyp(type);
        utkast.setStatus(status);
        utkast.setModel(model);
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);
        return utkast;
    }

    private WebCertUser createUser() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));

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
