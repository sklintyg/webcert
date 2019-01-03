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
package se.inera.intyg.webcert.web.service.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class WcAuthorityAsserterTest {

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private WcAuthorityAsserter authorityAsserter;

    @Test
    public void assertIsAuthorizedOK() {

        WebCertUser webCertUser = createDefaultUser(AuthoritiesConstants.PRIVILEGE_SIGNERA_INTYG);

        doReturn(webCertUser)
                .when(webCertUserService).getUser();

        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));

        authorityAsserter.assertIsAuthorized(
                Personnummer.createPersonnummer("191212121212").get(),
                AuthoritiesConstants.PRIVILEGE_SIGNERA_INTYG);

    }

    @Test
    public void assertIsAuthorizedNOK() {

        WebCertUser webCertUser = createDefaultUser(AuthoritiesConstants.PRIVILEGE_ERSATTA_INTYG); //Not correct privilegie

        doReturn(webCertUser)
                .when(webCertUserService).getUser();

        assertThatThrownBy(() -> authorityAsserter.assertIsAuthorized(
                Personnummer.createPersonnummer("191212121212").get(),
                AuthoritiesConstants.PRIVILEGE_SIGNERA_INTYG)).isExactlyInstanceOf(WebCertServiceException.class);

    }

    private WebCertUser createDefaultUser(final String privilegie) {
        Map<String, Feature> featureMap = new HashMap<>();

        Feature feature1 = new Feature();
        feature1.setName(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        feature1.setIntygstyper(Collections.singletonList("fk7263"));
        feature1.setGlobal(true);
        featureMap.put(feature1.getName(), feature1);

        Feature feature2 = new Feature();
        feature2.setName("base_feature");
        feature2.setIntygstyper(Collections.emptyList());
        feature2.setGlobal(true);
        featureMap.put(feature2.getName(), feature2);

        return createUser(AuthoritiesConstants.ROLE_LAKARE,
                createPrivilege(privilegie,
                        Collections.emptyList(),
                        Lists.newArrayList(
                                createRequestOrigin(UserOriginType.NORMAL.name(), Arrays.asList("fk7263")),
                                createRequestOrigin(UserOriginType.DJUPINTEGRATION.name(), Arrays.asList("ts-bas")))),
                featureMap,
                UserOriginType.NORMAL.name());
    }

    private WebCertUser createUser(String roleName, Privilege p, Map<String, Feature> features, String origin) {
        WebCertUser user = new WebCertUser();

        HashMap<String, Privilege> privilegeHashMap = new HashMap<>();
        privilegeHashMap.put(p.getName(), p);
        user.setAuthorities(privilegeHashMap);

        user.setOrigin(origin);
        user.setFeatures(features);

        HashMap<String, Role> roleHashMap = new HashMap<>();
        Role role = new Role();
        role.setName(roleName);
        roleHashMap.put(roleName, role);

        user.setRoles(roleHashMap);
        return user;
    }

    private RequestOrigin createRequestOrigin(String name, List<String> intygstyper) {
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName(name);
        requestOrigin.setIntygstyper(intygstyper);
        return requestOrigin;
    }

    private Privilege createPrivilege(String name, List<String> intygsTyper, List<RequestOrigin> requestOrigins) {
        Privilege privilege = new Privilege();
        privilege.setName(name);
        privilege.setIntygstyper(intygsTyper);
        privilege.setRequestOrigins(requestOrigins);
        return privilege;
    }

}
