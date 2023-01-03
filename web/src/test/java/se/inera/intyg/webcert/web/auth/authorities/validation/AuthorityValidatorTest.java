/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth.authorities.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

;

/**
 * Created by marced on 14/12/15.
 */
public class AuthorityValidatorTest {

    protected AuthoritiesValidator validator = new AuthoritiesValidator();

    @Test
    public void testMustHaveFeature() {
        WebCertUser user = createDefaultUser();

        user.setFeatures(Stream.of(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, AuthoritiesConstants.FEATURE_HANTERA_FRAGOR).collect(
            Collectors.toMap(Function.identity(), s -> {
                Feature feature = new Feature();
                feature.setName(s);
                feature.setIntygstyper(Collections.singletonList("fk7263"));
                feature.setGlobal(true);
                return feature;
            })));
        assertTrue(validator.given(user, "fk7263").
            features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST).
            features(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR).
            notFeatures(AuthoritiesConstants.FEATURE_FORNYA_INTYG).
            isVerified());
    }

    @Test
    public void testMustHaveSomeFeature() {
        WebCertUser user = createDefaultUser();

        user.setFeatures(Stream.of(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, AuthoritiesConstants.FEATURE_HANTERA_FRAGOR).collect(
            Collectors.toMap(Function.identity(), s -> {
                Feature feature = new Feature();
                feature.setName(s);
                feature.setIntygstyper(Collections.singletonList("fk7263"));
                feature.setGlobal(true);
                return feature;
            })));

        assertTrue(validator.given(user, "fk7263")
            .features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT)
            .isVerified());
    }

    @Test
    public void testMustNotHaveAnyFeature() {
        WebCertUser user = createDefaultUser();

        user.setFeatures(Stream.of(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, AuthoritiesConstants.FEATURE_HANTERA_FRAGOR).collect(
            Collectors.toMap(Function.identity(), s -> {
                Feature feature = new Feature();
                feature.setName(s);
                feature.setIntygstyper(Collections.singletonList("fk7263"));
                feature.setGlobal(true);
                return feature;
            })));

        assertTrue(validator.given(user).
            notFeatures(AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT, AuthoritiesConstants.FEATURE_FORNYA_INTYG).
            isVerified());
    }

    @Test(expected = AuthoritiesException.class)
    public void testMustNotHaveAnyFeatureFails() {
        WebCertUser user = createDefaultUser();

        user.setFeatures(Stream.of(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, AuthoritiesConstants.FEATURE_HANTERA_FRAGOR).collect(
            Collectors.toMap(Function.identity(), s -> {
                Feature feature = new Feature();
                feature.setName(s);
                feature.setIntygstyper(Collections.singletonList("fk7263"));
                feature.setGlobal(true);
                return feature;
            })));
        assertFalse(validator.given(user, "fk7263").
            notFeatures(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, AuthoritiesConstants.FEATURE_FORNYA_INTYG).isVerified());

        validator.given(user, "fk7263").
            notFeatures(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, AuthoritiesConstants.FEATURE_FORNYA_INTYG).orThrow();
    }

    @Test
    public void testMustHaveFeatureIntygstyp() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user, "fk7263").
            features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST).
            notFeatures(AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT).
            isVerified());
    }

    @Test(expected = AuthoritiesException.class)
    public void testGlobalFalseShouldFailEvenIfAllowedIntygstyp() {
        WebCertUser user = createDefaultUser();
        for (Map.Entry<String, Feature> e : user.getFeatures().entrySet()) {
            e.getValue().setGlobal(false);
        }

        assertFalse(validator.given(user, "fk7263").features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
            .notFeatures(AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT).isVerified());

        validator.given(user, "fk7263").features(AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT).orThrow();
    }

    @Test(expected = AuthoritiesException.class)
    public void testMustHaveFeatureFail() {
        WebCertUser user = createDefaultUser();

        assertFalse(validator.given(user, "fk7263").
            features(AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT).isVerified());

        validator.given(user, "fk7263").
            features(AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT).orThrow();
    }

    @Test(expected = AuthoritiesException.class)
    public void testMustNotHaveFeatureFail() {
        WebCertUser user = createDefaultUser();

        assertFalse(validator.given(user, "fk7263").
            notFeatures(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST).isVerified());

        validator.given(user, "fk7263").
            notFeatures(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST).orThrow();
    }

    @Test
    public void testMusthaveOrigin() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user).
            origins(UserOriginType.NORMAL).isVerified());
    }

    @Test(expected = AuthoritiesException.class)
    public void testMustHaveOriginFail() {
        WebCertUser user = createDefaultUser();

        validator.given(user).
            origins(UserOriginType.DJUPINTEGRATION).orThrow();
    }

    @Test
    public void testMustNotHaveOrigin() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user).
            notOrigins(UserOriginType.DJUPINTEGRATION).isVerified());
    }

    @Test(expected = AuthoritiesException.class)
    public void testMustNotHaveOriginFail() {
        WebCertUser user = createDefaultUser();

        validator.given(user).
            notOrigins(UserOriginType.NORMAL).orThrow();
    }

    @Test
    public void testMustHavePrevilege() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user).
            privilege("p1").isVerified());
        validator.given(user).
            privilege("p1").orThrow();
    }

    @Test(expected = AuthoritiesException.class)
    public void testMustHavePrevilegeFails() {
        WebCertUser user = createDefaultUser();

        validator.given(user).
            privilege("p2").orThrow();
    }

    @Test(expected = AuthoritiesException.class)
    public void testMustNotHavePrevilegeFails() {
        WebCertUser user = createDefaultUser();

        validator.given(user).
            notPrivilege("p1").orThrow();
    }

    @Test
    public void testMustHavePrevilegeIntygsTyp() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user, "fk7263").
            privilege("p1").isVerified());
    }

    @Test(expected = AuthoritiesException.class)
    public void testMustHavePrevilegeIntygsTypFails() {
        WebCertUser user = createDefaultUser();

        validator.given(user, "ts-diabetes").
            privilege("p1").orThrow();
    }

    @Test(expected = AuthoritiesException.class)
    public void testMustHavePrevilegeIntygsTypFailsOnMissingRequestOrigin() {
        WebCertUser user = createDefaultUser();
        user.setOrigin(UserOriginType.UTHOPP.name());

        validator.given(user, "fk7263").
            privilege("p1").orThrow();
    }

    @Test(expected = AuthoritiesException.class)
    public void testMustHavePrevilegeIntygsTypFailsOnMissingRequestOriginIntygsTyp() {
        WebCertUser user = createDefaultUser();
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        validator.given(user, "fk7263").
            privilege("p1").orThrow();
    }

    @Test
    public void testMustNotHavePrevilegeIntygsTyp() {
        WebCertUser user = createDefaultUser();

        validator.given(user, "fk7263").
            notPrivilege("p3");
    }

    @Test
    public void testMustHavePrevilegeForRequestOrigin() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user).
            privilege("p1").isVerified());
    }

    @Test
    public void testMustHaveRole() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user).
            roles(AuthoritiesConstants.ROLE_LAKARE).isVerified());
    }

    @Test
    public void testMustHaveAnyOfRole() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user).
            roles(AuthoritiesConstants.ROLE_LAKARE, AuthoritiesConstants.ROLE_ADMIN).isVerified());
    }

    @Test
    public void testNotMustHaveAnyOfRole() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user).
            notRoles(AuthoritiesConstants.ROLE_TANDLAKARE, AuthoritiesConstants.ROLE_ADMIN).isVerified());
    }

    @Test(expected = AuthoritiesException.class)
    public void testMustHaveRoleFails() {
        WebCertUser user = createDefaultUser();

        validator.given(user).
            roles(AuthoritiesConstants.ROLE_ADMIN).orThrow();
    }

    @Test
    public void testMustNotHaveRole() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user).
            notRoles(AuthoritiesConstants.ROLE_ADMIN).isVerified());
    }

    @Test(expected = AuthoritiesException.class)
    public void testMustNotHaveRoleFails() {
        WebCertUser user = createDefaultUser();

        validator.given(user).
            notRoles(AuthoritiesConstants.ROLE_LAKARE).orThrow();
    }

    @Test
    public void testAllTogether() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user, "fk7263").
            features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST).
            notFeatures(AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT).
            roles(AuthoritiesConstants.ROLE_LAKARE).
            notRoles("dummy_role").
            origins(UserOriginType.NORMAL).
            notOrigins(UserOriginType.DJUPINTEGRATION).
            privilege("p1").
            notPrivilege("dummy_privilege").
            isVerified());
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
        return user;
    }

    private WebCertUser createDefaultUser() {
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
            createPrivilege("p1",
                Arrays.asList("fk7263", "ts-bas"), // p1 is restricted to these intygstyper
                Arrays.asList(
                    createRequestOrigin(UserOriginType.NORMAL.name(), Arrays.asList("fk7263")),
                    // Normal restricted to fk7263
                    createRequestOrigin(UserOriginType.DJUPINTEGRATION.name(), Arrays.asList("ts-bas")))),
            featureMap,
            // feature_a is active for
            // intygscontext fk7263, base_feature
            // is not.
            UserOriginType.NORMAL.name());
    }

}
