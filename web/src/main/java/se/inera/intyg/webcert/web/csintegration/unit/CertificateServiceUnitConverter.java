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

import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.metadata.Unit;

@Component
public class CertificateServiceUnitConverter {

    public CertificateServiceUnitDTO convert(Unit unit) {
        final var convertedUnit = new CertificateServiceUnitDTO();

        convertedUnit.setId(unit.getUnitId());
        convertedUnit.setName(unit.getUnitName());
        convertedUnit.setAddress(unit.getAddress());
        convertedUnit.setZipCode(unit.getZipCode());
        convertedUnit.setCity(unit.getCity());
        convertedUnit.setPhoneNumber(unit.getPhoneNumber());
        convertedUnit.setEmail(unit.getEmail());
        convertedUnit.setInactive(unit.getIsInactive());

        return convertedUnit;

    }

}
