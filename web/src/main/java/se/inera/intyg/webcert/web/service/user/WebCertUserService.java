/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import jakarta.servlet.http.HttpSession;
import java.util.List;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public interface WebCertUserService {

    /**
     * Checks wether the currently executing thread has a user Principal, e.g. through
     * {@link org.springframework.security.core.context.SecurityContextHolder#getContext}.
     * <p>
     * Typically, all requests from the frontend for a protected resource will have a Principal while
     * unsecured and threads invoked by a service will not, such as CreateDraftCertificate.
     *
     * @return true if there is an available user Principal, false if not.
     */
    boolean hasAuthenticationContext();

    /**
     * Returns the user principal from the session.
     */
    WebCertUser getUser();

    /**
     * Stores (creates or updates) the given key-value pair for current user. Stores in DB and updates the session.
     *
     * @param key An arbitrary string-based key.
     * @param value An arbitrary string-based value.
     */
    void storeUserPreference(String key, String value);

    /**
     * Deletes the specified user preference for the current user.
     *
     * @param key Preference key.
     */
    void deleteUserPreference(String key);

    /**
     * Deletes all user preferences for the current user.
     */
    void deleteUserPreferences();

    boolean isAuthorizedForUnit(String vardgivarHsaId, String enhetsHsaId, boolean isReadOnlyOperation);

    boolean isAuthorizedForUnit(String enhetsHsaId, boolean isReadOnlyOperation);

    boolean isAuthorizedForUnits(List<String> enhetsHsaIds);

    void updateOrigin(String origin);

    void updateUserRole(String roleName);

    /**
     * Note - this is just a proxy for accessing
     * {@link se.inera.intyg.infra.security.common.service.CareUnitAccessHelper#userIsLoggedInOnEnhetOrUnderenhet}.
     *
     * @param enhetId HSA-id of a vardenhet or mottagning.
     * @return True if the current IntygUser has access to the specified enhetsId including mottagningsnivå.
     */
    boolean isUserAllowedAccessToUnit(String enhetId);

    void scheduleSessionRemoval(HttpSession session);

    void cancelScheduledLogout(HttpSession session);

    /**
     * Performs immediate removal of backend session (both HttpSession and Spring session) for user.
     *
     * @param session The HttpSession to be immediately invalidated.
     */
    void removeSessionNow(HttpSession session);
}
