/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.anvandarmetadata.repository;

import java.util.Map;
import se.inera.intyg.webcert.persistence.anvandarmetadata.model.AnvandarPreference;

/**
 * Created by eriklupander on 2015-08-05.
 */
public interface AnvandarPreferenceRepositoryCustom {

    /**
     * Returns all anvandar preferences for a given hsaId as key-value pairs.
     *
     * @param hsaId User identifier.
     * @return If no entries exists for the given hsaId, an empty (non-null) map will be returned.
     */
    Map<String, String> getAnvandarPreference(String hsaId);

    /**
     * Returns true if there is an entry for the given hsaId/key.
     *
     * @param hsaId User identifier.
     * @param key Preference identifier.
     */
    boolean exists(String hsaId, String key);

    /**
     * If no AnvandarPreference exists for the given hsaId and key, null will be returned.
     *
     * @param hsaId User identifier.
     * @param key Preference identifier.
     * @return If found, a AnvandarPreference instance, otherwise null.
     */
    AnvandarPreference findByHsaIdAndKey(String hsaId, String key);
}
