/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;

@RunWith(MockitoJUnitRunner.class)
public class UtkastApiControllerTest {

    private static final String PATIENT_EFTERNAMN = "Tolvansson";

    private static final String PATIENT_FORNAMN = "Tolvan";

    private static final String PATIENT_MELLANNAMN = "Von";

    private static final Personnummer PATIENT_PERSONNUMMER = new Personnummer("19121212-1212");

    private static final String PATIENT_POSTADRESS = "Testadress";

    private static final String PATIENT_POSTNUMMER = "12345";

    private static final String PATIENT_POSTORT = "Testort";

    @Mock
    private UtkastService utkastService;

    @Mock
    private WebCertUserService webcertUserService;

    @InjectMocks
    private UtkastApiController utkastController;

    @Before
    public void setup() throws Exception {
    }

    @Test
    public void testCreateUtkast() throws JsonParseException, JsonMappingException, IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(Mockito.any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        Response response = utkastController.createUtkast(intygsTyp, buildRequest("fk7263"));
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateUtkastSetsPatientFullName() throws JsonParseException, JsonMappingException, IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(Mockito.any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        Response response = utkastController.createUtkast(intygsTyp, buildRequest("fk7263"));
        assertEquals(OK.getStatusCode(), response.getStatus());

        ArgumentCaptor<CreateNewDraftRequest> requestCaptor = ArgumentCaptor.forClass(CreateNewDraftRequest.class);
        verify(utkastService).createNewDraft(requestCaptor.capture());
        assertNotNull(requestCaptor.getValue().getPatient().getFullstandigtNamn());
        assertEquals(PATIENT_FORNAMN + " " + PATIENT_MELLANNAMN + " " + PATIENT_EFTERNAMN,
                requestCaptor.getValue().getPatient().getFullstandigtNamn());
    }

    @Test
    public void testCreateUtkastSetsPatientFullNameWithoutMiddlename() throws JsonParseException, JsonMappingException, IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(Mockito.any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

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
    public void testCreateUtkastFornamnOk() throws JsonParseException, JsonMappingException, IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(Mockito.any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        CreateUtkastRequest utkastRequest = buildRequest(intygsTyp);
        utkastRequest.setPatientFornamn(StringUtils.repeat("a", 255));
        Response response = utkastController.createUtkast(intygsTyp, utkastRequest);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateUtkastFornamnTooLong() throws JsonParseException, JsonMappingException, IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(Mockito.any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        CreateUtkastRequest utkastRequest = buildRequest(intygsTyp);
        utkastRequest.setPatientFornamn(StringUtils.repeat("a", 256));
        Response response = utkastController.createUtkast(intygsTyp, utkastRequest);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateUtkastEfternamnOk() throws JsonParseException, JsonMappingException, IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(Mockito.any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        CreateUtkastRequest utkastRequest = buildRequest(intygsTyp);
        utkastRequest.setPatientEfternamn(StringUtils.repeat("a", 255));
        Response response = utkastController.createUtkast(intygsTyp, utkastRequest);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateUtkastEfternamnTooLong() throws JsonParseException, JsonMappingException, IOException {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(Mockito.any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        CreateUtkastRequest utkastRequest = buildRequest(intygsTyp);
        utkastRequest.setPatientEfternamn(StringUtils.repeat("a", 256));
        Response response = utkastController.createUtkast(intygsTyp, utkastRequest);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test(expected = AuthoritiesException.class)
    public void createUtkastWithoutPrivilegeSkrivIntygFails() {
        String intygsTyp = "fk7263";
        setupUser("", intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);
        utkastController.createUtkast(intygsTyp, buildRequest("fk7263"));
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
