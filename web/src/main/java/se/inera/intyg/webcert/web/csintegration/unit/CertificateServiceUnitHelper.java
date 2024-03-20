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
import se.inera.intyg.infra.integration.hsatk.model.legacy.AbstractVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Component
public class CertificateServiceUnitHelper {

    private final WebCertUserService webCertUserService;
    private final CertificateServiceVardenhetConverter certificateServiceVardenhetConverter;

    public CertificateServiceUnitHelper(WebCertUserService webCertUserService,
        CertificateServiceVardenhetConverter certificateServiceVardenhetConverter) {
        this.webCertUserService = webCertUserService;
        this.certificateServiceVardenhetConverter = certificateServiceVardenhetConverter;
    }

    public CertificateServiceUnitDTO getCareProvider() {
        final var user = webCertUserService.getUser();
        return CertificateServiceUnitDTO.builder()
            .id(user.getValdVardgivare().getId())
            .name(user.getValdVardgivare().getNamn())
            .build();
    }

    public CertificateServiceUnitDTO getCareUnit() {
        final var user = webCertUserService.getUser();

        if (user.getValdVardenhet() instanceof Mottagning) {
            final var mottagning = (Mottagning) user.getValdVardenhet();
            final var parentUnitId = mottagning.getParentHsaId();

            final var chosenCareProvider = (Vardgivare) user.getValdVardgivare();
            final var parentUnit = chosenCareProvider.getVardenheter().stream()
                .filter(unit -> hasMatch(parentUnitId, unit.getId()))
                .findFirst()
                .orElseThrow();

            return certificateServiceVardenhetConverter.convert(parentUnit);
        }

        return certificateServiceVardenhetConverter.convert((AbstractVardenhet) user.getValdVardenhet());
    }

    public CertificateServiceUnitDTO getUnit() {
        final var user = webCertUserService.getUser();
        return certificateServiceVardenhetConverter.convert((AbstractVardenhet) user.getValdVardenhet());
    }

    private static boolean hasMatch(String id1, String id2) {
        return id1.equalsIgnoreCase(id2);
    }
}
