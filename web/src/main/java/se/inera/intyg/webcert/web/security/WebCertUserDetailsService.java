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

package se.inera.intyg.webcert.web.security;

import static se.inera.intyg.common.integration.hsa.stub.Medarbetaruppdrag.VARD_OCH_BEHANDLING;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.opensaml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.integration.hsa.model.AuthenticationMethod;
import se.inera.intyg.common.integration.hsa.model.Vardenhet;
import se.inera.intyg.common.integration.hsa.model.Vardgivare;
import se.inera.intyg.common.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.common.integration.hsa.services.HsaPersonService;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.auth.common.BaseWebCertUserDetailsService;
import se.inera.intyg.webcert.web.auth.exceptions.HsaServiceException;
import se.inera.intyg.webcert.web.auth.exceptions.MissingMedarbetaruppdragException;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.infrastructure.directory.v1.PersonInformationType;

import javax.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author andreaskaltenbach
 */
@Service
public class WebCertUserDetailsService extends BaseWebCertUserDetailsService implements SAMLUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserDetailsService.class);

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Autowired
    private HsaPersonService hsaPersonService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    // ~ API
    // =====================================================================================

    @Override
    public Object loadUserBySAML(SAMLCredential credential) {

        if (credential == null) {
            throw new RuntimeException("SAMLCredential has not been set.");
        }

        LOG.info("Start user authentication...");

        if (LOG.isDebugEnabled()) {
            // I dont want to read this object every time.
            String str = ToStringBuilder.reflectionToString(credential);
            LOG.debug("SAML credential is:\n{}", str);
        }

        try {
            // Create the user
            WebCertUser webCertUser = createUser(credential);

            LOG.info("End user authentication...SUCCESS");
            return webCertUser;

        } catch (Exception e) {
            LOG.error("End user authentication...FAIL");
            if (e instanceof AuthenticationException) {
                throw e;
            }

            LOG.error("Error building user {}, failed with message {}", getAssertion(credential).getHsaId(), e);
            throw new RuntimeException(getAssertion(credential).getHsaId(), e);
        }
    }

    // ~ Protected scope
    // =====================================================================================

    protected SakerhetstjanstAssertion getAssertion(SAMLCredential credential) {
        return getAssertion(credential.getAuthenticationAssertion());
    }

    protected List<Vardgivare> getAuthorizedVardgivare(String hsaId) {
        LOG.debug("Retrieving authorized units from HSA...");

        try {
            return hsaOrganizationsService.getAuthorizedEnheterForHosPerson(hsaId);

        } catch (Exception e) {
            LOG.error("Failed retrieving authorized units from HSA for user {}, error message {}", hsaId, e.getMessage());
            throw new HsaServiceException(hsaId, e);
        }
    }

    protected List<PersonInformationType> getPersonInfo(String hsaId) {
        LOG.debug("Retrieving user information from HSA...");

        List<PersonInformationType> hsaPersonInfo;
        try {
            hsaPersonInfo = hsaPersonService.getHsaPersonInfo(hsaId);
            if (hsaPersonInfo == null || hsaPersonInfo.isEmpty()) {
                LOG.info("Call to web service getHsaPersonInfo did not return any info for user '{}'", hsaId);
            }

        } catch (Exception e) {
            LOG.error("Failed retrieving user information from HSA for user {}, error message {}", hsaId, e.getMessage());
            throw new HsaServiceException(hsaId, e);
        }

        return hsaPersonInfo;
    }

    // ~ Package scope
    // =====================================================================================

    WebCertUser createUser(SAMLCredential credential) {
        LOG.debug("Creating Webcert user object...");

        String hsaId = getAssertion(credential).getHsaId();
        List<PersonInformationType> personInfo = getPersonInfo(hsaId);
        List<Vardgivare> authorizedVardgivare = getAuthorizedVardgivare(hsaId);

        try {
            assertMIU(credential);
            assertAuthorizedVardgivare(hsaId, authorizedVardgivare);

            HttpServletRequest request = getCurrentRequest();
            Role role = getAuthoritiesResolver().resolveRole(credential, request);
            LOG.debug("User role is set to {}", role);

            return createWebCertUser(role, credential, authorizedVardgivare, personInfo);

        } catch (MissingMedarbetaruppdragException e) {
            monitoringLogService.logMissingMedarbetarUppdrag(getAssertion(credential).getHsaId());
            throw e;
        }

    }

    SakerhetstjanstAssertion getAssertion(Assertion assertion) {
        if (assertion == null) {
            throw new IllegalArgumentException("Assertion parameter cannot be null");
        }

        return new SakerhetstjanstAssertion(assertion);
    }

    // ~ Private scope
    // =====================================================================================

    private void assertAuthorizedVardgivare(String hsaId, List<Vardgivare> authorizedVardgivare) {
        LOG.debug("Assert user has authorization to one or more 'vårdenheter'");

        // if user does not have access to any vardgivare, we have to reject authentication
        if (authorizedVardgivare == null || authorizedVardgivare.isEmpty()) {
            throw new MissingMedarbetaruppdragException(hsaId);
        }
    }

    private void assertMIU(SAMLCredential credential) {
        LOG.debug("Assert 'medarbetaruppdrag (MIU)'");

        // if user has authenticated with other contract than 'Vård och behandling', we have to reject her
        if (!VARD_OCH_BEHANDLING.equals(getAssertion(credential).getMedarbetaruppdragType())) {
            throw new MissingMedarbetaruppdragException(getAssertion(credential).getHsaId());
        }
    }

    private WebCertUser createWebCertUser(Role role, SAMLCredential credential, List<Vardgivare> authorizedVardgivare,
            List<PersonInformationType> personInfo) {
        LOG.debug("Decorate/populate user object with additional information");

        SakerhetstjanstAssertion sa = getAssertion(credential);
        WebCertUserOrigin webCertUserOrigin = new WebCertUserOrigin();

        // Create the WebCert user object injection user's privileges
        WebCertUser webcertUser = new WebCertUser();

        webcertUser.setHsaId(sa.getHsaId());
        webcertUser.setNamn(compileName(sa.getFornamn(), sa.getMellanOchEfternamn()));
        webcertUser.setVardgivare(authorizedVardgivare);

        // Set role and privileges
        webcertUser.setRoles(AuthoritiesResolverUtil.toMap(role));
        webcertUser.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));

        // Förskrivarkod is sensitive information, not allowed to store real value
        webcertUser.setForskrivarkod("0000000");

        // Set user's authentication scheme
        webcertUser.setAuthenticationScheme(sa.getAuthenticationScheme());

        // Set application mode / request origin
        String requestOrigin = webCertUserOrigin.resolveOrigin(getCurrentRequest());
        webcertUser.setOrigin(getAuthoritiesResolver().getRequestOrigin(requestOrigin).getName());

        decorateWebCertUserWithAdditionalInfo(webcertUser, credential, personInfo);
        decorateWebCertUserWithAvailableFeatures(webcertUser);
        decorateWebCertUserWithAuthenticationMethod(webcertUser, credential);
        decorateWebCertUserWithDefaultVardenhet(webcertUser, credential);

        return webcertUser;
    }

    private void decorateWebCertUserWithAdditionalInfo(WebCertUser webcertUser, SAMLCredential credential,
            List<PersonInformationType> hsaPersonInfo) {

        List<String> specialiseringar = extractSpecialiseringar(hsaPersonInfo);
        List<String> legitimeradeYrkesgrupper = extractLegitimeradeYrkesgrupper(hsaPersonInfo);
        List<String> befattningar = extractBefattningar(hsaPersonInfo);
        String titel = extractTitel(hsaPersonInfo);

        webcertUser.setSpecialiseringar(specialiseringar);
        webcertUser.setLegitimeradeYrkesgrupper(legitimeradeYrkesgrupper);
        webcertUser.setBefattningar(befattningar);
        webcertUser.setTitel(titel);
    }

    private List<String> extractBefattningar(List<PersonInformationType> hsaPersonInfo) {
        Set<String> befattningar = new TreeSet<>();

        for (PersonInformationType userType : hsaPersonInfo) {
            if (userType.getPaTitle() != null) {
                List<String> hsaTitles = userType.getPaTitle().stream()
                        .map(paTitle -> paTitle.getPaTitleName())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                befattningar.addAll(hsaTitles);
            }
        }

        return new ArrayList<>(befattningar);
    }

    /**
     * Tries to use title attribute, otherwise resorts to healthcareProfessionalLicenses.
     */
    private String extractTitel(List<PersonInformationType> hsaPersonInfo) {
        Set<String> titleSet = new HashSet<>();
        for (PersonInformationType pit : hsaPersonInfo) {
            if (pit.getTitle() != null && pit.getTitle().trim().length() > 0) {
                titleSet.add(pit.getTitle());
            } else if (pit.getHealthCareProfessionalLicence() != null && pit.getHealthCareProfessionalLicence().size() > 0) {
                titleSet.addAll(pit.getHealthCareProfessionalLicence());
            }
        }
        return titleSet.stream().sorted().collect(Collectors.joining(", "));
    }

    private void decorateWebCertUserWithAuthenticationMethod(WebCertUser webcertUser, SAMLCredential credential) {
        String authenticationScheme = getAssertion(credential).getAuthenticationScheme();

        if (authenticationScheme.endsWith(":fake")) {
            webcertUser.setAuthenticationMethod(AuthenticationMethod.FAKE);
        } else {
            webcertUser.setAuthenticationMethod(AuthenticationMethod.SITHS);
        }
    }

    private void decorateWebCertUserWithDefaultVardenhet(WebCertUser user, SAMLCredential credential) {

        // Get HSA id for the selected MIU
        String medarbetaruppdragHsaId = getAssertion(credential).getEnhetHsaId();

        boolean changeSuccess;

        if (StringUtils.isNotBlank(medarbetaruppdragHsaId)) {
            changeSuccess = user.changeValdVardenhet(medarbetaruppdragHsaId);
        } else {
            LOG.error("Assertion did not contain any 'medarbetaruppdrag', defaulting to use one of the Vardenheter present in the user");
            changeSuccess = setFirstVardenhetOnFirstVardgivareAsDefault(user);
        }

        if (!changeSuccess) {
            LOG.error("When logging in user '{}', unit with HSA-id {} could not be found in users MIUs", user.getHsaId(), medarbetaruppdragHsaId);
            throw new MissingMedarbetaruppdragException(user.getHsaId());
        }

        LOG.debug("Setting care unit '{}' as default unit on user '{}'", user.getValdVardenhet().getId(), user.getHsaId());
    }

    private List<String> extractLegitimeradeYrkesgrupper(List<PersonInformationType> hsaUserTypes) {
        Set<String> lygSet = new TreeSet<>();

        for (PersonInformationType userType : hsaUserTypes) {
            if (userType.getPaTitle() != null) {
                List<String> hsaTitles = userType.getPaTitle().stream()
                        .map(paTitle -> paTitle.getPaTitleName())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                lygSet.addAll(hsaTitles);
            }
        }

        return new ArrayList<>(lygSet);
    }

    private List<String> extractSpecialiseringar(List<PersonInformationType> hsaUserTypes) {
        Set<String> specSet = new TreeSet<>();

        for (PersonInformationType userType : hsaUserTypes) {
            if (userType.getSpecialityName() != null) {
                List<String> specialityNames = userType.getSpecialityName();
                specSet.addAll(specialityNames);
            }
        }

        return new ArrayList<>(specSet);
    }

    private boolean setFirstVardenhetOnFirstVardgivareAsDefault(WebCertUser user) {
        Vardgivare firstVardgivare = user.getVardgivare().get(0);
        user.setValdVardgivare(firstVardgivare);

        Vardenhet firstVardenhet = firstVardgivare.getVardenheter().get(0);
        user.setValdVardenhet(firstVardenhet);

        return true;
    }
}
