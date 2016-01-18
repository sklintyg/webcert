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

package se.inera.intyg.webcert.web.auth.authorities.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesException;
import se.inera.intyg.webcert.web.auth.authorities.Privilege;
import se.inera.intyg.webcert.web.auth.authorities.RequestOrigin;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import com.google.common.collect.ImmutableSet;

/**
 * Created by marced on 14/12/15.
 */
public class AuthoritiesValidatorTest {

    protected AuthoritiesValidator validator = new AuthoritiesValidator();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testMustHaveFeature() {
        WebCertUser user = createDefaultUser();
        user.setFeatures(ImmutableSet.of(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), WebcertFeature.HANTERA_INTYGSUTKAST.getName() + ".fk7263",
                WebcertFeature.HANTERA_FRAGOR.getName(), WebcertFeature.HANTERA_FRAGOR.getName() + ".fk7263"));

        assertTrue(validator.given(user).
                features(WebcertFeature.HANTERA_INTYGSUTKAST).
                features(WebcertFeature.HANTERA_FRAGOR).
                notFeatures(WebcertFeature.KOPIERA_INTYG).
                isVerified());
    }

    @Test
    public void testMustHaveSomeFeature() {
        WebCertUser user = createDefaultUser();
        user.setFeatures(ImmutableSet.of(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), WebcertFeature.HANTERA_INTYGSUTKAST.getName() + ".fk7263",
                WebcertFeature.HANTERA_FRAGOR.getName(), WebcertFeature.HANTERA_FRAGOR.getName() + ".fk7263"));

        assertTrue(validator.given(user).
                features(WebcertFeature.HANTERA_INTYGSUTKAST, WebcertFeature.ARBETSGIVARUTSKRIFT).
                isVerified());
    }

    @Test
    public void testMustNotHaveAnyFeature() {
        WebCertUser user = createDefaultUser();
        user.setFeatures(ImmutableSet.of(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), WebcertFeature.HANTERA_INTYGSUTKAST.getName() + ".fk7263",
                WebcertFeature.HANTERA_FRAGOR.getName(), WebcertFeature.HANTERA_FRAGOR.getName() + ".fk7263"));

        assertTrue(validator.given(user).
                notFeatures(WebcertFeature.ARBETSGIVARUTSKRIFT, WebcertFeature.KOPIERA_INTYG).
                isVerified());
    }

    @Test
    public void testMustNotHaveAnyFeatureFails() {
        WebCertUser user = createDefaultUser();
        user.setFeatures(ImmutableSet.of(
                WebcertFeature.HANTERA_INTYGSUTKAST.getName(), WebcertFeature.HANTERA_INTYGSUTKAST.getName() + ".fk7263",
                WebcertFeature.HANTERA_FRAGOR.getName(), WebcertFeature.HANTERA_FRAGOR.getName() + ".fk7263"));

        assertFalse(validator.given(user, "fk7263").
                notFeatures(WebcertFeature.HANTERA_INTYGSUTKAST, WebcertFeature.KOPIERA_INTYG).isVerified());

        thrown.expect(AuthoritiesException.class);

        validator.given(user, "fk7263").
                notFeatures(WebcertFeature.HANTERA_INTYGSUTKAST, WebcertFeature.KOPIERA_INTYG).orThrow();
    }

    @Test
    public void testMustHaveFeatureIntygstyp() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user, "fk7263").
                features(WebcertFeature.HANTERA_INTYGSUTKAST).
                notFeatures(WebcertFeature.ARBETSGIVARUTSKRIFT).
                isVerified());
    }

    @Test
    public void testMustHaveFeatureFail() {
        WebCertUser user = createDefaultUser();



        assertFalse(validator.given(user, "fk7263").
                features(WebcertFeature.ARBETSGIVARUTSKRIFT).isVerified());

        thrown.expect(AuthoritiesException.class);

        validator.given(user, "fk7263").
                features(WebcertFeature.ARBETSGIVARUTSKRIFT).orThrow();
    }

    @Test
    public void testMustNotHaveFeatureFail() {
        WebCertUser user = createDefaultUser();

        assertFalse(validator.given(user, "fk7263").
                notFeatures(WebcertFeature.HANTERA_INTYGSUTKAST).isVerified());

        thrown.expect(AuthoritiesException.class);

        validator.given(user, "fk7263").
                notFeatures(WebcertFeature.HANTERA_INTYGSUTKAST).orThrow();
    }

    @Test
    public void testMusthaveOrigin() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user).
                origins(WebCertUserOriginType.NORMAL).isVerified());
    }

    @Test
    public void testMustHaveOriginFail() {
        WebCertUser user = createDefaultUser();

        thrown.expect(AuthoritiesException.class);

        validator.given(user).
                origins(WebCertUserOriginType.DJUPINTEGRATION).orThrow();
    }

    @Test
    public void testMustNotHaveOrigin() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user).
                notOrigins(WebCertUserOriginType.DJUPINTEGRATION).isVerified());
    }

    @Test
    public void testMustNotHaveOriginFail() {
        WebCertUser user = createDefaultUser();

        thrown.expect(AuthoritiesException.class);

        validator.given(user).
                notOrigins(WebCertUserOriginType.NORMAL).orThrow();
    }

    @Test
    public void testMustHavePrevilege() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user).
                privilege("p1").isVerified());
        validator.given(user).
                privilege("p1").orThrow();
    }

    @Test
    public void testMustHavePrevilegeFails() {
        WebCertUser user = createDefaultUser();

        thrown.expect(AuthoritiesException.class);

        validator.given(user).
                privilege("p2").orThrow();
    }

    @Test
    public void testMustNotHavePrevilegeFails() {
        WebCertUser user = createDefaultUser();

        thrown.expect(AuthoritiesException.class);

        validator.given(user).
                notPrivilege("p1").orThrow();
    }

    @Test
    public void testMustHavePrevilegeIntygsTyp() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user, "fk7263").
                privilege("p1").isVerified());
    }

    @Test
    public void testMustHavePrevilegeIntygsTypFails() {
        WebCertUser user = createDefaultUser();

        thrown.expect(AuthoritiesException.class);

        validator.given(user, "ts-diabetes").
                privilege("p1").orThrow();
    }

    @Test
    public void testMustHavePrevilegeIntygsTypFailsOnMissingRequestOrigin() {
        WebCertUser user = createDefaultUser();
        user.setOrigin(WebCertUserOriginType.UTHOPP.name());

        thrown.expect(AuthoritiesException.class);

        validator.given(user, "fk7263").
                privilege("p1").orThrow();
    }

    @Test
    public void testMustHavePrevilegeIntygsTypFailsOnMissingRequestOriginIntygsTyp() {
        WebCertUser user = createDefaultUser();
        user.setOrigin(WebCertUserOriginType.DJUPINTEGRATION.name());

        thrown.expect(AuthoritiesException.class);

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

    @Test
    public void testMustHaveRoleFails() {
        WebCertUser user = createDefaultUser();

        thrown.expect(AuthoritiesException.class);

        validator.given(user).
                roles(AuthoritiesConstants.ROLE_ADMIN).orThrow();
    }

    @Test
    public void testMustNotHaveRole() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user).
                notRoles(AuthoritiesConstants.ROLE_ADMIN).isVerified());
    }

    @Test
    public void testMustNotHaveRoleFails() {
        WebCertUser user = createDefaultUser();

        thrown.expect(AuthoritiesException.class);

        validator.given(user).
                notRoles(AuthoritiesConstants.ROLE_LAKARE).orThrow();
    }

    @Test
    public void testAllTogether() {
        WebCertUser user = createDefaultUser();

        assertTrue(validator.given(user, "fk7263").
                features(WebcertFeature.HANTERA_INTYGSUTKAST).
                notFeatures(WebcertFeature.ARBETSGIVARUTSKRIFT).
                roles(AuthoritiesConstants.ROLE_LAKARE).
                notRoles("dummy_role").
                origins(WebCertUserOriginType.NORMAL).
                notOrigins(WebCertUserOriginType.DJUPINTEGRATION).
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
        return user;
    }

    private WebCertUser createDefaultUser() {
        return createUser(AuthoritiesConstants.ROLE_LAKARE,
                createPrivilege("p1",
                        Arrays.asList("fk7263", "ts-bas"), // p1 is restricted to these intygstyper
                        Arrays.asList(
                                createRequestOrigin(WebCertUserOriginType.NORMAL.name(), Arrays.asList("fk7263")), // Normal
                                                                                                                   // restricted
                                                                                                                   // to
                                                                                                                   // fk7263
                                createRequestOrigin(WebCertUserOriginType.DJUPINTEGRATION.name(), Arrays.asList("ts-bas")))),
                ImmutableSet.of(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), WebcertFeature.HANTERA_INTYGSUTKAST.getName() + ".fk7263",
                        "base_feature"),// feature_a is active for
                                                                                 // intygscontext fk7263, base_feature
                                                                                 // is not.
                WebCertUserOriginType.NORMAL.name());
    }

}
