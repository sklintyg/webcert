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
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;

@Component
public class CertificateServiceVardenhetConverter {

    public CertificateServiceUnitDTO convert(Vardenhet unit) {
        return CertificateServiceUnitDTO.builder()
            .id(unit.getId())
            .name(unit.getNamn())
            .address(unit.getPostadress())
            .zipCode(unit.getPostnummer())
            .city(unit.getPostort())
            .phoneNumber(unit.getTelefonnummer())
            .email(unit.getEpost())
            .inactive(isActive(unit.getStart(), unit.getEnd()))
            .build();
    }

    private static boolean isActive(LocalDateTime fromDate, LocalDateTime toDate) {
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