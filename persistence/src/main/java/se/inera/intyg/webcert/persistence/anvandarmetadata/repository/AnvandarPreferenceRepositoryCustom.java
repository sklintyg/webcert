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

package se.inera.intyg.webcert.persistence.anvandarmetadata.repository;

import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.anvandarmetadata.model.AnvandarPreference;

import java.util.Map;

/**
 * Created by eriklupander on 2015-08-05.
 */
@Transactional(value = "jpaTransactionManager", readOnly = false)
public interface AnvandarPreferenceRepositoryCustom {

    Map<String, String> getAnvandarPreference(String hsaId);

    boolean exists(String hsaId, String key);

    AnvandarPreference findByHsaIdAndKey(String hsaId, String key);
}
