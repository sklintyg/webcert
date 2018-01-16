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
package se.inera.intyg.webcert.web.service.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.infra.security.common.service.CareUnitAccessHelper;
import se.inera.intyg.webcert.persistence.anvandarmetadata.model.AnvandarPreference;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
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

    @Autowired
    private WebCertUserService webCertUserService;

    @Override
    public boolean hasAuthenticationContext() {
        return SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null;
    }

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

    /**
     * Note - this is just a proxy for accessing {@link CareUnitAccessHelper#userIsLoggedInOnEnhetOrUnderenhet(IntygUser, String)}.
     *
     * @param enhetId
     *      HSA-id of a vardenhet or mottagning.
     * @return
     *      True if the current IntygUser has access to the specified enhetsId including mottagningsnivå.
     */
    @Override
    public boolean userIsLoggedInOnEnhetOrUnderenhet(String enhetId) {
        return CareUnitAccessHelper.userIsLoggedInOnEnhetOrUnderenhet(getUser(), enhetId);
    }


    // - - - - - Package scope - - - - -

    boolean checkIfAuthorizedForUnit(WebCertUser user, String vardgivarHsaId, String enhetsHsaId, boolean isReadOnlyOperation) {
        if (user == null) {
            return false;
        }

        String origin = user.getOrigin();
        if (origin.equals(UserOriginType.DJUPINTEGRATION.name())) {
            if (isReadOnlyOperation && vardgivarHsaId != null) {
                return user.getValdVardgivare().getId().equals(vardgivarHsaId);
            }
            return user.getIdsOfSelectedVardenhet().contains(enhetsHsaId);
        } else if (origin.equals(UserOriginType.READONLY.name())) {
            return CareUnitAccessHelper.userIsLoggedInOnEnhetOrUnderenhet(user, enhetsHsaId);
        } else {
            return user.getIdsOfSelectedVardenhet().contains(enhetsHsaId);
        }
    }

    private void updateUserRole(Role role) {
        getUser().setRoles(AuthoritiesResolverUtil.toMap(role));
        getUser().setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
    }
}
