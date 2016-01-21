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

package se.inera.intyg.webcert.web.auth.authorities;

import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.ROLE_ADMIN;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.ROLE_LAKARE;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.ROLE_TANDLAKARE;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.TITLECODE_AT_LAKARE;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.TITLE_LAKARE;
import static se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants.TITLE_TANDLAKARE;

import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import se.inera.intyg.common.integration.hsa.services.HsaPersonService;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationLoader;
import se.inera.intyg.webcert.web.auth.exceptions.HsaServiceException;
import se.inera.intyg.webcert.web.security.SakerhetstjanstAssertion;
import se.riv.infrastructure.directory.v1.PersonInformationType;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Magnus Ekstrand on 20/11/15.
 */
@Service
public class AuthoritiesResolver {

    private static final Logger LOG = LoggerFactory.getLogger(AuthoritiesResolver.class);


    @Autowired
    private AuthoritiesConfigurationLoader configurationLoader;

    @Autowired
    private HsaPersonService hsaPersonService;


    // ~ API
    // ======================================================================================

    public Role resolveRole(SAMLCredential credential, HttpServletRequest request) {
        Assert.notNull(credential, "Argument 'credential' cannot be null");
        Assert.notNull(request, "Argument 'request' cannot be null");

        SakerhetstjanstAssertion sa = getAssertion(credential.getAuthenticationAssertion());
        List<PersonInformationType> personInfo = getPersonInfo(sa.getHsaId());

        Role role = lookupUserRole(sa, personInfo);
        return role;
    }

    /**
     * Get all configured (known/loaded) intygstyper.
     * @return a list with intygstyper
     */
    public List<String> getIntygstyper() {
        return configurationLoader.getConfiguration().getKnownIntygstyper();
    }

    /**
     * Get all configured (loaded) privileges.
     * @return a list with privileges
     */
    public List<Privilege> getPrivileges() {
        return configurationLoader.getConfiguration().getPrivileges();
    }

    public Role getRole(String name) {
        return fnRole.apply(name);
    }

    public RequestOrigin getRequestOrigin(String name) {
        return fnRequestOrigin.apply(name);
    }

    /**
     * Get all configured (loaded) request origins.
     * @return a list with request origins
     */
    public List<RequestOrigin> getRequestOrigins() {
        return configurationLoader.getConfiguration().getRequestOrigins();
    }

    /**
     * Get all configured (loaded) roles.
     * @return a list with  roles
     */
    public List<Role> getRoles() {
        return configurationLoader.getConfiguration().getRoles();
    }

    /**
     * Get all configured (loaded) titles (a.k.a legitimerade yrkesgrupper).
     * @return a list with titles
     */
    public List<Title> getTitles() {
        return configurationLoader.getConfiguration().getTitles();
    }

    /**
     * Get all configured (loaded) title codes (a.k.a befattningskoder).
     * @return a list with title codes
     */
    public List<TitleCode> getTitleCodes() {
        return configurationLoader.getConfiguration().getTitleCodes();
    }


    // ~ Getter and setter
    // ======================================================================================

    public AuthoritiesConfigurationLoader getConfigurationLoader() {
        return configurationLoader;
    }

    public void setConfigurationLoader(AuthoritiesConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }

    public HsaPersonService getHsaPersonService() {
        return hsaPersonService;
    }

    public void setHsaPersonService(HsaPersonService hsaPersonService) {
        this.hsaPersonService = hsaPersonService;
    }


    // ~ Package methods
    // ======================================================================================

    /**
     * Resolve a user role using SAML credential and HSA information.
     *
     * @param sa SAML credentials wrapped into a cusom class SakerhetstjanstAssertion
     * @param userTypes HSA person information object.
     *
     * @return the resolved role
     */
    Role lookupUserRole(SakerhetstjanstAssertion sa, List<PersonInformationType> userTypes) {
        Role role;

        // 1. Bestäm användarens roll utefter titel som kommer från SAML.
        //    Titel ska vara detsamma som legitimerade yrkesgrupper.
        role = lookupUserRoleByLegitimeradeYrkesgrupper(sa.getTitel());
        if (role != null) {
            return role;
        }

        // 2. Bestäm användarens roll utefter legitimerade yrkesgrupper som hämtas från HSA.
        role = lookupUserRoleByLegitimeradeYrkesgrupper(extractLegitimeradeYrkesgrupper(userTypes));
        if (role != null) {
            return role;
        }

        // 3. Bestäm användarens roll utefter befattningskod som kommer från SAML.
        role = lookupUserRoleByBefattningskod(sa.getTitelKod());
        if (role != null) {
            return role;
        }

        // 4. Bestäm användarens roll utefter kombinationen befattningskod och gruppförskrivarkod
        role = lookupUserRoleByBefattningskodAndGruppforskrivarkod(sa.getTitelKod(), sa.getForskrivarkod());
        if (role != null) {
            return role;
        }

        // 6. Användaren är en vårdadministratör inom landstinget
        return fnRole.apply(ROLE_ADMIN);
    }

    /** Lookup user role by looking into 'legitimerade yrkesgrupper'.
     * Currently there are only two 'yrkesgrupper' to look for:
     * <ul>
     * <li>Läkare</li>
     * <li>Tandläkare</li>
     * </ul>
     *
     * @param legitimeradeYrkesgrupper string array with 'legitimerade yrkesgrupper'
     * @return a user role if valid 'yrkesgrupper', otherwise null
     */
    Role lookupUserRoleByLegitimeradeYrkesgrupper(List<String> legitimeradeYrkesgrupper) {
        LOG.debug("  * legitimerade yrkesgrupper");
        if (legitimeradeYrkesgrupper == null || legitimeradeYrkesgrupper.size() == 0) {
            return null;
        }

        if (legitimeradeYrkesgrupper.contains(TITLE_LAKARE)) {
            return fnRole.apply(ROLE_LAKARE);
        }

        if (legitimeradeYrkesgrupper.contains(TITLE_TANDLAKARE)) {
            return fnRole.apply(ROLE_TANDLAKARE);
        }

        return null;
    }

