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

package se.inera.intyg.webcert.web.csintegration.unit;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.integration.hsatk.model.Unit;

@Component
public class CertificateServiceHsaUnitConverter {

    public CertificateServiceUnitDTO convert(Unit unit) {
        final var convertedUnit = new CertificateServiceUnitDTO();
        convertedUnit.setId(unit.getUnitHsaId());
        convertedUnit.setName(unit.getUnitName());
        convertedUnit.setAddress(unit.getPostalAddress().isEmpty() ? null : unit.getPostalAddress().get(0));
        convertedUnit.setZipCode(unit.getPostalCode());
        convertedUnit.setCity(shouldIncludeCity(unit) ? unit.getPostalAddress().get(unit.getPostalAddress().size() - 1) : null);
        convertedUnit.setPhoneNumber(unit.getTelephoneNumber().isEmpty() ? null : unit.getTelephoneNumber().get(0));
        convertedUnit.setEmail(unit.getMail());
        convertedUnit.setInactive(isActive(unit.getUnitStartDate(), unit.getUnitEndDate()));
        return convertedUnit;
    }

    private static boolean shouldIncludeCity(Unit unit) {
        return !unit.getPostalAddress().isEmpty() && unit.getPostalAddress().size() > 1;
    }

    public static boolean isActive(LocalDateTime fromDate, LocalDateTime toDate) {
        final var now = LocalDateTime.now();
        final var alwaysActive = fromDate == null && toDate == null;

        if (alwaysActive) {
            return true;
        }

        if (fromDate == null) {
            return toDate.isAfter(now);
        }

        if (toDate == null) {
            return fromDate.isBefore(now);
        }

        return fromDate.isBefore(now) && toDate.isAfter(now);
    }
}
