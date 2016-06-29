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

import se.inera.intyg.common.security.common.service.CommonUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public interface WebCertUserService extends CommonUserService {

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

    void deleteUserPreference(String key);
}
