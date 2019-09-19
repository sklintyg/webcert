/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.base.Strings;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.integration.hsa.services.HsaEmployeeService;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.access.AccessResult;
import se.inera.intyg.webcert.web.service.access.DraftAccessService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygResponse;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;
import se.riv.infrastructure.directory.v1.PersonInformationType;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UtkastApiControllerTest {

    private static final String PATIENT_EFTERNAMN = "Tolvansson";
    private static final String PATIENT_FORNAMN = "Tolvan";
    private static final String PATIENT_MELLANNAMN = "Von";
    private static final String PATIENT_POSTADRESS = "Testadress";
    private static final String PATIENT_POSTNUMMER = "12345";
    private static final String PATIENT_POSTORT = "Testort";

    private static final Personnummer PATIENT_PERSONNUMMER = createPnr("19121212-1212");
    private static final Personnummer PATIENT_PERSONNUMMER_PU_SEKRETESS = createPnr("20121212-1212");
    private static final java.lang.String INTYG_TYPE_VERSION = "1.2";

    @Mock
    private UtkastService utkastService;

    @Mock
    private WebCertUserService webcertUserService;

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private IntygTextsService intygTextsService;

    @Mock
    private DraftAccessService draftAccessService;

    @Mock
    private AccessResultExceptionHelper accessResultExceptionHelper;

    @Mock
    private HsaEmployeeService hsaEmployeeService;

    @InjectMocks
    private UtkastApiController utkastController;

    @Before
    public void setup() throws ModuleNotFoundException {
        when(patientDetailsResolver.getSekretessStatus(eq(PATIENT_PERSONNUMMER))).thenReturn(SekretessStatus.FALSE);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString())).thenReturn(buildPatient());
        when(moduleRegistry.getIntygModule(eq(LuseEntryPoint.MODULE_ID)))
                .thenReturn(new IntygModule("luse", "", "", "", "", "", "", "", "", false));
        when(moduleRegistry.getIntygModule(eq(Fk7263EntryPoint.MODULE_ID)))
                .thenReturn(new IntygModule("fk7263", "", "", "", "", "", "", "", "", true));

        Map<String, Map<String, PreviousIntyg>> hasPrevious = new HashMap<>();
        Map<String, PreviousIntyg> hasPreviousIntyg = new HashMap<>();
        hasPreviousIntyg.put("luse", PreviousIntyg.of(true, false, "Enhet", "intygsId", null));
        hasPrevious.put("intyg", hasPreviousIntyg);
        when(utkastService.checkIfPersonHasExistingIntyg(eq(PATIENT_PERSONNUMMER), any())).thenReturn(hasPrevious);
        when(intygTextsService.getLatestVersion(any(String.class))).thenReturn(INTYG_TYPE_VERSION);

        // Return hsaId as name
        when(hsaEmployeeService.getEmployee(anyString(), any())).thenAnswer(invocation -> {
            PersonInformationType personInformation = new PersonInformationType();
            personInformation.setMiddleAndSurName((String) invocation.getArguments()[0]);

            List<PersonInformationType> personInformationTypeList = new ArrayList<>();
            personInformationTypeList.add(personInformation);
            return personInformationTypeList;
        });
    }

    @Test
    public void testCreateUtkastFailsForDeprecated() {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        Response response = utkastController.createUtkast(intygsTyp, buildRequest("fk7263"));
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateUtkast() {
        String intygsTyp = "luse";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(new Utkast());
        doReturn(AccessResult.noProblem()).when(draftAccessService).allowToCreateDraft(any(), any());

        Response response = utkastController.createUtkast(intygsTyp, buildRequest("luse"));
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateUtkastSetsPatientFullName() {
        String intygsTyp = "luse";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        doReturn(AccessResult.noProblem()).when(draftAccessService).allowToCreateDraft(any(), any());
        when(utkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        Response response = utkastController.createUtkast(intygsTyp, buildRequest("luse"));
        assertEquals(OK.getStatusCode(), response.getStatus());

        ArgumentCaptor<CreateNewDraftRequest> requestCaptor = ArgumentCaptor.forClass(CreateNewDraftRequest.class);
        verify(utkastService).createNewDraft(requestCaptor.capture());
        assertNotNull(requestCaptor.getValue().getPatient().getFullstandigtNamn());
        assertEquals(PATIENT_FORNAMN + " " + PATIENT_MELLANNAMN + " " + PATIENT_EFTERNAMN,
                requestCaptor.getValue().getPatient().getFullstandigtNamn());
    }

    @Test
    public void testCreateUtkastSetsPatientFullNameWithoutMiddlename() {
        String intygsTyp = "luse";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        // Fake PU service being down
        when(patientDetailsResolver.resolvePatient(PATIENT_PERSONNUMMER, intygsTyp, INTYG_TYPE_VERSION)).thenReturn(null);
        doReturn(AccessResult.noProblem()).when(draftAccessService).allowToCreateDraft(any(), any());
        CreateUtkastRequest utkastRequest = buildRequest("luse");
        utkastRequest.setPatientMellannamn(null); // no middlename
        Response response = utkastController.createUtkast(intygsTyp, utkastRequest);
        assertEquals(OK.getStatusCode(), response.getStatus());

        ArgumentCaptor<CreateNewDraftRequest> requestCaptor = ArgumentCaptor.forClass(CreateNewDraftRequest.class);
        verify(utkastService).createNewDraft(requestCaptor.capture());
        assertNotNull(requestCaptor.getValue().getPatient().getFullstandigtNamn());
        assertEquals(PATIENT_FORNAMN + " " + PATIENT_EFTERNAMN,
                requestCaptor.getValue().getPatient().getFullstandigtNamn());
    }

    @Test
    public void testCreateUtkastFornamnOk() {
        String intygsTyp = "luse";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(new Utkast());
        doReturn(AccessResult.noProblem()).when(draftAccessService).allowToCreateDraft(any(), any());
        CreateUtkastRequest utkastRequest = buildRequest(intygsTyp);
        utkastRequest.setPatientFornamn(Strings.repeat("a", 255));
        Response response = utkastController.createUtkast(intygsTyp, utkastRequest);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateUtkastFornamnTooLong() {
        String intygsTyp = "luse";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        CreateUtkastRequest utkastRequest = buildRequest(intygsTyp);
        utkastRequest.setPatientFornamn(Strings.repeat("a", 256));
        Response response = utkastController.createUtkast(intygsTyp, utkastRequest);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateUtkastEfternamnOk() {
        String intygsTyp = "luse";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(new Utkast());
        doReturn(AccessResult.noProblem()).when(draftAccessService).allowToCreateDraft(any(), any());

        CreateUtkastRequest utkastRequest = buildRequest(intygsTyp);
        utkastRequest.setPatientEfternamn(Strings.repeat("a", 255));
        Response response = utkastController.createUtkast(intygsTyp, utkastRequest);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateUtkastEfternamnTooLong() {
        String intygsTyp = "luse";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        CreateUtkastRequest utkastRequest = buildRequest(intygsTyp);
        utkastRequest.setPatientEfternamn(Strings.repeat("a", 256));
        Response response = utkastController.createUtkast(intygsTyp, utkastRequest);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testFilterDraftsForUnit() {
        setupUser(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT, LuseEntryPoint.MODULE_ID,
                AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        when(utkastService.filterIntyg(any()))
                .thenReturn(Arrays.asList(buildUtkast(PATIENT_PERSONNUMMER), buildUtkast(PATIENT_PERSONNUMMER)));

        final Response response = utkastController.filterDraftsForUnit(buildQueryIntygParameter());
        final QueryIntygResponse queryIntygResponse = response.readEntity(QueryIntygResponse.class);
        assertEquals(2, queryIntygResponse.getTotalCount());
        assertEquals(2, queryIntygResponse.getResults().size());

    }

    @Test
    public void testFilterDraftsForUnitHsaNotFound() {
        String hsaIdNotFound = "notFoundHsa";
        String nameNotFound = "notFoundName";
        String hsaIdFound = "hsaIdFound";

        setupUser(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT, LuseEntryPoint.MODULE_ID,
            AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        when(hsaEmployeeService.getEmployee(eq(hsaIdNotFound), any())).thenThrow(WebServiceException.class);

        Utkast utkast2 = buildUtkast(PATIENT_PERSONNUMMER, new VardpersonReferens(hsaIdFound, "nameFoundAndReplaced"));
        utkast2.setSenastSparadDatum(LocalDateTime.now().plusDays(1));

        when(utkastService.filterIntyg(any()))
            .thenReturn(Arrays.asList(
                buildUtkast(PATIENT_PERSONNUMMER, new VardpersonReferens(hsaIdNotFound, nameNotFound)),
                utkast2));

        final Response response = utkastController.filterDraftsForUnit(buildQueryIntygParameter());
        final QueryIntygResponse queryIntygResponse = response.readEntity(QueryIntygResponse.class);
        assertEquals(2, queryIntygResponse.getTotalCount());
        assertEquals(2, queryIntygResponse.getResults().size());

        assertEquals(hsaIdFound, queryIntygResponse.getResults().get(0).getUpdatedSignedById());
        assertEquals(hsaIdFound, queryIntygResponse.getResults().get(0).getUpdatedSignedBy());

        assertEquals(hsaIdNotFound, queryIntygResponse.getResults().get(1).getUpdatedSignedById());
        assertEquals(nameNotFound, queryIntygResponse.getResults().get(1).getUpdatedSignedBy());
    }

    @Test
    public void testFilterDraftsForUnitSkipsSekretessIntygForUserWithoutAuthorithy() {
        setupUser("", LuseEntryPoint.MODULE_ID, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        Map<Personnummer, SekretessStatus> sekretessMap = mock(Map.class);
        when(sekretessMap.get(eq(PATIENT_PERSONNUMMER))).thenReturn(SekretessStatus.FALSE);
        when(sekretessMap.get(eq(PATIENT_PERSONNUMMER_PU_SEKRETESS))).thenReturn(SekretessStatus.TRUE);
        when(patientDetailsResolver.getSekretessStatusForList(anyList())).thenReturn(sekretessMap);

        when(utkastService.filterIntyg(any()))
                .thenReturn(Arrays.asList(buildUtkast(PATIENT_PERSONNUMMER), buildUtkast(PATIENT_PERSONNUMMER_PU_SEKRETESS)));

        final Response response = utkastController.filterDraftsForUnit(buildQueryIntygParameter());
        final QueryIntygResponse queryIntygResponse = response.readEntity(QueryIntygResponse.class);
        assertEquals(1, queryIntygResponse.getTotalCount());
        assertEquals(1, queryIntygResponse.getResults().size());
        assertEquals(PATIENT_PERSONNUMMER, queryIntygResponse.getResults().get(0).getPatientId());

    }

    @Test
    public void testFilterDraftsForUnitSkipAllIntygWithUndefinedSekretessStatus() {
        setupUser(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT, LuseEntryPoint.MODULE_ID,
                AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);

        Map<Personnummer, SekretessStatus> sekretessMap = mock(Map.class);
        when(sekretessMap.get(any())).thenReturn(SekretessStatus.UNDEFINED);
        when(patientDetailsResolver.getSekretessStatusForList(anyList())).thenReturn(sekretessMap);

        when(utkastService.filterIntyg(any()))
                .thenReturn(Arrays.asList(buildUtkast(PATIENT_PERSONNUMMER), buildUtkast(PATIENT_PERSONNUMMER)));

        final Response response = utkastController.filterDraftsForUnit(buildQueryIntygParameter());
        final QueryIntygResponse queryIntygResponse = response.readEntity(QueryIntygResponse.class);
        assertEquals(0, queryIntygResponse.getTotalCount());
        assertEquals(0, queryIntygResponse.getResults().size());

    }

    @Test
    public void testGetPrevious() {
        setupUser("", "luse");

        Utkast utkast1 = buildUtkast(PATIENT_PERSONNUMMER);
        utkast1.setStatus(UtkastStatus.SIGNED);
        Utkast utkast2 = buildUtkast(PATIENT_PERSONNUMMER);
        utkast2.setStatus(UtkastStatus.SIGNED);
        Utkast utkast3 = buildUtkast(PATIENT_PERSONNUMMER);
        utkast3.setIntygsTyp("Fel typ");
        utkast3.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        Utkast utkast4 = buildUtkast(PATIENT_PERSONNUMMER);
        utkast4.setIntygsTyp("Fel typ 2");
        utkast4.setStatus(UtkastStatus.SIGNED);
        utkast4.setAterkalladDatum(LocalDateTime.of(2017, 1, 1, 1, 1));

        Response response = utkastController.getPreviousCertificateWarnings(PATIENT_PERSONNUMMER.getPersonnummer());

        assertNotNull(response);
        Map<String, Map<String, PreviousIntyg>> responseBody = (Map<String, Map<String, PreviousIntyg>>) response.readEntity(HashMap.class);
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.get("intyg").get("luse").isSameVardgivare());
    }

    private QueryIntygParameter buildQueryIntygParameter() {
        QueryIntygParameter queryIntygParameter = new QueryIntygParameter();
        queryIntygParameter.setPageSize(10);
        queryIntygParameter.setStartFrom(0);
        return queryIntygParameter;

    }

    private void setupUser(String privilegeString, String intygType, String... features) {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());
        user.getFeatures().putAll(Stream.of(features).collect(Collectors.toMap(Function.identity(), s -> {
            Feature feature = new Feature();
            feature.setName(s);
            feature.setIntygstyper(Arrays.asList(intygType));
            feature.setGlobal(true);
            return feature;
        })));
        Privilege privilege = new Privilege();
        privilege.setIntygstyper(Arrays.asList(intygType));
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName("NORMAL");
        requestOrigin.setIntygstyper(privilege.getIntygstyper());
        privilege.setRequestOrigins(Arrays.asList(requestOrigin));
        user.getAuthorities().put(privilegeString, privilege);
        user.setOrigin("NORMAL");

        user.setValdVardenhet(buildVardenhet());
        user.setValdVardgivare(buildVardgivare());
        when(webcertUserService.getUser()).thenReturn(user);
    }

    private CreateUtkastRequest buildRequest(String typ) {
        CreateUtkastRequest request = new CreateUtkastRequest();
        request.setIntygType(typ);
        request.setPatientEfternamn(PATIENT_EFTERNAMN);
        request.setPatientFornamn(PATIENT_FORNAMN);
        request.setPatientMellannamn(PATIENT_MELLANNAMN);
        request.setPatientPersonnummer(PATIENT_PERSONNUMMER);
        request.setPatientPostadress(PATIENT_POSTADRESS);
        request.setPatientPostnummer(PATIENT_POSTNUMMER);
        request.setPatientPostort(PATIENT_POSTORT);
        return request;
    }

    private Utkast buildUtkast(Personnummer personnr) {
        return buildUtkast(personnr, new VardpersonReferens("hsa1", "name"));
    }

    private Utkast buildUtkast(Personnummer personnr, VardpersonReferens vardperson) {
        Utkast utkast = new Utkast();
        utkast.setIntygsTyp("luse");
        utkast.setVardgivarId("456");
        utkast.setStatus(UtkastStatus.DRAFT_COMPLETE);
        utkast.setSenastSparadAv(vardperson);
        utkast.setSenastSparadDatum(LocalDateTime.now());
        utkast.setPatientPersonnummer(personnr);

        return utkast;
    }

    private Patient buildPatient() {
        Patient patient = new Patient();
        patient.setPersonId(PATIENT_PERSONNUMMER);
        patient.setEfternamn(PATIENT_EFTERNAMN);
        patient.setFornamn(PATIENT_FORNAMN);
        patient.setMellannamn(PATIENT_MELLANNAMN);
        patient.setFullstandigtNamn(PATIENT_FORNAMN + " " + PATIENT_MELLANNAMN + " " + PATIENT_EFTERNAMN);

        patient.setPostadress(PATIENT_POSTADRESS);
        patient.setPostnummer(PATIENT_POSTNUMMER);
        patient.setPostort(PATIENT_POSTORT);

        return patient;
    }

    private SelectableVardenhet buildVardgivare() {
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setId("456");
        vardgivare.setNamn("vardgivarnamn");
        return vardgivare;
    }

    private SelectableVardenhet buildVardenhet() {
        Vardenhet enhet = new Vardenhet();
        enhet.setId("123");
        enhet.setNamn("Enhetsnamn");
        enhet.setEpost("test@test.com");
        enhet.setTelefonnummer("12345");
        enhet.setPostadress("Enhetsadress");
        enhet.setPostnummer("12345");
        enhet.setPostort("Enhetsort");
        enhet.setArbetsplatskod("000000");
        return enhet;
    }

    private static Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer: " + personId));
    }

}
