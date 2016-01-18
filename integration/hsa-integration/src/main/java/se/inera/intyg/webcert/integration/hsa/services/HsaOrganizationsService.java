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

package se.inera.intyg.webcert.integration.hsa.services;

import java.util.List;

import se.inera.intyg.webcert.integration.hsa.model.Vardenhet;
import se.inera.intyg.webcert.integration.hsa.model.Vardgivare;

/**
 *
 * @author andreaskaltenbach
 */
public interface HsaOrganizationsService {

    /**
     * Returns a list of Vardgivare and authorized enheter where the HoS person is authorized to work at.
     *
     * @return list of vårdgivare containing authorized enheter and mottagningar. If user is not authorized at all,
     *         an empty list will be returned
     */
    List<Vardgivare> getAuthorizedEnheterForHosPerson(String hosPersonHsaId);

    Vardenhet getVardenhet(String vardenhetHsaId);
}
