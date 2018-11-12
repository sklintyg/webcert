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
package se.inera.intyg.webcert.web.web.controller.integration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.web.controller.integration.dto.PrepareRedirectToIntyg;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Magnus Ekstrand on 2017-10-25.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntygIntegrationControllerTest {

    private final String ALTERNATE_SSN = "19010101-0101";
    private final String INTYGSTYP = "lisjp";
    private final String INTYGSID = "A1234-B5678-C90123-D4567";
    private final String ENHETSID = "11111";
    private UriInfo uriInfo;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private IntegrationService integrationService;

    @Mock
    private CommonAuthoritiesResolver authoritiesResolver;

    @Mock
    private ReferensService referensService;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @InjectMocks
    private IntygIntegrationController testee;

    @Before
    public void setup() {
        uriInfo = mock(UriInfo.class);
        UriBuilder uriBuilder = mock(UriBuilder.class);
        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.replacePath(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.fragment(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.buildFromMap(any())).thenReturn(URI.create(""));

        when(integrationService.prepareRedirectToIntyg(anyString(), anyString(), any()))
                .thenReturn(createPrepareRedirectToIntyg());
        when(authoritiesResolver.getFeatures(any())).thenReturn(new HashMap<>());

        testee.setUrlBaseTemplate("baseTemplate");
        testee.setUrlIntygFragmentTemplate("intygTemplate");
        testee.setUrlUtkastFragmentTemplate("utkastTemplate");
    }

    @Test
    public void referenceGetsPersistedCorrectly() {
        when(referensService.referensExists(eq(INTYGSID))).thenReturn(false);

        String ref = "referens";
        IntegrationParameters parameters = new IntegrationParameters(ref, null, ALTERNATE_SSN, null, null, null, null,
                null, null, false, false, false, false);

        WebCertUser user = createDefaultUser();
        user.setParameters(parameters);

        Response res = testee.handleRedirectToIntyg(uriInfo, INTYGSTYP, INTYGSID, ENHETSID, user);

        assertEquals(Response.Status.TEMPORARY_REDIRECT.getStatusCode(), res.getStatus());

        verify(referensService).saveReferens(eq(INTYGSID), eq(ref));
    }

    @Test
    public void referenceNotPersistedIfNotSupplied() {
        IntegrationParameters parameters = new IntegrationParameters(null, null, ALTERNATE_SSN, null, null, null, null,
                null, null, false, false, false, false);

        WebCertUser user = createDefaultUser();
        user.setParameters(parameters);

        Response res = testee.handleRedirectToIntyg(uriInfo, INTYGSTYP, INTYGSID, ENHETSID, user);

        assertEquals(Response.Status.TEMPORARY_REDIRECT.getStatusCode(), res.getStatus());

        verify(referensService, times(0)).saveReferens(eq(INTYGSID), or(isNull(), any()));
    }

    @Test
    public void invalidParametersShouldNotFailOnNullPatientInfo() {
        IntegrationParameters parameters = new IntegrationParameters(null, null, ALTERNATE_SSN, null, null, null, null, null, null, false,
                false, false, false);

        WebCertUser user = createDefaultUser();
        user.setParameters(parameters);

        Response res = testee.handleRedirectToIntyg(uriInfo, INTYGSTYP, INTYGSID, ENHETSID, user);

        assertEquals(Response.Status.TEMPORARY_REDIRECT.getStatusCode(), res.getStatus());
        verify(authoritiesResolver).getFeatures(Arrays.asList(user.getValdVardenhet().getId(), user.getValdVardgivare().getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntygsTypLookupFailGet() {
        String intygTyp = "nonExistant";
        when(moduleRegistry.getModuleIdFromExternalId(intygTyp.toUpperCase())).thenReturn("");
        testee.getRedirectToIntyg(null, intygTyp, "intygId", null, null, null, null, null, null, null, null, null, null, false, false,
                false, true);
    }

    @Test(expected = IllegalStateException.class)
    public void testSavedRequestGETHandlerRequiresIntegrationParameters() {
        WebCertUser user = mock(WebCertUser.class);
        when(user.getParameters()).thenReturn(null);
        when(webCertUserService.getUser()).thenReturn(user);

        testee.getRedirectToIntyg(null, INTYGSID, null);
    }

    private PrepareRedirectToIntyg createPrepareRedirectToIntyg() {
        PrepareRedirectToIntyg redirect = new PrepareRedirectToIntyg();
        redirect.setIntygId(INTYGSID);
        redirect.setIntygTyp(INTYGSTYP);
        redirect.setUtkast(true);
        return redirect;
    }

    private RequestOrigin createRequestOrigin(String name, List<String> intygstyper) {
        RequestOrigin o = new RequestOrigin();
        o.setName(name);
        o.setIntygstyper(intygstyper);
        return o;
    }

    private Privilege createPrivilege(String name, List<String> intygsTyper, List<RequestOrigin> requestOrigins) {
        Privilege p = new Privilege();
        p.setName(name);
        p.setIntygstyper(intygsTyper);
        p.setRequestOrigins(requestOrigins);
        return p;
    }

    private WebCertUser createUser(String roleName, Privilege p, Map<String, Feature> features, String origin) {
        WebCertUser user = new WebCertUser();

        HashMap<String, Privilege> pMap = new HashMap<>();
        pMap.put(p.getName(), p);
        user.setAuthorities(pMap);

        user.setOrigin(origin);
        user.setFeatures(features);

        HashMap<String, Role> rMap = new HashMap<>();
        Role role = new Role();
        role.setName(roleName);
        rMap.put(roleName, role);

        user.setRoles(rMap);

        Vardgivare vg = new Vardgivare();
        vg.setId("vg1");
        Vardenhet ve = new Vardenhet();
        ve.setVardgivareHsaId("vg1");
        ve.setId(ENHETSID);
        vg.setVardenheter(Arrays.asList(ve));
        user.setVardgivare(Arrays.asList(vg));
        return user;
    }

    private WebCertUser createDefaultUser() {
        return createUser(AuthoritiesConstants.ROLE_LAKARE,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG,
                        Arrays.asList("lisjp", "ts-bas"), // p1 is restricted to these intygstyper
                        Arrays.asList(
                                createRequestOrigin(UserOriginType.DJUPINTEGRATION.name(), Arrays.asList("lisjp")),
                                createRequestOrigin(UserOriginType.DJUPINTEGRATION.name(), Arrays.asList("ts-bas")))),
                Stream.of(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, "base_feature")
                        .collect(Collectors.toMap(Function.identity(), s -> {
                            Feature feature = new Feature();
                            feature.setName(s);
                            feature.setIntygstyper(Arrays.asList("lisjp"));
                            return feature;
                        })),
                UserOriginType.DJUPINTEGRATION.name());
    }

}
