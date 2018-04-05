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

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.common.model.WebcertFeature;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
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

    @Mock
    private IntegrationService integrationService;

    @InjectMocks
    private IntygIntegrationController testee;

    @Test
    public void invalidParametersShouldNotFailOnNullPatientInfo() {

        testee.setCommonFeatureService(Optional.empty());
        // given
        UriInfo uriInfo = mock(UriInfo.class);
        UriBuilder uriBuilder = mock(UriBuilder.class);
        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.replacePath(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.fragment(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.buildFromMap(any())).thenReturn(URI.create(""));

        when(integrationService.prepareRedirectToIntyg(anyString(), anyString(), anyObject()))
                .thenReturn(createPrepareRedirectToIntyg());

        IntegrationParameters parameters = new IntegrationParameters(null, null, ALTERNATE_SSN, null, null, null, null, null, null, false,
                false, false, false);

        WebCertUser user = createDefaultUser();
        user.setParameters(parameters);

        Response res = testee.handleRedirectToIntyg(uriInfo, INTYGSTYP, INTYGSID, ENHETSID, user);

        assertEquals(Response.Status.TEMPORARY_REDIRECT.getStatusCode(), res.getStatus());

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

    private WebCertUser createUser(String roleName, Privilege p, Set<String> features, String origin) {
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
                ImmutableSet.of(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), WebcertFeature.HANTERA_INTYGSUTKAST.getName() + ".lisjp",
                        "base_feature"), UserOriginType.DJUPINTEGRATION.name());
    }

}
