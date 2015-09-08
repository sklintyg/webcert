/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Certificate Web (http://code.google.com/p/inera-certificate-web).
 *
 * Inera Certificate Web is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Inera Certificate Web is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.webcert.service.user;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import se.inera.certificate.modules.support.feature.ModuleFeature;
import se.inera.webcert.service.user.dto.WebCertUser;
import se.inera.webcert.service.feature.WebcertFeature;

import java.util.List;

@Service
public class WebCertUserServiceImpl implements WebCertUserService {

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserService.class);

    public WebCertUser getWebCertUser() {
        return (WebCertUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public boolean isAuthorizedForUnit(String vardgivarHsaId, String enhetsHsaId, boolean isReadOnlyOperation) {
        WebCertUser user = getWebCertUser();
        return checkIfAuthorizedForUnit(user, vardgivarHsaId, enhetsHsaId, isReadOnlyOperation);
    }

    @Override
    public boolean isAuthorizedForUnit(String enhetsHsaId, boolean isReadOnlyOperation) {
        WebCertUser user = getWebCertUser();
        return checkIfAuthorizedForUnit(user, null, enhetsHsaId, isReadOnlyOperation);
    }

    public boolean checkIfAuthorizedForUnit(WebCertUser user, String vardgivarHsaId, String enhetsHsaId, boolean isReadOnlyOperation) {

        if (user == null) {
            return false;
        }

        if (user.hasAktivFunktion(WebcertFeature.FRAN_JOURNALSYSTEM.getName())) {

            if (isReadOnlyOperation && vardgivarHsaId != null) {
                return user.getValdVardgivare().getId().equals(vardgivarHsaId);
            }

            return user.getIdsOfSelectedVardenhet().contains(enhetsHsaId);

        } else {
            return user.getIdsOfSelectedVardenhet().contains(enhetsHsaId);
        }
    }

    public boolean isAuthorizedForUnits(List<String> enhetsHsaIds) {
        WebCertUser user = getWebCertUser();
        return user != null && user.getIdsOfSelectedVardenhet().containsAll(enhetsHsaIds);
    }

    public void enableFeaturesOnUser(WebcertFeature... featuresToEnable) {
        WebCertUser user = getWebCertUser();
        enableFeatures(user, featuresToEnable);
    }

    public void enableFeatures(WebCertUser user, WebcertFeature... featuresToEnable) {
        LOG.debug("User {} had these features: {}", user.getHsaId(), StringUtils.join(user.getAktivaFunktioner(), ", "));

        for (WebcertFeature feature : featuresToEnable) {
            user.getAktivaFunktioner().add(feature.getName());
        }

        LOG.debug("User {} now has these features: {}", user.getHsaId(), StringUtils.join(user.getAktivaFunktioner(), ", "));
    }

    @Override
    public void enableModuleFeatureOnUser(String moduleName, ModuleFeature... modulefeaturesToEnable) {

        Assert.notNull(moduleName);
        Assert.notEmpty(modulefeaturesToEnable);

        WebCertUser user = getWebCertUser();

        enableModuleFeatures(user, moduleName, modulefeaturesToEnable);
    }

    public void enableModuleFeatures(WebCertUser user, String moduleName, ModuleFeature... modulefeaturesToEnable) {
        for (ModuleFeature moduleFeature : modulefeaturesToEnable) {

            String moduleFeatureName = moduleFeature.getName();
            String moduleFeatureStr = StringUtils.join(new String[] { moduleFeatureName, moduleName.toLowerCase() }, ".");

            if (!user.hasAktivFunktion(moduleFeatureName)) {
                LOG.warn("Could not add module feature '{}' to user {} since corresponding webcert feature is not enabled", moduleFeatureStr,
                        user.getHsaId());
                continue;
            }

            user.getAktivaFunktioner().add(moduleFeatureStr);
            LOG.debug("Added module feature {} to user", moduleFeatureStr);
        }
    }

    @Override
    public void clearEnabledFeaturesOnUser() {

        WebCertUser user = getWebCertUser();
        user.getAktivaFunktioner().clear();

        LOG.debug("Cleared enabled featured from user {}", user.getHsaId());
    }
}
