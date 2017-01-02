/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.service.Feature;
import se.inera.intyg.common.support.modules.support.feature.ModuleFeature;
import se.inera.intyg.webcert.persistence.anvandarmetadata.model.AnvandarPreference;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.List;
import java.util.Map;

@Service
public class WebCertUserServiceImpl implements WebCertUserService {

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserService.class);

    @Autowired
    private CommonAuthoritiesResolver authoritiesResolver;

    @Autowired
    private AnvandarPreferenceRepository anvandarPreferenceRepository;

    @Override
    public WebCertUser getUser() {
        return (WebCertUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public void storeUserPreference(String key, String value) {
        WebCertUser user = getUser();
        String hsaId = user.getHsaId();
        AnvandarPreference am = anvandarPreferenceRepository.findByHsaIdAndKey(hsaId, key);
        if (am == null) {
            anvandarPreferenceRepository.save(new AnvandarPreference(hsaId, key, value));
        } else {
            am.setValue(value);
            anvandarPreferenceRepository.save(am);
        }
        user.getAnvandarPreference().put(key, value);
    }

    @Override
    public void deleteUserPreference(String key) {
        WebCertUser user = getUser();
        String hsaId = user.getHsaId();
        AnvandarPreference am = anvandarPreferenceRepository.findByHsaIdAndKey(hsaId, key);
        if (am != null) {
            anvandarPreferenceRepository.delete(am);
        }
        user.getAnvandarPreference().remove(key);
    }

    @Override
    public void deleteUserPreferences() {
        WebCertUser user = getUser();
        String hsaId = user.getHsaId();
        Map<String, String> anvandarPreferences = anvandarPreferenceRepository.getAnvandarPreference(hsaId);
        int deleteCount = 0;
        for (Map.Entry<String, String> preference : anvandarPreferences.entrySet()) {
            AnvandarPreference toDelete = anvandarPreferenceRepository.findByHsaIdAndKey(hsaId, preference.getKey());
            if (toDelete != null) {
                anvandarPreferenceRepository.delete(toDelete);
                deleteCount++;
            }
        }
        if (deleteCount > 0) {
            user.getAnvandarPreference().clear();
            LOG.info("Successfully deleted " + deleteCount + " user preferences for user " + hsaId);
        }
    }

    @Override
    public void enableFeaturesOnUser(Feature... featuresToEnable) {
        enableFeatures(getUser(), featuresToEnable);
    }

    @Override
    public void enableModuleFeatureOnUser(String moduleName, ModuleFeature... modulefeaturesToEnable) {
        Assert.notNull(moduleName);
        Assert.notEmpty(modulefeaturesToEnable);

        enableModuleFeatures(getUser(), moduleName, modulefeaturesToEnable);
    }

        // Return the privilege's intygstyper
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

    void enableFeatures(WebCertUser user, Feature... featuresToEnable) {
        LOG.debug("User {} had these features: {}", user.getHsaId(), Joiner.on(", ").join(user.getFeatures()));

        for (Feature feature : featuresToEnable) {
            user.getFeatures().add(feature.getName());
        }

        LOG.debug("User {} now has these features: {}", user.getHsaId(), Joiner.on(", ").join(user.getFeatures()));
    }

    void enableModuleFeatures(WebCertUser user, String moduleName, ModuleFeature... modulefeaturesToEnable) {
        for (ModuleFeature moduleFeature : modulefeaturesToEnable) {

            String moduleFeatureName = moduleFeature.getName();
            String moduleFeatureStr = Joiner.on(".").join(moduleFeatureName, moduleName.toLowerCase());

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
