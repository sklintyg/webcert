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
package se.inera.intyg.webcert.web.web.controller.moduleapi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessageType;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveDraftResponse;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeSignedIntygParameter;

import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtkastModuleApiControllerTest {

    private static final String CERTIFICATE_ID = "123";

    private static final long UTKAST_VERSION = 1;

    private static final UtkastStatus UTKAST_STATUS = UtkastStatus.DRAFT_COMPLETE;

    private static final String UTKAST_ENHETSNAMN = "Enhetsnamn";

    private static final String UTKAST_VARDGIVARNAMN = "Vardgivarnamn";

    private static final String UTKAST_MODEL = "<Model>";

    private static final String UTKAST_PERSONNUMMER = "19121212-1212";
    private static final String INTYG_TYPE_VERSION = "1.0";

    private HttpServletRequest request;
    private HttpSession session;

    @Mock
    private IntygTextsService intygTextsService;

    @Mock
    private CertificateRelationService certificateRelationService;

    @Mock
    private UtkastService utkastService;

    @Mock
    private WebCertUserService webcertUserService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @Mock
    private CopyUtkastService copyUtkastService;

    @Spy
    private CopyUtkastServiceHelper copyUtkastServiceHelper = new CopyUtkastServiceHelper();

    @InjectMocks
    private UtkastModuleApiController moduleApiController = new UtkastModuleApiController();

    @Before
    public void setup() throws ModuleNotFoundException, ModuleException, IOException {
        copyUtkastServiceHelper.setWebCertUserService(webcertUserService);

        session = mock(HttpSession.class);
        request = mock(HttpServletRequest.class);
        Mockito.doNothing().when(session).removeAttribute("lastSavedDraft");
        when(request.getSession(true)).thenReturn(session);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString())).thenReturn(buildPatient());

        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(new Fk7263Utlatande());
        when(moduleApi.updateBeforeSave(anyString(), any(Patient.class))).thenReturn("MODEL");
    }

    @Test
    public void testGetDraft() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, false, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        when(utkastService.getDraft(CERTIFICATE_ID, intygTyp)).thenReturn(buildUtkast(intygTyp, intygId));
        when(certificateRelationService.getRelations(eq(intygId))).thenReturn(new Relations());

        Response response = moduleApiController.getDraft(intygTyp, CERTIFICATE_ID, request);
        verify(utkastService).getDraft(CERTIFICATE_ID, intygTyp);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = AuthoritiesException.class)
    public void getDraftWithoutPrivilegeSkrivaIntygFails() {
        String intygType = "fk7263";
        setupUser("", intygType, false, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        when(utkastService.getDraft(CERTIFICATE_ID, intygType)).thenReturn(buildUtkast(intygType, CERTIFICATE_ID));
        moduleApiController.getDraft(intygType, CERTIFICATE_ID, request);
    }

    @Test
    public void getDraftShouldPatchIfNewAddressExist() throws Exception {
        // Given
        String intygsTyp = "gdspinae-intygsTyp";
        String intygsId = "gdspinae-intygsId";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, false, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        when(utkastService.getDraft(CERTIFICATE_ID, intygsTyp)).thenReturn(buildUtkast(intygsTyp, intygsId));
        when(certificateRelationService.getRelations(eq(intygsId))).thenReturn(new Relations());

        String postadress = "gdspinae-postadress";
        String postort = "gdspinae-postort";
        String postnummer = "gdspinae-postnummer";
        Patient patientWithIncompleteAddress = buildPatient();
        patientWithIncompleteAddress.setPostadress(postadress);
        patientWithIncompleteAddress.setPostort(postort);
        patientWithIncompleteAddress.setPostnummer(postnummer);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString()))
                .thenReturn(patientWithIncompleteAddress);

        // When
        Response response = moduleApiController.getDraft(intygsTyp, CERTIFICATE_ID, request);

        // Then
        ArgumentCaptor<Patient> argumentCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(moduleApi).updateBeforeSave(anyString(), argumentCaptor.capture());
        assertEquals(postadress, argumentCaptor.getValue().getPostadress());
        assertEquals(postort, argumentCaptor.getValue().getPostort());
        assertEquals(postnummer, argumentCaptor.getValue().getPostnummer());
    }

    @Test
    public void getDraftShouldNotPatchIfNewAddressIsIncomplete() throws Exception {
        // Given
        String intygsTyp = "gdsnpinaii-intygsTyp";
        String intygsId = "gdsnpinaii-intygsId";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, false, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        when(utkastService.getDraft(CERTIFICATE_ID, intygsTyp)).thenReturn(buildUtkast(intygsTyp, intygsId));
        when(certificateRelationService.getRelations(eq(intygsId))).thenReturn(new Relations());

        String postadress = "gdsnpinaii-postadress";
        String postort = "";
        String postnummer = "gdsnpinaii-postnummer";
        Patient patientWithIncompleteAddress = buildPatient();
        patientWithIncompleteAddress.setPostadress(postadress);
        patientWithIncompleteAddress.setPostort(postort);
        patientWithIncompleteAddress.setPostnummer(postnummer);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString()))
                .thenReturn(patientWithIncompleteAddress);

        // When
        Response response = moduleApiController.getDraft(intygsTyp, CERTIFICATE_ID, request);

        // Then
        ArgumentCaptor<Patient> argumentCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(moduleApi).updateBeforeSave(anyString(), argumentCaptor.capture());
        assertNotEquals(postadress, argumentCaptor.getValue().getPostadress());
        assertNotEquals(postnummer, argumentCaptor.getValue().getPostnummer());
    }

    @Test
    public void testSaveDraft() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        String draftAsJson = "test";
        byte[] payload = draftAsJson.getBytes();
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, false, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        when(utkastService.saveDraft(intygId, UTKAST_VERSION, draftAsJson, true))
                .thenReturn(new SaveDraftResponse(UTKAST_VERSION, UtkastStatus.DRAFT_COMPLETE));

        Response response = moduleApiController.saveDraft(intygTyp, intygId, UTKAST_VERSION, false, payload, request);

        verify(utkastService).saveDraft(intygId, UTKAST_VERSION, draftAsJson, true);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = AuthoritiesException.class)
    public void saveDraftWithoutPrivilegeSkrivaIntygFails() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        byte[] payload = "test".getBytes();
        setupUser("", intygTyp, false, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        moduleApiController.saveDraft(intygTyp, intygId, UTKAST_VERSION, false, payload, request);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveDraftOptimisticLockException() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        String draftAsJson = "test";
        byte[] payload = draftAsJson.getBytes();
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, false, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        when(utkastService.saveDraft(intygId, UTKAST_VERSION, draftAsJson, true)).thenThrow(new OptimisticLockException(""));

        try {
            moduleApiController.saveDraft(intygTyp, intygId, UTKAST_VERSION, false, payload, request);
        } finally {
            verify(monitoringLogService).logUtkastConcurrentlyEdited(intygId, intygTyp);
        }
    }

    @Test
    public void testValidateDraft() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        String draftAsJson = "test";
        byte[] payload = draftAsJson.getBytes();
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, false, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        when(utkastService.validateDraft(intygId, intygTyp, draftAsJson)).thenReturn(buildDraftValidation());

        Response response = moduleApiController.validateDraft(intygTyp, intygId, payload);

        verify(utkastService).validateDraft(intygId, intygTyp, draftAsJson);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = AuthoritiesException.class)
    public void testValidateDraftUnauthorized() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        String draftAsJson = "test";
        byte[] payload = draftAsJson.getBytes();
        setupUser("", intygTyp, false, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        try {
            moduleApiController.validateDraft(intygTyp, intygId, payload);
        } finally {
            verifyZeroInteractions(utkastService);
        }
    }

    @Test
    public void testDiscardDraft() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, false, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        Response response = moduleApiController.discardDraft(intygTyp, intygId, UTKAST_VERSION, request);

        verify(utkastService).deleteUnsignedDraft(intygId, UTKAST_VERSION);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = AuthoritiesException.class)
    public void discardDraftWithoutPrivilegeSkrivaIntygFails() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        setupUser("", intygTyp, false, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        moduleApiController.discardDraft(intygTyp, intygId, UTKAST_VERSION, request);
    }

    @Test
    public void testValidateDraftWithWarningsArePropagatedToCaller() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        String draftAsJson = "test";
        byte[] payload = draftAsJson.getBytes();
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, false, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        DraftValidation draftValidation = buildDraftValidation();
        draftValidation.addWarning(
                new DraftValidationMessage("category", "field", ValidationMessageType.WARN, "this.is.a.message", "dy.nam.ic.key"));

        when(utkastService.validateDraft(intygId, intygTyp, draftAsJson)).thenReturn(draftValidation);

        Response response = moduleApiController.validateDraft(intygTyp, intygId, payload);

        DraftValidation entity = (DraftValidation) response.getEntity();

        verify(utkastService).validateDraft(intygId, intygTyp, draftAsJson);
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(ValidationStatus.VALID, entity.getStatus());
        assertEquals(0, entity.getMessages().size());
        assertEquals(1, entity.getWarnings().size());
    }

    @Test
    public void testCopyUtkast() {
        String intygId = "intygId";
        String newIntygId = "newIntygId";
        String intygTyp = "fk7263";
        String intygTypeVersion = "1.0";

        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, false, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        Utkast utkast = new Utkast();
        utkast.setPatientPersonnummer(Personnummer.createPersonnummer("19121212-1212").get());
        when(utkastService.getDraft(eq(intygId), eq(intygTyp))).thenReturn(utkast);

        ArgumentCaptor<CreateUtkastFromTemplateRequest> captor = ArgumentCaptor.forClass(CreateUtkastFromTemplateRequest.class);
        when(copyUtkastService.createUtkastCopy(captor.capture()))
                .thenReturn(new CreateUtkastFromTemplateResponse(intygTyp, intygTypeVersion, newIntygId, intygId));

        Response response = moduleApiController.copyUtkast(intygTyp, intygId);

        verify(copyUtkastService).createUtkastCopy(any());
        verifyNoMoreInteractions(copyUtkastService);
        assertEquals(newIntygId, ((CopyIntygResponse) response.getEntity()).getIntygsUtkastId());
        assertEquals(intygTyp, ((CopyIntygResponse) response.getEntity()).getIntygsTyp());
    }

    @Test(expected = AuthoritiesException.class)
    public void testCopyUtkastUnauthorized() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        setupUser("", intygTyp, false, AuthoritiesConstants.FEATURE_MAKULERA_INTYG);

        moduleApiController.copyUtkast(intygTyp, intygId);

        verifyZeroInteractions(utkastService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testCopyUtkastKopieraOKFalse() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        IntegrationParameters integrationParameters = IntegrationParameters.of("", "", "", "", "", "", "", "", "", false, false, false,
                false);
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, integrationParameters,
                AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        moduleApiController.copyUtkast(intygTyp, intygId);

        verifyZeroInteractions(utkastService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testCopyUtkastInaktivEnhetTrue() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        IntegrationParameters integrationParameters = IntegrationParameters.of("", "", "", "", "", "", "", "", "", false, false, true,
                true);
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, integrationParameters,
                AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        moduleApiController.copyUtkast(intygTyp, intygId);

        verifyZeroInteractions(utkastService);
    }

    @Test(expected = AuthoritiesException.class)
    public void testCopyUtkastAvlidenTrue() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        IntegrationParameters integrationParameters = IntegrationParameters.of("", "", "", "", "", "", "", "", "", false, true, false,
                true);
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, integrationParameters,
                AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        Utkast utkast = new Utkast();
        Personnummer personnummer = Personnummer.createPersonnummer("19121212-1212").get();
        utkast.setPatientPersonnummer(personnummer);
        when(utkastService.getDraft(eq(intygId), eq(intygTyp))).thenReturn(utkast);

        when(patientDetailsResolver.isAvliden(personnummer)).thenReturn(true);

        moduleApiController.copyUtkast(intygTyp, intygId);

        verifyZeroInteractions(utkastService);
    }

    @Test
    public void testRevokeLockedDraft() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        setupUser(AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG, intygTyp, false, AuthoritiesConstants.FEATURE_MAKULERA_INTYG);
        RevokeSignedIntygParameter param = new RevokeSignedIntygParameter();

        Response response = moduleApiController.revokeLockedDraft(intygTyp, intygId, param);

        verify(utkastService).revokeLockedDraft(intygId, intygTyp, param.getMessage(), param.getReason());
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = AuthoritiesException.class)
    public void testRevokeLockedDraftUnauthorized() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        setupUser("", intygTyp, false, AuthoritiesConstants.FEATURE_MAKULERA_INTYG);
        RevokeSignedIntygParameter param = new RevokeSignedIntygParameter();

        moduleApiController.revokeLockedDraft(intygTyp, intygId, param);

        verifyZeroInteractions(utkastService);
    }

    private DraftValidation buildDraftValidation() {
        DraftValidation validation = new DraftValidation();
        validation.setMessages(new ArrayList<>());
        validation.setStatus(ValidationStatus.VALID);
        return validation;
    }

    private Utkast buildUtkast(String intygType, String intygId) {
        Utkast utkast = new Utkast();
        utkast.setVersion(UTKAST_VERSION);
        utkast.setVidarebefordrad(false);
        utkast.setStatus(UTKAST_STATUS);
        utkast.setEnhetsNamn(UTKAST_ENHETSNAMN);
        utkast.setVardgivarNamn(UTKAST_VARDGIVARNAMN);
        utkast.setModel(UTKAST_MODEL);
        utkast.setIntygsTyp(intygType);
        utkast.setIntygTypeVersion(INTYG_TYPE_VERSION);
        utkast.setIntygsId(intygId);
        utkast.setPatientPersonnummer(Personnummer.createPersonnummer(UTKAST_PERSONNUMMER).get());
        return utkast;
    }

    private Patient buildPatient() {
        Patient patient = new Patient();
        patient.setFornamn("Tolvan");
        patient.setEfternamn("Tolvansson");
        return patient;
    }

    private void setupUser(String privilegeString, String intygType, boolean coherentJournaling, String... features) {
        IntegrationParameters integrationParameters = new IntegrationParameters("", "", "", "", "", "", "", "", "", coherentJournaling,
                false, false, true);

        setupUser(privilegeString, intygType, integrationParameters, features);
    }

    private void setupUser(String privilegeString, String intygType, IntegrationParameters integrationParameters, String... features) {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());
        user.setFeatures(Stream.of(features).collect(Collectors.toMap(Function.identity(), s -> {
            Feature feature = new Feature();
            feature.setName(s);
            feature.setIntygstyper(Arrays.asList(intygType));
            feature.setGlobal(true);
            return feature;
        })));
        user.setParameters(integrationParameters);
        Privilege privilege = new Privilege();
        privilege.setIntygstyper(Arrays.asList(intygType));
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName("NORMAL");
        requestOrigin.setIntygstyper(privilege.getIntygstyper());
        privilege.setRequestOrigins(Arrays.asList(requestOrigin));
        user.getAuthorities().put(privilegeString, privilege);
        user.setOrigin("NORMAL");
        user.setAuthenticationMethod(AuthenticationMethod.FAKE);
        when(webcertUserService.getUser()).thenReturn(user);
    }
}
