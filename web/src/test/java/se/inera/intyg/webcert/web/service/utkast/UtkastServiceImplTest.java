package se.inera.intyg.webcert.web.service.utkast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.HoSPersonal;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.ValidateDraftResponse;
import se.inera.certificate.modules.support.api.dto.ValidationMessage;
import se.inera.certificate.modules.support.api.dto.ValidationMessageType;
import se.inera.certificate.modules.support.api.dto.ValidationStatus;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.common.security.authority.UserPrivilege;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveAndValidateDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveAndValidateDraftResponse;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;

import javax.persistence.OptimisticLockException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class UtkastServiceImplTest {

    private static final String INTYG_ID = "abc123";
    private static final String INTYG_COPY_ID = "def456";
    private static final String INTYG_JSON = "A bit of text representing json";
    private static final String INTYG_TYPE = "fk7263";

    private static final long UTKAST_VERSION = 1;
    private static final long INTYG_VERSION = 2;

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

    @Spy
    private CreateIntygsIdStrategy mockIdStrategy = new CreateIntygsIdStrategy() {
        @Override
        public String createId() {
            return INTYG_COPY_ID;
        }
    };

    @InjectMocks
    private UtkastService draftService = new UtkastServiceImpl();

    private Utkast utkast;
    private Utkast signedUtkast;
    private HoSPerson hoSPerson;

    @Before
    public void setup() {
        hoSPerson = new HoSPerson();
        hoSPerson.setHsaId("AAA");
        hoSPerson.setNamn("Dr Dengroth");
        hoSPerson.setBefattning("Befattning");
        hoSPerson.getSpecialiseringar().add("Ortoped");

        se.inera.intyg.webcert.web.service.dto.Vardgivare vardgivare = new se.inera.intyg.webcert.web.service.dto.Vardgivare();
        vardgivare.setHsaId("SE234234");
        vardgivare.setNamn("Vårdgivaren");

        se.inera.intyg.webcert.web.service.dto.Vardenhet vardenhet = new se.inera.intyg.webcert.web.service.dto.Vardenhet();
        vardenhet.setArbetsplatskod("00000");
        vardenhet.setNamn("Vårdenheten");
        vardenhet.setHsaId("SE234897348");
        vardenhet.setPostadress("Sjukvägen 1");
        vardenhet.setPostnummer("12345");
        vardenhet.setNamn("Testberga");
        vardenhet.setTelefonnummer("0123-456789");
        vardenhet.setEpost("ingen@ingen.se");
        vardenhet.setVardgivare(vardgivare);

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hoSPerson.getHsaId());
        vardperson.setNamn(hoSPerson.getNamn());

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
        verify(notificationService).sendNotificationForDraftDeleted(any(Utkast.class));

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

    @Test
    public void testLogPrintOfIntygAsDraft() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        draftService.logPrintOfDraftToPDL(INTYG_ID);

        // Assert pdl log
        verify(logService).logPrintIntygAsDraft(any(LogRequest.class));

        verify(mockMonitoringService).logUtkastPrint(INTYG_ID, INTYG_TYPE);
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
    public void testSaveAndValidateDraftFirstSave() throws Exception {

        ModuleApi mockModuleApi = mock(ModuleApi.class);
        SaveAndValidateDraftRequest request = buildSaveAndValidateRequest(utkast);
        ValidationMessage valMsg = new ValidationMessage("a.field.somewhere", ValidationMessageType.OTHER, "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Collections.singletonList(valMsg));
        WebCertUser user = createUser();

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(validationResponse);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(mockModuleApi.isModelChanged(any(String.class), any(String.class))).thenReturn(true);
        when(userService.getUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(any(InternalModelHolder.class), any(HoSPersonal.class))).thenReturn(
                new InternalModelResponse("{}"));

        SaveAndValidateDraftResponse res = draftService.saveAndValidateDraft(request, true);

        verify(mockUtkastRepository).save(any(Utkast.class));

        // Assert notification message
        verify(notificationService).sendNotificationForDraftChanged(any(Utkast.class));

        // Assert pdl log
        verify(logService).logUpdateIntyg(any(LogRequest.class));

        verify(mockMonitoringService).logUtkastEdited(INTYG_ID, INTYG_TYPE);

        assertNotNull("An DraftValidation should be returned", res);
        assertFalse("Validation should fail", res.getDraftValidation().isDraftValid());
        assertEquals("Validation should have 1 message", 1, res.getDraftValidation().getMessages().size());
    }

    @Test
    public void testSaveAndValidateDraftSecondSave() throws Exception {

        ModuleApi mockModuleApi = mock(ModuleApi.class);
        SaveAndValidateDraftRequest request = buildSaveAndValidateRequest(utkast);
        ValidationMessage valMsg = new ValidationMessage("a.field.somewhere", ValidationMessageType.OTHER, "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Collections.singletonList(valMsg));
        WebCertUser user = createUser();

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(validationResponse);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(mockModuleApi.isModelChanged(any(String.class), any(String.class))).thenReturn(true);
        when(userService.getUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(any(InternalModelHolder.class), any(HoSPersonal.class))).thenReturn(
                new InternalModelResponse("{}"));

        SaveAndValidateDraftResponse res = draftService.saveAndValidateDraft(request, false);

        verify(mockUtkastRepository).save(any(Utkast.class));

        // Assert notification message
        verify(notificationService).sendNotificationForDraftChanged(any(Utkast.class));

        // Assert that no logs are called
        verifyZeroInteractions(logService);
        verifyZeroInteractions(mockMonitoringService);

        assertNotNull("An DraftValidation should be returned", res);
        assertFalse("Validation should fail", res.getDraftValidation().isDraftValid());
        assertEquals("Validation should have 1 message", 1, res.getDraftValidation().getMessages().size());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveAndValidateDraftThatIsSigned() {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);

        draftService.saveAndValidateDraft(buildSaveAndValidateRequest(signedUtkast), false);

        verify(mockUtkastRepository).findOne(INTYG_ID);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = WebCertServiceException.class)
    public void testSaveAndValidateDraftWithExceptionInModule() throws Exception {

        ModuleApi mockModuleApi = mock(ModuleApi.class);
        SaveAndValidateDraftRequest request = buildSaveAndValidateRequest(utkast);
        WebCertUser user = createUser();

        when(userService.getUser()).thenReturn(user);
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.updateBeforeSave(any(InternalModelHolder.class), any(HoSPersonal.class))).thenReturn(new InternalModelResponse("{}"));
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenThrow(ModuleException.class);

        draftService.saveAndValidateDraft(request, false);
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

    private SaveAndValidateDraftRequest buildSaveAndValidateRequest(Utkast utkast) {
        SaveAndValidateDraftRequest request = new SaveAndValidateDraftRequest();
        request.setIntygId(utkast.getIntygsId());
        request.setVersion(utkast.getVersion());
        request.setDraftAsJson(utkast.getModel());
        request.setSavedBy(hoSPerson);
        request.setAutoSave(false);
        return request;
    }

    private Utkast createUtkast(String intygId, long version, String type, UtkastStatus status, String model, VardpersonReferens vardperson) {
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
        WebCertUser user = new WebCertUser();
        user.setRoles(getGrantedRole());
        user.setAuthorities(getGrantedPrivileges());

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
        vardenhet.setId("enhetid");
        vardenhet.setNamn("enhetnamn");
        user.setValdVardenhet(vardenhet);
        return user;
    }

    private Map<String, UserRole> getGrantedRole() {
        Map<String, UserRole> map = new HashMap<>();
        map.put(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_LAKARE);
        return map;
    }

    private Map<String, UserPrivilege> getGrantedPrivileges() {
        List<UserPrivilege> list = Arrays.asList(UserPrivilege.values());

        // convert list to map
        Map<String, UserPrivilege> privilegeMap = Maps.uniqueIndex(list, new Function<UserPrivilege, String>() {
            @Override
            public String apply(UserPrivilege userPrivilege) {
                return userPrivilege.name();
            }
        });

        return privilegeMap;
    }

}