    Role lookupUserRoleByBefattningskod(List<String> befattningsKoder) {
        LOG.debug("  * befattningskod");

        if (befattningsKoder == null || befattningsKoder.size() == 0) {
            return null;
        }

        if (befattningsKoder.contains(TITLECODE_AT_LAKARE)) {
            return fnRole.apply(ROLE_LAKARE);
        }

        return null;
    }

    Role lookupUserRoleByBefattningskodAndGruppforskrivarkod(List<String> befattningsKoder, List<String> gruppforskrivarKoder) {
        for (String befattningskod : befattningsKoder) {
            for (String gruppforskrivarKod : gruppforskrivarKoder) {
                Role role = lookupUserRoleByBefattningskodAndGruppforskrivarkod(befattningskod, gruppforskrivarKod);
                if (role != null) {
                    return role;
                }
            }
        }

        return null;
    }

    Role lookupUserRoleByBefattningskodAndGruppforskrivarkod(String befattningsKod, String gruppforskrivarKod) {
        LOG.debug("  * befattningskod i kombination med gruppförskrivarkod");
        LOG.debug("    befattningskod = {}, gruppförskrivarkod = {}", befattningsKod, gruppforskrivarKod);

        if (befattningsKod == null || gruppforskrivarKod == null) {
            return null;
        }

        TitleCode titleCode = fnTitleCode.apply(befattningsKod, gruppforskrivarKod);
        if (titleCode == null) {
            LOG.debug("    kombinationen befattningskod and gruppförskrivarkod finns inte i konfigurationen");
            return null;
        }
        LOG.debug("    kombinationen befattningskod and gruppförskrivarkod hittad");

        Role role = fnRole.apply(titleCode.getRole().getName());
        if (role == null) {
            throw new AuthoritiesException("fnRole.apply(titleCode.fnRole()) returnerade 'null' vilket indikerar felaktig konfiguration av roller");
        }

        return role;
    }


    // ~ Private methods
    // ======================================================================================

    private List<String> extractLegitimeradeYrkesgrupper(List<PersonInformationType> hsaUserTypes) {
        Set<String> lygSet = new TreeSet<>();

        for (PersonInformationType userType : hsaUserTypes) {
            if (userType.getPaTitle() != null) {
                List<String> hsaTitles = userType.getPaTitle().stream().map(paTitle -> paTitle.getPaTitleName()).collect(Collectors.toList());
                lygSet.addAll(hsaTitles);
            }
        }

        return new ArrayList<>(lygSet);
    }

    private SakerhetstjanstAssertion getAssertion(Assertion assertion) {
        if (assertion == null) {
            throw new IllegalArgumentException("Assertion parameter cannot be null");
        }

        return new SakerhetstjanstAssertion(assertion);
    }

    private List<PersonInformationType> getPersonInfo(String hsaId) {
        LOG.debug("Retrieving user information from HSA...");

        List<PersonInformationType> personInfo;
        try {
            personInfo = hsaPersonService.getHsaPersonInfo(hsaId);
            if (personInfo == null || personInfo.isEmpty()) {
                LOG.info("Call to web service getHsaPersonInfo did not return any info for user '{}'", hsaId);
            }
        } catch (Exception e) {
            LOG.error("Failed retrieving user information from HSA for user {}, error message {}", hsaId, e.getMessage());
            throw new HsaServiceException(hsaId, e);
        }

        return personInfo;
    }


    // ~ Lambdas
    // ======================================================================================

    private Predicate<RequestOrigin> isRequestOrigin(String name) {
        return ro -> ro.getName() != null && ro.getName().equalsIgnoreCase(name);
    }

    private Predicate<Role> isRole(String name) {
        return r -> r.getName() != null && r.getName().equalsIgnoreCase(name);
    }

    private Predicate<Title> isTitle(String title) {
        return t -> t.getTitle() != null && t.getTitle().equalsIgnoreCase(title);
    }

    private Predicate<TitleCode> isTitleCode(String titleCode) {
        return tc -> tc.getTitleCode() != null && tc.getTitleCode().equalsIgnoreCase(titleCode);
    }

    private Predicate<TitleCode> isGroupPrescriptionCode(String groupPrescriptionCode) {
        return tc -> tc.getGroupPrescriptionCode() != null && tc.getGroupPrescriptionCode().equalsIgnoreCase(groupPrescriptionCode);
    }

    private Function<String, RequestOrigin> fnRequestOrigin = (name) -> {
        return getRequestOrigins().stream()
                .filter(isRequestOrigin(name))
                .findFirst()
                .orElse(null);
    };

    private Function<String, Role> fnRole = (name) -> {
        return getRoles().stream()
                .filter(isRole(name))
                .findFirst()
                .orElse(null);
    };

    private Function<String, Title> fnTitle = (title) -> {
        return getTitles().stream()
                .filter(isTitle(title))
                .findFirst()
                .orElse(null);
    };

    private BiFunction<String, String, TitleCode> fnTitleCode = (titleCode, groupPrescriptionCode) -> {
        return getTitleCodes().stream()
                .filter(isTitleCode(titleCode).and(isGroupPrescriptionCode(groupPrescriptionCode)))
                .findFirst()
                .orElse(null);
    };

}
