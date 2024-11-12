/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static jakarta.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessageType;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.LockedDraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.UtkastCandidateServiceImpl;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveDraftResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyFromCandidateRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DraftHolder;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeSignedIntygParameter;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

@RunWith(MockitoJUnitRunner.class)
public class UtkastModuleApiControllerTest {

    private static final String CERTIFICATE_ID = "123";

    private static final long UTKAST_VERSION = 1;

    private static final UtkastStatus UTKAST_STATUS = UtkastStatus.DRAFT_COMPLETE;

    private static final String UTKAST_ENHETSID = "SE0987654321";
    private static final String UTKAST_ENHETSNAMN = "Enhetsnamn";
    private static final String UTKAST_VARDGIVARNAMN = "Vardgivarnamn";
    private static final String UTKAST_MODEL = "<Model>";
    private static final String UTKAST_PERSONNUMMER = "19121212-1212";

    private static final String INTYG_TYPE_VERSION = "1.0";

    private HttpServletRequest request;
    private HttpSession session;

    private WebCertUser webCertUser;

    @Mock
    private IntygTextsService intygTextsService;

    @Mock
    private CertificateRelationService certificateRelationService;

    @Mock
    private UtkastService utkastService;

    @Mock
    private UtkastCandidateServiceImpl utkastCandidateService;

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

    @Mock
    private ResourceLinkHelper resourceLinkHelper;

    @Mock
    private DraftAccessServiceHelper draftAccessServiceHelper;

    @Mock
    private LockedDraftAccessServiceHelper lockedDraftAccessServiceHelper;

    @Spy
    private CopyUtkastServiceHelper copyUtkastServiceHelper = new CopyUtkastServiceHelper();

    @InjectMocks
    private UtkastModuleApiController moduleApiController = new UtkastModuleApiController();

