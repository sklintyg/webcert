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
import se.inera.intyg.infra.integration.hsatk.services.HsatkOrganizationService;
import se.inera.intyg.webcert.web.service.facade.user.UserService;

@Component
public class CertificateServiceUnitHelper {

    private final UserService userService;
    private final CertificateServiceUnitConverter certificateServiceUnitConverter;
    private final CertificateServiceHsaUnitConverter certificateServiceHsaUnitConverter;
    private final HsatkOrganizationService hsatkOrganizationService;

    public CertificateServiceUnitHelper(UserService userService, CertificateServiceUnitConverter certificateServiceUnitConverter,
        CertificateServiceHsaUnitConverter certificateServiceHsaUnitConverter, HsatkOrganizationService hsatkOrganizationService) {
        this.userService = userService;
        this.certificateServiceUnitConverter = certificateServiceUnitConverter;
        this.certificateServiceHsaUnitConverter = certificateServiceHsaUnitConverter;
        this.hsatkOrganizationService = hsatkOrganizationService;
    }

    public CertificateServiceUnitDTO getCareProvider() {
        final var user = userService.getLoggedInUser();
        return certificateServiceUnitConverter.convert(user.getLoggedInCareProvider());
    }

    public CertificateServiceUnitDTO getCareUnit() {
        final var user = userService.getLoggedInUser();
        return certificateServiceUnitConverter.convert(user.getLoggedInCareUnit());
    }

    public CertificateServiceUnitDTO getUnit() {
        final var user = userService.getLoggedInUser();
        final var unit = hsatkOrganizationService.getUnit(user.getLoggedInUnit().getUnitId(), null);
        return certificateServiceHsaUnitConverter.convert(unit);
    }
}