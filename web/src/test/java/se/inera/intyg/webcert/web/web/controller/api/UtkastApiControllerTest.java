package se.inera.intyg.webcert.web.web.controller.api;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.common.integration.hsa.model.Vardenhet;
import se.inera.intyg.common.integration.hsa.model.Vardgivare;
import se.inera.intyg.common.security.authorities.AuthoritiesException;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.security.common.model.Privilege;
import se.inera.intyg.common.security.common.model.RequestOrigin;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
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
    public void testCreateUtkast() {
        String intygsTyp = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG, intygsTyp, WebcertFeature.HANTERA_INTYGSUTKAST);

        when(utkastService.createNewDraft(Mockito.any(CreateNewDraftRequest.class))).thenReturn(new Utkast());

        Response response = utkastController.createUtkast(intygsTyp, buildRequest("fk7263"));
        assertEquals(OK.getStatusCode(), response.getStatus());
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
