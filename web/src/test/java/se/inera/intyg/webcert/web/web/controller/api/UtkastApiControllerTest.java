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
package se.inera.intyg.webcert.web.web.controller.api;

import com.google.common.base.Strings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertFeature;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygResponse;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtkastApiControllerTest {

    private static final String PATIENT_EFTERNAMN = "Tolvansson";

    private static final String PATIENT_FORNAMN = "Tolvan";

    private static final String PATIENT_MELLANNAMN = "Von";

    private static final Personnummer PATIENT_PERSONNUMMER = new Personnummer("19121212-1212");
    private static final Personnummer PATIENT_PERSONNUMMER_PU_SEKRETESS = new Personnummer("20121212-1212");

    private static final String PATIENT_POSTADRESS = "Testadress";

    private static final String PATIENT_POSTNUMMER = "12345";

    private static final String PATIENT_POSTORT = "Testort";

    @Mock
    private UtkastService utkastService;

    @Mock
    private WebCertUserService webcertUserService;

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private WebcertFeatureService featureService;

    @InjectMocks
    private UtkastApiController utkastController;

    @Before
    public void setup() throws Exception {
        when(patientDetailsResolver.getSekretessStatus(eq(PATIENT_PERSONNUMMER))).thenReturn(SekretessStatus.FALSE);
        when(patientDetailsResolver.getSekretessStatus(eq(PATIENT_PERSONNUMMER_PU_SEKRETESS))).thenReturn(SekretessStatus.TRUE);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString())).thenReturn(buildPatient());
        Map<String, Boolean> hasPrevious = new HashMap<>();
        hasPrevious.put("fk7263", true);
        when(utkastService.checkIfPersonHasExistingIntyg(eq(PATIENT_PERSONNUMMER))).thenReturn(hasPrevious);

    }

    @Test
    public void testCreateUtkast() throws IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        Response response = utkastController.createUtkast(intygsTyp, buildRequest("fk7263"));
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateUtkastSetsPatientFullName() throws IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        Response response = utkastController.createUtkast(intygsTyp, buildRequest("fk7263"));
        assertEquals(OK.getStatusCode(), response.getStatus());

        ArgumentCaptor<CreateNewDraftRequest> requestCaptor = ArgumentCaptor.forClass(CreateNewDraftRequest.class);
        verify(utkastService).createNewDraft(requestCaptor.capture());
        assertNotNull(requestCaptor.getValue().getPatient().getFullstandigtNamn());
        assertEquals(PATIENT_FORNAMN + " " + PATIENT_MELLANNAMN + " " + PATIENT_EFTERNAMN,
                requestCaptor.getValue().getPatient().getFullstandigtNamn());
    }

    @Test
    public void testCreateUtkastSetsPatientFullNameWithoutMiddlename() throws IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        // Fake PU service being down
        when(patientDetailsResolver.resolvePatient(PATIENT_PERSONNUMMER, intygsTyp)).thenReturn(null);

        CreateUtkastRequest utkastRequest = buildRequest("fk7263");
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
    public void testCreateUtkastFornamnOk() throws IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        CreateUtkastRequest utkastRequest = buildRequest(intygsTyp);
        utkastRequest.setPatientFornamn(Strings.repeat("a", 255));
        Response response = utkastController.createUtkast(intygsTyp, utkastRequest);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateUtkastFornamnTooLong() throws IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        CreateUtkastRequest utkastRequest = buildRequest(intygsTyp);
        utkastRequest.setPatientFornamn(Strings.repeat("a", 256));
        Response response = utkastController.createUtkast(intygsTyp, utkastRequest);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateUtkastEfternamnOk() throws IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        CreateUtkastRequest utkastRequest = buildRequest(intygsTyp);
        utkastRequest.setPatientEfternamn(Strings.repeat("a", 255));
        Response response = utkastController.createUtkast(intygsTyp, utkastRequest);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateUtkastEfternamnTooLong() throws IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        CreateUtkastRequest utkastRequest = buildRequest(intygsTyp);
        utkastRequest.setPatientEfternamn(Strings.repeat("a", 256));
        Response response = utkastController.createUtkast(intygsTyp, utkastRequest);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test(expected = AuthoritiesException.class)
    public void createUtkastWithoutPrivilegeSkrivIntygFails() {
        String intygsTyp = "fk7263";
        setupUser("", intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);
        utkastController.createUtkast(intygsTyp, buildRequest("fk7263"));
    }

    @Test
    public void testFilterDraftsForUnit() {
        setupUser(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT, Fk7263EntryPoint.MODULE_ID,
                WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.filterIntyg(any()))
                .thenReturn(Arrays.asList(buildUtkast(PATIENT_PERSONNUMMER), buildUtkast(PATIENT_PERSONNUMMER)));

        final Response response = utkastController.filterDraftsForUnit(buildQueryIntygParameter());
        final QueryIntygResponse queryIntygResponse = response.readEntity(QueryIntygResponse.class);
        assertEquals(2, queryIntygResponse.getTotalCount());
        assertEquals(2, queryIntygResponse.getResults().size());

    }

    @Test
    public void testFilterDraftsForUnitSkipsSekretessIntygForUserWithoutAuthorithy() {
        setupUser("", Fk7263EntryPoint.MODULE_ID, WebcertFeature.HANTERA_INTYGSUTKAST);

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
        setupUser(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT, Fk7263EntryPoint.MODULE_ID,
                WebcertFeature.HANTERA_INTYGSUTKAST);

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.UNDEFINED);

        when(utkastService.filterIntyg(any()))
                .thenReturn(Arrays.asList(buildUtkast(PATIENT_PERSONNUMMER), buildUtkast(PATIENT_PERSONNUMMER)));

        final Response response = utkastController.filterDraftsForUnit(buildQueryIntygParameter());
        final QueryIntygResponse queryIntygResponse = response.readEntity(QueryIntygResponse.class);
        assertEquals(0, queryIntygResponse.getTotalCount());
        assertEquals(0, queryIntygResponse.getResults().size());

    }

    @Test
    public void testGetPrevious() {
        setupUser("", "fk7263");

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
        Map<String, Boolean> responseBody = (Map<String, Boolean>) response.readEntity(HashMap.class);
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.get("fk7263"));
    }

    private QueryIntygParameter buildQueryIntygParameter() {
        QueryIntygParameter queryIntygParameter = new QueryIntygParameter();
        queryIntygParameter.setPageSize(10);
        queryIntygParameter.setStartFrom(0);
        return queryIntygParameter;

    }

    private void setupUser(String privilegeString, String intygType, WebcertFeature... features) {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());
        user.setFeatures(Stream.of(features).map(WebcertFeature::getName).collect(Collectors.toSet()));
        user.getFeatures().addAll(Stream.of(features).map(f -> f.getName() + "." + intygType).collect(Collectors.toSet()));
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
        Utkast utkast = new Utkast();
        utkast.setIntygsTyp("fk7263");
        utkast.setVardgivarId("456");
        utkast.setStatus(UtkastStatus.DRAFT_COMPLETE);
        utkast.setSenastSparadAv(new VardpersonReferens());
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
}
