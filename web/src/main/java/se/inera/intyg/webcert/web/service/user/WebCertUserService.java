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

import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.security.common.service.Feature;
import se.inera.intyg.common.support.modules.support.feature.ModuleFeature;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.List;

public interface WebCertUserService {

    /**
     * Checks wether the currently executing thread has a user Principal, e.g. through
     * {@link org.springframework.security.core.context.SecurityContextHolder#getContext}.
     *
     * Typically, all requests from the frontend for a protected resource will have a Principal while
     * unsecured and threads invoked by a service will not, such as CreateDraftCertificate.
     *
     * @return
     *      true if there is an available user Principal, false if not.
     */
    boolean hasAuthenticationContext();

    /**
     * Returns the user principal from the session.
     * @return
     */
    WebCertUser getUser();

    /**
     * Stores (creates or updates) the given key-value pair for current user. Stores in DB and updates the session.
     *
     * @param key
     *         An arbitrary string-based key.
     *
     * @param value
     *         An arbitrary string-based value.
     */
     void storeUserPreference(String key, String value);

    /**
     * Deletes the specified user preference for the current user.
     *
     * @param key
     *      Preference key.
     */
    void deleteUserPreference(String key);

    /**
     * Deletes all user preferences for the current user.
     */
    void deleteUserPreferences();

    void enableFeaturesOnUser(Feature... featuresToEnable);

    void enableModuleFeatureOnUser(String moduleName, ModuleFeature... modulefeaturesToEnable);

    boolean isAuthorizedForUnit(String vardgivarHsaId, String enhetsHsaId, boolean isReadOnlyOperation);

    boolean isAuthorizedForUnit(String enhetsHsaId, boolean isReadOnlyOperation);

    boolean isAuthorizedForUnits(List<String> enhetsHsaIds);

    void updateOrigin(String origin);

    void updateUserRole(String roleName);

    /**
     * Since the WebCertUser#getValdVardenhet may either return a {@link Vardenhet} or a
     * {@link se.inera.intyg.infra.integration.hsa.model.Mottagning}, this method can be used to determine if:
     *
     * <ul>
     *     <li>If the selectedVardenhet is a Vardenhet: The supplied enhetsId is for the Vardenhet or one of its Mottagningar.</li>
     *     <li>If the selcetedVardenhet is a Mottagning: The supplied enhetsId is the Mottagning,
     *     its parent Vardenhet or one of the sibling Mottagningar.</li>
     * </ul>
     *
     * @param enhetsId
     *      HSA-id of a vardenhet or mottagning.
     * @return
     *      true if match is found.
     */
    boolean userIsLoggedInOnEnhetOrUnderenhet(String enhetsId);
}