    @Before
    public void setup() throws ModuleNotFoundException, ModuleException, IOException {
        copyUtkastServiceHelper.setWebCertUserService(webcertUserService);

        session = mock(HttpSession.class);
        Mockito.doNothing().when(session).removeAttribute("lastSavedDraft");

        request = mock(HttpServletRequest.class);
        when(request.getSession(true)).thenReturn(session);

        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString())).thenReturn(buildPatient());
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(moduleApi.updateBeforeSave(anyString(), any(Patient.class))).thenReturn("MODEL");
        when(intygTextsService.isLatestMajorVersion(any(String.class), any(String.class))).thenReturn(true);
    }

    @Test
    public void testGetDraft() throws Exception {
        String intygsTyp = "fk7263";
        String intygsId = "intyg1";

        setupGrundData();
        setupUser(intygsTyp, false, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG), Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        when(utkastService.getDraft(CERTIFICATE_ID, intygsTyp)).thenReturn(buildUtkast(intygsTyp, intygsId));
        when(certificateRelationService.getRelations(eq(intygsId))).thenReturn(new Relations());

        Response response = moduleApiController.getDraft(intygsTyp, CERTIFICATE_ID, request);

        verify(utkastService).getDraft(CERTIFICATE_ID, intygsTyp);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void getDraftShouldPatchIfNewAddressExist() throws Exception {
        // Given
        String intygsTyp = "gdspinae-intygsTyp";
        String intygsId = "gdspinae-intygsId";

        setupGrundData();
        setupUser(intygsTyp, false, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG), Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        String postadress = "gdspinae-postadress";
        String postort = "gdspinae-postort";
        String postnummer = "gdspinae-postnummer";
        Patient patientWithIncompleteAddress = buildPatient();
        patientWithIncompleteAddress.setPostadress(postadress);
        patientWithIncompleteAddress.setPostort(postort);
        patientWithIncompleteAddress.setPostnummer(postnummer);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString()))
            .thenReturn(patientWithIncompleteAddress);

        doReturn(buildUtkast("intygtyp", "intygsid")).when(utkastService).getDraft(anyString(), anyString());

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

        setupGrundData();
        setupUser(intygsTyp, false, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG), Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

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
        String intygsTyp = "fk7263";
        String intygsId = "intyg1";
        String draftAsJson = "test";
        byte[] payload = draftAsJson.getBytes();

        setupUser(intygsTyp, false, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG), Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        when(utkastService.saveDraft(intygsId, UTKAST_VERSION, draftAsJson, true))
            .thenReturn(new SaveDraftResponse(UTKAST_VERSION, UtkastStatus.DRAFT_COMPLETE));

        doReturn(buildUtkast("intygtyp", "intygsid")).when(utkastService).getDraft(anyString(), anyString(), anyBoolean());

        Response response = moduleApiController.saveDraft(intygsTyp, intygsId, UTKAST_VERSION, false, payload, request);

        verify(utkastService).saveDraft(intygsId, UTKAST_VERSION, draftAsJson, true);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveDraftOptimisticLockException() {
        String intygsTyp = "fk7263";
        String intygsId = "intyg1";
        String draftAsJson = "test";
        byte[] payload = draftAsJson.getBytes();

        setupUser(intygsTyp, false, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG), Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        when(utkastService.saveDraft(intygsId, UTKAST_VERSION, draftAsJson, true)).thenThrow(new OptimisticLockException(""));
        doReturn(buildUtkast("intygtyp", "intygsid")).when(utkastService).getDraft(anyString(), anyString(), anyBoolean());

        try {
            moduleApiController.saveDraft(intygsTyp, intygsId, UTKAST_VERSION, false, payload, request);
        } finally {
            verify(monitoringLogService).logUtkastConcurrentlyEdited(intygsId, intygsTyp);
        }
    }

    @Test
    public void testValidateDraft() {
        String intygsTyp = "fk7263";
        String intygsId = "intyg1";
        String draftAsJson = "test";
        byte[] payload = draftAsJson.getBytes();

        setupUser(intygsTyp, false, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG), Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        when(utkastService.validateDraft(intygsId, intygsTyp, draftAsJson)).thenReturn(buildDraftValidation());

        doReturn(buildUtkast("intygtyp", "intygsid")).when(utkastService).getDraft(anyString(), anyString(), anyBoolean());

        Response response = moduleApiController.validateDraft(intygsTyp, intygsId, payload);

        verify(utkastService).validateDraft(intygsId, intygsTyp, draftAsJson);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDiscardDraft() {
        String intygsTyp = "fk7263";
        String intygsId = "intyg1";

        setupUser(intygsTyp, false, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG), Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        doReturn(buildUtkast("intygtyp", "intygsid")).when(utkastService).getDraft(anyString(), anyString(), anyBoolean());

        Response response = moduleApiController.discardDraft(intygsTyp, intygsId, UTKAST_VERSION, request);

        verify(utkastService).deleteUnsignedDraft(intygsId, UTKAST_VERSION);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testValidateDraftWithWarningsArePropagatedToCaller() {
        String intygsTyp = "fk7263";
        String intygsId = "intyg1";
        String draftAsJson = "test";
        byte[] payload = draftAsJson.getBytes();

        setupUser(intygsTyp, false, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG), Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        DraftValidation draftValidation = buildDraftValidation();
        draftValidation.addWarning(
            new DraftValidationMessage("category", "field", ValidationMessageType.WARN, "this.is.a.message", "dy.nam.ic.key"));

        when(utkastService.validateDraft(intygsId, intygsTyp, draftAsJson)).thenReturn(draftValidation);

        doReturn(buildUtkast("intygtyp", "intygsid")).when(utkastService).getDraft(anyString(), anyString(), anyBoolean());

        Response response = moduleApiController.validateDraft(intygsTyp, intygsId, payload);

        DraftValidation entity = (DraftValidation) response.getEntity();

        verify(utkastService).validateDraft(intygsId, intygsTyp, draftAsJson);
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(ValidationStatus.VALID, entity.getStatus());
        assertEquals(0, entity.getMessages().size());
        assertEquals(1, entity.getWarnings().size());
    }

    @Test
    public void testCopyUtkast() {
        String intygsId = "intygId";
        String intygsTyp = "fk7263";
        String intygsTypVersion = INTYG_TYPE_VERSION;
        String newIntygsId = "newIntygId";

        setupUser(intygsTyp, false, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG),
            Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        Utkast utkast = new Utkast();
        utkast.setPatientPersonnummer(Personnummer.createPersonnummer("19121212-1212").get());
        when(utkastService.getDraft(eq(intygsId), eq(intygsTyp), eq(Boolean.FALSE))).thenReturn(utkast);

        ArgumentCaptor<CreateUtkastFromTemplateRequest> captor = ArgumentCaptor.forClass(CreateUtkastFromTemplateRequest.class);
        when(copyUtkastService.createUtkastCopy(captor.capture()))
            .thenReturn(new CreateUtkastFromTemplateResponse(intygsTyp, intygsTypVersion, newIntygsId, intygsId));

        Response response = moduleApiController.copyUtkast(intygsTyp, intygsId);

        verify(copyUtkastService).createUtkastCopy(any());
        verifyNoMoreInteractions(copyUtkastService);
        assertEquals(newIntygsId, ((CopyIntygResponse) response.getEntity()).getIntygsUtkastId());
        assertEquals(intygsTyp, ((CopyIntygResponse) response.getEntity()).getIntygsTyp());
    }

    @Test(expected = WebCertServiceException.class)
    public void testCopyUtkastWhenUtkastIsLockedAndAccessIsDenied() {
        String intygsId = "intygId";
        String intygsTyp = "fk7263";

        when(utkastService.getDraft(eq(intygsId), eq(intygsTyp), eq(Boolean.FALSE))).thenReturn(mock(Utkast.class));

        doThrow(
            new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING, "Some error message")
        ).when(lockedDraftAccessServiceHelper).validateAccessToCopy(any(Utkast.class));

        moduleApiController.copyUtkast(intygsTyp, intygsId);

        verify(utkastService).getDraft(eq(intygsId), eq(intygsTyp));
        verify(lockedDraftAccessServiceHelper).validateAccessToCopy(any(Utkast.class));
    }

    @Test
    public void testCopyUtkastKopieraOKFalse() {
        String intygsTyp = "fk7263";
        String intygsId = "intyg1";
        String intygsTypVersion = INTYG_TYPE_VERSION;
        String newIntygId = "newIntygId";

        IntegrationParameters integrationParameters = IntegrationParameters.of("", "", "", "", "", "", "", "", "", false, false, false,
            false);

        setupUser(intygsTyp, integrationParameters, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG), Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        Utkast utkast = new Utkast();
        utkast.setPatientPersonnummer(createPnr("19121212-1212"));
        when(utkastService.getDraft(eq(intygsId), eq(intygsTyp), eq(Boolean.FALSE))).thenReturn(utkast);

        ArgumentCaptor<CreateUtkastFromTemplateRequest> captor = ArgumentCaptor.forClass(CreateUtkastFromTemplateRequest.class);
        when(copyUtkastService.createUtkastCopy(captor.capture()))
            .thenReturn(new CreateUtkastFromTemplateResponse(intygsTyp, intygsTypVersion, newIntygId, intygsId));

        Response response = moduleApiController.copyUtkast(intygsTyp, intygsId);

        verify(copyUtkastService).createUtkastCopy(any());
        verifyNoMoreInteractions(copyUtkastService);
        assertEquals(newIntygId, ((CopyIntygResponse) response.getEntity()).getIntygsUtkastId());
        assertEquals(intygsTyp, ((CopyIntygResponse) response.getEntity()).getIntygsTyp());
    }

    @Test
    public void testRevokeLockedDraft() {
        String intygsTyp = "fk7263";
        String intygsId = "intyg1";

        setupUser(intygsTyp, false, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG), Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        RevokeSignedIntygParameter param = new RevokeSignedIntygParameter();

        doReturn(buildUtkast("intygtyp", "intygsid")).when(utkastService).getDraft(anyString(), anyString(), anyBoolean());

        Response response = moduleApiController.revokeLockedDraft(intygsTyp, intygsId, param);

        verify(utkastService).revokeLockedDraft(intygsId, intygsTyp, param.getMessage(), param.getReason());
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void verifyCandidateMetaDataWhenCandidateFound() throws Exception {
        String intygsId = CERTIFICATE_ID;
        String intygsTyp = "ag7804";
        String intygsIdCandidate = "candidate-intyg-id";
        String intygsTypCandidate = "lisjp";
        String intygsTypVersionCandidate = "1.0";

        setupGrundData();
        setupUser(intygsTyp, false, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG), Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        when(certificateRelationService.getRelations(eq(intygsId)))
            .thenReturn(new Relations());
        when(utkastService.getDraft(intygsId, intygsTyp))
            .thenReturn(buildUtkast(intygsTyp, intygsId, 0L, UtkastStatus.DRAFT_INCOMPLETE));
        when(utkastCandidateService.getCandidateMetaData(any(ModuleApi.class), anyString(), anyString(), any(Patient.class), anyBoolean()))
            .thenReturn(Optional.of(createCandidateMetaData(intygsIdCandidate, intygsTypCandidate, INTYG_TYPE_VERSION)));

        Response response = moduleApiController.getDraft(intygsTyp, intygsId, request);

        verify(utkastService).getDraft(intygsId, intygsTyp);
        verify(utkastCandidateService).getCandidateMetaData(any(ModuleApi.class), anyString(), anyString(), any(Patient.class),
            anyBoolean());

        DraftHolder draftHolder = (DraftHolder) response.getEntity();
        assertEquals(intygsIdCandidate, draftHolder.getCandidateMetaData().getIntygId());
        assertEquals(intygsTypCandidate, draftHolder.getCandidateMetaData().getIntygType());
        assertEquals(INTYG_TYPE_VERSION, draftHolder.getCandidateMetaData().getIntygTypeVersion());
    }

    @Test
    public void verifyCandidateMetaDataNotRequestedWhenHavingRelations() throws Exception {
        String intygsId = CERTIFICATE_ID;
        String intygsTyp = "ag7804";

        setupGrundData();
        setupUser(intygsTyp, false, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG), Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        when(certificateRelationService.getRelations(eq(intygsId)))
            .thenReturn(new Relations());
        final Utkast utkast = buildUtkast(intygsTyp, intygsId, 0L, UtkastStatus.DRAFT_INCOMPLETE);
        utkast.setRelationKod(RelationKod.FRLANG);
        when(utkastService.getDraft(intygsId, intygsTyp))
            .thenReturn(utkast);

        Response response = moduleApiController.getDraft(intygsTyp, intygsId, request);

        verify(utkastService).getDraft(intygsId, intygsTyp);

        DraftHolder draftHolder = (DraftHolder) response.getEntity();
        assertNull(draftHolder.getCandidateMetaData());
    }

    @Test
    public void verifyCandidateMetaDataWhenCandidateNotFound() throws Exception {
        String intygsId = CERTIFICATE_ID;
        String intygsTyp = "ag7804";
        String intygsTypVersion = "1.0";

        setupGrundData();
        setupUser(intygsTyp, false, UserOriginType.NORMAL.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG), Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        when(certificateRelationService.getRelations(eq(intygsId)))
            .thenReturn(new Relations());
        when(utkastService.getDraft(intygsId, intygsTyp))
            .thenReturn(buildUtkast(intygsTyp, intygsId, 0L, UtkastStatus.DRAFT_INCOMPLETE));
        when(utkastCandidateService.getCandidateMetaData(any(ModuleApi.class), anyString(), anyString(), any(Patient.class), anyBoolean()))
            .thenReturn(Optional.ofNullable(null));

        Response response = moduleApiController.getDraft(intygsTyp, intygsId, request);

        verify(utkastService).getDraft(intygsId, intygsTyp);
        verify(utkastCandidateService).getCandidateMetaData(any(ModuleApi.class), anyString(), anyString(), any(Patient.class),
            anyBoolean());

        DraftHolder draftHolder = (DraftHolder) response.getEntity();
        assertNull(draftHolder.getCandidateMetaData());
    }

    @Test
    public void updateUtkastWithDataFromCandidate() throws Exception {
        String intygsId = CERTIFICATE_ID;
        String intygsTyp = "ag7804";
        String intygsIdCandidate = "candidate-intyg-id";
        String intygsTypCandidate = "lisjp";

        setupGrundData();
        setupUser(intygsTyp, false, UserOriginType.DJUPINTEGRATION.name(),
            Arrays.asList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, AuthoritiesConstants.PRIVILEGE_COPY_FROM_CANDIDATE),
            Arrays.asList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        when(utkastService.getDraft(anyString(), anyString(), anyBoolean())).thenReturn(mock(Utkast.class));
        when(utkastService.updateDraftFromCandidate(anyString(), anyString(), any(Utkast.class)))
            .thenReturn(new SaveDraftResponse(1L, UtkastStatus.DRAFT_INCOMPLETE));

        CopyFromCandidateRequest request = new CopyFromCandidateRequest();
        request.setCandidateId(intygsIdCandidate);
        request.setCandidateType(intygsTypCandidate);

        Response response = moduleApiController.copyFromCandidate(intygsTyp, intygsId, request);

        verify(utkastService).updateDraftFromCandidate(anyString(), anyString(), any(Utkast.class));

        SaveDraftResponse saveDraftResponse = (SaveDraftResponse) response.getEntity();
        assertNotNull(saveDraftResponse);
        assertTrue(saveDraftResponse.getVersion() > 0);
        assertEquals(UtkastStatus.DRAFT_INCOMPLETE, saveDraftResponse.getStatus());
    }

    @Test
    public void shouldSetIsLatestMajorTextVersion() throws IOException, ModuleException {
        final var certificateType = "lisjp";
        setupGrundData();
        setupUser(certificateType, false, UserOriginType.NORMAL.name(),
            Collections.singletonList(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG),
            Collections.singletonList(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST));

        when(utkastService.getDraft(CERTIFICATE_ID, certificateType)).thenReturn(buildUtkast(certificateType, CERTIFICATE_ID));
        when(certificateRelationService.getRelations(eq(CERTIFICATE_ID))).thenReturn(new Relations());
        when(intygTextsService.isLatestMajorVersion(any(String.class), any(String.class))).thenReturn(false);

        final var response = moduleApiController.getDraft(certificateType, CERTIFICATE_ID, request);

        assertFalse(response.readEntity(DraftHolder.class).isLatestMajorTextVersion());
    }

    private UtkastCandidateMetaData createCandidateMetaData(String intygId, String intygType, String intygTypeVersion) {
        return new UtkastCandidateMetaData.Builder()
            .with(builder -> {
                builder.intygId = intygId;
                builder.intygType = intygType;
                builder.intygTypeVersion = intygTypeVersion;
            })
            .create();
    }

    private DraftValidation buildDraftValidation() {
        DraftValidation validation = new DraftValidation();
        validation.setMessages(new ArrayList<>());
        validation.setStatus(ValidationStatus.VALID);
        return validation;
    }

    private Utkast buildUtkast(String intygType, String intygId) {
        return buildUtkast(intygType, intygId, UTKAST_VERSION, UTKAST_STATUS);
    }

    private Utkast buildUtkast(String intygType, String intygId, long utkastVersion, UtkastStatus utkastStatus) {
        Utkast utkast = new Utkast();
        utkast.setVersion(utkastVersion);
        utkast.setVidarebefordrad(false);
        utkast.setStatus(utkastStatus);
        utkast.setEnhetsId(UTKAST_ENHETSID);
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
        return buildPatient("Tolvan", "Tolvansson", createPnr(UTKAST_PERSONNUMMER));
    }

    private Patient buildPatient(String fornamn, String efternamn, Personnummer personnummer) {
        Patient patient = new Patient();
        patient.setFornamn("Tolvan");
        patient.setEfternamn("Tolvansson");
        patient.setPersonId(personnummer);
        return patient;
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer: " + pnr));
    }

    private void setupGrundData() throws ModuleException, IOException {
        final Utlatande utlatande = mock(Utlatande.class);
        doReturn(utlatande).when(moduleApi).getUtlatandeFromJson(any());
        final GrundData grundData = mock(GrundData.class);
        doReturn(grundData).when(utlatande).getGrundData();
        final HoSPersonal skapadAv = mock(HoSPersonal.class);
        doReturn(skapadAv).when(grundData).getSkapadAv();
    }

    private void setupUser(String intygType, boolean coherentJournaling, String origin, List<String> privileges, List<String> features) {
        IntegrationParameters integrationParameters = new IntegrationParameters("", "", "", "", "", "", "", "", "", coherentJournaling,
            false, false, true, null);
        setupUser(intygType, integrationParameters, origin, privileges, features);
    }

    private void setupUser(String intygType, IntegrationParameters integrationParameters,
        String origin, List<String> privileges, List<String> features) {

        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());
        user.setFeatures(features.stream().collect(Collectors.toMap(Function.identity(), s -> {
            Feature feature = new Feature();
            feature.setName(s);
            feature.setIntygstyper(Arrays.asList(intygType));
            feature.setGlobal(true);
            return feature;
        })));
        user.setParameters(integrationParameters);

        Privilege privilege = new Privilege();
        privilege.setIntygstyper(Arrays.asList(intygType));
        privileges.forEach(s -> user.getAuthorities().put(s, privilege));

        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName("NORMAL");
        requestOrigin.setIntygstyper(privilege.getIntygstyper());

        privilege.setRequestOrigins(Arrays.asList(requestOrigin));

        user.setOrigin(requestOrigin.getName());
        user.setAuthenticationMethod(AuthenticationMethod.FAKE);

        this.webCertUser = user;

        when(webcertUserService.getUser()).thenReturn(webCertUser);
    }

}
