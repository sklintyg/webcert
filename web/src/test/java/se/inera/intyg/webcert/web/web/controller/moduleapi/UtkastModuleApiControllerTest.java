package se.inera.intyg.webcert.web.web.controller.moduleapi;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.security.authorities.AuthoritiesException;
import se.inera.intyg.common.security.common.model.*;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.relation.RelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.*;

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
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.getDraft(CERTIFICATE_ID)).thenReturn(buildUtkast(intygTyp, intygId));
        when(relationService.getRelations(eq(intygId))).thenReturn(Optional.empty());

        Response response = moduleApiController.getDraft(intygTyp, CERTIFICATE_ID, request);
        verify(utkastService).getDraft(CERTIFICATE_ID);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = AuthoritiesException.class)
    public void getDraftWithoutPrivilegeSkrivaIntygFails() {
        String intygType = "fk7263";
        setupUser("", intygType, WebcertFeature.HANTERA_INTYGSUTKAST);
        moduleApiController.getDraft(intygType, CERTIFICATE_ID, request);
    }

    @Test
    public void testSaveDraft() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        byte[] payload = "test".getBytes();
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.saveAndValidateDraft(any(SaveAndValidateDraftRequest.class), any(boolean.class))).thenReturn(buildSaveAndValidateDraftResponse());

        Response response = moduleApiController.saveDraft(intygTyp, intygId, UTKAST_VERSION, false, payload, request);

        verify(utkastService).saveAndValidateDraft(any(SaveAndValidateDraftRequest.class), eq(true));
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = AuthoritiesException.class)
    public void saveDraftWithoutPrivilegeSkrivaIntygFails() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        byte[] payload = "test".getBytes();
        setupUser("", intygTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        moduleApiController.saveDraft(intygTyp, intygId, UTKAST_VERSION, false, payload, request);
    }

    @Test
    public void testDiscardDraft() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        Response response = moduleApiController.discardDraft(intygTyp, intygId, UTKAST_VERSION, request);

        verify(utkastService).deleteUnsignedDraft(intygId, UTKAST_VERSION);
        assertEquals(OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = AuthoritiesException.class)
    public void discardDraftWithoutPrivilegeSkrivaIntygFails() {
        String intygTyp = "fk7263";
        String intygId = "intyg1";
        setupUser("", intygTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        moduleApiController.discardDraft(intygTyp, intygId, UTKAST_VERSION, request);
    }

    private SaveAndValidateDraftResponse buildSaveAndValidateDraftResponse() {
        DraftValidation validation = new DraftValidation();
        validation.setMessages(new ArrayList<DraftValidationMessage>());
        validation.setStatus(DraftValidationStatus.VALID);
        SaveAndValidateDraftResponse response = new SaveAndValidateDraftResponse(UTKAST_VERSION, validation);
        return response;
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
        when(webcertUserService.getUser()).thenReturn(user);
    }
}
