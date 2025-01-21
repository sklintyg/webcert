/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.fmb.sjukfall;

import java.util.List;

import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.controller.api.dto.Period;

public interface FmbSjukfallService {

    /**
     * Get total sjukfall days for the patient including the added list of periods.
     *
     * The service will calculate based on active sjukfall registrered for the patient, that are related to the list of periods.
     *
     * E.g. If the passed period is 2020-01-15 - 2020-01-25, the service will check to see if the period is related to a sjukfall that
     * is withing the starting date (2020-01-15) and MAX_GAP (e.g. 5 days). Total time in days will include the sjukfall, but make sure
     * that any overlapping periods are excluded.
     *
     * @param personnummer Patient´s social security numberr
     * @param periods List of periods to consider when calculating total time in days.
     * @return Total time in days.
     */
    int totalSjukskrivningstidForPatientAndCareUnit(Personnummer personnummer, List<Period> periods);
}
