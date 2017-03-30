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
package se.inera.intyg.webcert.web.web.controller.moduleapi;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessageType;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.relation.RelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveDraftResponse;

@RunWith(MockitoJUnitRunner.class)
public class UtkastModuleApiControllerTest {

    private static final String CERTIFICATE_ID = "123";

    private static final long UTKAST_VERSION = 1;

    private static final UtkastStatus UTKAST_STATUS = UtkastStatus.DRAFT_COMPLETE;

    private static final String UTKAST_ENHETSNAMN = "Enhetsnamn";

    private static final String UTKAST_VARDGIVARNAMN = "Vardgivarnamn";

    private static final String UTKAST_MODEL = "<Model>";

    private HttpServletRequest request;
    private HttpSession session;

    @Mock
    private IntygTextsService intygTextsService;

    @Mock
    private RelationService relationService;

    @Mock
    private UtkastService utkastService;

    @Mock
    private WebCertUserService webcertUserService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private UtkastModuleApiController moduleApiController = new UtkastModuleApiController();

    @Before
    public void setup() {
        session = mock(HttpSession.class);
        request = mock(HttpServletRequest.class);
        Mockito.doNothing().when(session).removeAttribute("lastSavedDraft");
        when(request.getSession(true)).thenReturn(session);
    }

    @Test
    public void testGetDraft() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, false, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.getDraft(CERTIFICATE_ID, intygTyp, false)).thenReturn(buildUtkast(intygTyp, intygId));
        when(relationService.getRelations(eq(intygId))).thenReturn(Optional.empty());

        Response response = moduleApiController.getDraft(intygTyp, CERTIFICATE_ID, request);
        verify(utkastService).getDraft(CERTIFICATE_ID, intygTyp, false);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetDraftWithCoherentJournaling() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, true, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.getDraft(CERTIFICATE_ID, intygTyp, true)).thenReturn(buildUtkast(intygTyp, intygId));
        when(relationService.getRelations(eq(intygId))).thenReturn(Optional.empty());

        Response response = moduleApiController.getDraft(intygTyp, CERTIFICATE_ID, request);
        verify(utkastService).getDraft(CERTIFICATE_ID, intygTyp, true);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = AuthoritiesException.class)
    public void getDraftWithoutPrivilegeSkrivaIntygFails() {
        String intygType = "fk7263";
        setupUser("", intygType, false, WebcertFeature.HANTERA_INTYGSUTKAST);
        moduleApiController.getDraft(intygType, CERTIFICATE_ID, request);
    }

    @Test
    public void testSaveDraft() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        String draftAsJson = "test";
        byte[] payload = draftAsJson.getBytes();
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, false, WebcertFeature.HANTERA_INTYGSUTKAST);

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
        setupUser("", intygTyp, false, WebcertFeature.HANTERA_INTYGSUTKAST);

        moduleApiController.saveDraft(intygTyp, intygId, UTKAST_VERSION, false, payload, request);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveDraftOptimisticLockException() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        String draftAsJson = "test";
        byte[] payload = draftAsJson.getBytes();
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, false, WebcertFeature.HANTERA_INTYGSUTKAST);

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
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, false, WebcertFeature.HANTERA_INTYGSUTKAST);

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
        setupUser("", intygTyp, false, WebcertFeature.HANTERA_INTYGSUTKAST);

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
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, false, WebcertFeature.HANTERA_INTYGSUTKAST);

        Response response = moduleApiController.discardDraft(intygTyp, intygId, UTKAST_VERSION, request);

        verify(utkastService).deleteUnsignedDraft(intygId, UTKAST_VERSION);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = AuthoritiesException.class)
    public void discardDraftWithoutPrivilegeSkrivaIntygFails() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        setupUser("", intygTyp, false, WebcertFeature.HANTERA_INTYGSUTKAST);

        moduleApiController.discardDraft(intygTyp, intygId, UTKAST_VERSION, request);
    }

    @Test
    public void testValidateDraftWithWarningsArePropagatedToCaller() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        String draftAsJson = "test";
        byte[] payload = draftAsJson.getBytes();
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, false, WebcertFeature.HANTERA_INTYGSUTKAST);
        DraftValidation draftValidation = buildDraftValidation();
        draftValidation.addWarning(new DraftValidationMessage("field", ValidationMessageType.WARN, "this.is.a.message", "dy.nam.ic.key"));

        when(utkastService.validateDraft(intygId, intygTyp, draftAsJson)).thenReturn(draftValidation);

        Response response = moduleApiController.validateDraft(intygTyp, intygId, payload);

        DraftValidation entity = (DraftValidation) response.getEntity();

        verify(utkastService).validateDraft(intygId, intygTyp, draftAsJson);
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(ValidationStatus.VALID, entity.getStatus());
        assertEquals(0, entity.getMessages().size());
        assertEquals(1, entity.getWarnings().size());
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
        utkast.setIntygsId(intygId);
        return utkast;
    }

    private void setupUser(String privilegeString, String intygType, boolean coherentJournaling, WebcertFeature... features) {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());
        user.setFeatures(Stream.of(features).map(WebcertFeature::getName).collect(Collectors.toSet()));
        user.getFeatures().addAll(Stream.of(features).map(f -> f.getName() + "." + intygType).collect(Collectors.toSet()));
        user.setParameters(new IntegrationParameters("", "", "", "", "", "", "", "", "", coherentJournaling, false, false, true));
        Privilege privilege = new Privilege();
        privilege.setIntygstyper(Arrays.asList(intygType));
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName("NORMAL");
        requestOrigin.setIntygstyper(privilege.getIntygstyper());
        privilege.setRequestOrigins(Arrays.asList(requestOrigin));
        user.getAuthorities().put(privilegeString, privilege);
        user.setOrigin("NORMAL");
        when(webcertUserService.getUser()).thenReturn(user);
    }
}
