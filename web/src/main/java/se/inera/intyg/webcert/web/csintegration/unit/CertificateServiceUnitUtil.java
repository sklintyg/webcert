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

package se.inera.intyg.webcert.web.csintegration.unit;

import se.inera.intyg.infra.integration.hsatk.model.legacy.AbstractVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.IntygUser;

public class CertificateServiceUnitUtil {

    private CertificateServiceUnitUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static AbstractVardenhet getCareUnit(IntygUser user) {
        final var vardenhet = (AbstractVardenhet) user.getValdVardenhet();

        if (vardenhet instanceof Mottagning) {
            final var mottagning = (Mottagning) user.getValdVardenhet();
            final var chosenCareProvider = (Vardgivare) user.getValdVardgivare();

            return chosenCareProvider.getVardenheter().stream()
                .filter(unit -> hasMatch(mottagning.getParentHsaId(), unit.getId()))
                .findFirst()
                .orElseThrow();
        }
        return vardenhet;
    }

    private static boolean hasMatch(String id1, String id2) {
        return id1.equalsIgnoreCase(id2);
    }
}
