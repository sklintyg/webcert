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

package se.inera.intyg.webcert.web.service.user;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import se.inera.intyg.common.support.modules.support.feature.ModuleFeature;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolver;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.webcert.web.auth.authorities.Privilege;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.auth.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WebCertUserServiceImpl implements WebCertUserService {

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserService.class);

    @Autowired
    private AuthoritiesResolver authoritiesResolver;

    @Override
    public WebCertUser getUser() {
        return (WebCertUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public void enableFeaturesOnUser(WebcertFeature... featuresToEnable) {
        enableFeatures(getUser(), featuresToEnable);
    }

    @Override
    public void enableModuleFeatureOnUser(String moduleName, ModuleFeature... modulefeaturesToEnable) {
        Assert.notNull(moduleName);
        Assert.notEmpty(modulefeaturesToEnable);

        enableModuleFeatures(getUser(), moduleName, modulefeaturesToEnable);
    }

    /**
     * Method returns all granted intygstyper for a certain user's privilege.
     * If user doesn't have a privilege, an empty set is returned.
     *
     * Note:
     * The configuration mindset of privileges is that if there are no
     * intygstyper attached to a privilege, the privilege is implicitly
     * valid for all intygstyper. However, this method will return an
     * explicit list with granted intygstyper in all cases.
     *
     * @param privilegeName the privilege name
     * @return returns a set of granted intygstyper, an empty set means no granted intygstyper for this privilege
     */
    @Override
    public Set<String> getIntygstyper(String privilegeName) {
        Assert.notNull(privilegeName);

        // If user doesn't have a privilege, return an empty set
        AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        if (!authoritiesValidator.given(getUser()).privilege(privilegeName).isVerified()) {
            return Collections.emptySet();
        }

        // User is granted privilege access, get the privilege's intygstyper
        Privilege privilege = getUser().getAuthorities().get(privilegeName);

        // Return intygstyper configured for this privilege
        List<String> intygsTyper = privilege.getIntygstyper();
        if (intygsTyper == null || intygsTyper.isEmpty()) {
            // The privilege didn't have any intygstyper
            // restrictions, return all known intygstyper
            intygsTyper = authoritiesResolver.getIntygstyper();
        }

        return intygsTyper.stream().collect(Collectors.toSet());
    }

    @Override
    public boolean isAuthorizedForUnit(String vardgivarHsaId, String enhetsHsaId, boolean isReadOnlyOperation) {
        return checkIfAuthorizedForUnit(getUser(), vardgivarHsaId, enhetsHsaId, isReadOnlyOperation);
    }

    @Override
    public boolean isAuthorizedForUnit(String enhetsHsaId, boolean isReadOnlyOperation) {
        return checkIfAuthorizedForUnit(getUser(), null, enhetsHsaId, isReadOnlyOperation);
    }

    @Override
    public boolean isAuthorizedForUnits(List<String> enhetsHsaIds) {
        WebCertUser user = getUser();
        return user != null && user.getIdsOfSelectedVardenhet().containsAll(enhetsHsaIds);
    }

    @Override
    public void updateOrigin(String origin) {
        getUser().setOrigin(origin);
    }

    @Override
    public void updateUserRole(String roleName) {
        updateUserRole(authoritiesResolver.getRole(roleName));
    }

    public void updateUserRole(Role role) {
        getUser().setRoles(AuthoritiesResolverUtil.toMap(role));
        getUser().setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));
    }

    // - - - - - Package scope - - - - -

    boolean checkIfAuthorizedForUnit(WebCertUser user, String vardgivarHsaId, String enhetsHsaId, boolean isReadOnlyOperation) {
        if (user == null) {
            return false;
        }

        String origin = user.getOrigin();
        if (origin.equals(WebCertUserOriginType.DJUPINTEGRATION.name())) {
            if (isReadOnlyOperation && vardgivarHsaId != null) {
                return user.getValdVardgivare().getId().equals(vardgivarHsaId);
            }
            return user.getIdsOfSelectedVardenhet().contains(enhetsHsaId);
        } else {
            return user.getIdsOfSelectedVardenhet().contains(enhetsHsaId);
        }
    }

    void enableFeatures(WebCertUser user, WebcertFeature... featuresToEnable) {
        LOG.debug("User {} had these features: {}", user.getHsaId(), StringUtils.join(user.getFeatures(), ", "));

        for (WebcertFeature feature : featuresToEnable) {
            user.getFeatures().add(feature.getName());
        }

        LOG.debug("User {} now has these features: {}", user.getHsaId(), StringUtils.join(user.getFeatures(), ", "));
    }

    void enableModuleFeatures(WebCertUser user, String moduleName, ModuleFeature... modulefeaturesToEnable) {
        for (ModuleFeature moduleFeature : modulefeaturesToEnable) {

            String moduleFeatureName = moduleFeature.getName();
            String moduleFeatureStr = StringUtils.join(new String[] { moduleFeatureName, moduleName.toLowerCase() }, ".");

            if (!user.isFeatureActive(moduleFeatureName)) {
                LOG.warn("Could not add module feature '{}' to user {} since corresponding webcert feature is not enabled", moduleFeatureStr,
                        user.getHsaId());
                continue;
            }

            user.getFeatures().add(moduleFeatureStr);
            LOG.debug("Added module feature {} to user", moduleFeatureStr);
        }
    }

}
