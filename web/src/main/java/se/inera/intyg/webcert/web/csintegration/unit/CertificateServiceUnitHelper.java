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

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.integration.hsatk.model.legacy.AbstractVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Component
@RequiredArgsConstructor
public class CertificateServiceUnitHelper {

    private final WebCertUserService webCertUserService;
    private final CertificateServiceVardenhetConverter certificateServiceVardenhetConverter;

    public CertificateServiceUnitDTO getCareProvider() {
        return getCareProvider(Optional.empty());
    }

    public CertificateServiceUnitDTO getCareProvider(Optional<IntygUser> optionalUser) {
        final var user = getUser(optionalUser);
        return CertificateServiceUnitDTO.builder()
            .id(user.getValdVardgivare().getId())
            .name(user.getValdVardgivare().getNamn())
            .build();
    }

    public CertificateServiceUnitDTO getCareUnit() {
        return getCareUnit(Optional.empty());
    }

    public CertificateServiceUnitDTO getCareUnit(Optional<IntygUser> optionalUser) {
        final var user = getUser(optionalUser);
        final var vardenhet = (AbstractVardenhet) user.getValdVardenhet();

        if (vardenhet instanceof Mottagning) {
            final var mottagning = (Mottagning) user.getValdVardenhet();
            final var chosenCareProvider = (Vardgivare) user.getValdVardgivare();

            final var parentUnit = chosenCareProvider.getVardenheter().stream()
                .filter(unit -> hasMatch(mottagning.getParentHsaId(), unit.getId()))
                .findFirst()
                .orElseThrow();

            return certificateServiceVardenhetConverter.convert(parentUnit);
        } else {
            return certificateServiceVardenhetConverter.convert(vardenhet);
        }
    }

    public CertificateServiceUnitDTO getUnit() {
        return getUnit(Optional.empty());
    }

    public CertificateServiceUnitDTO getUnit(Optional<IntygUser> optionalUser) {
        final var user = getUser(optionalUser);
        return certificateServiceVardenhetConverter.convert((AbstractVardenhet) user.getValdVardenhet());
    }

    private static boolean hasMatch(String id1, String id2) {
        return id1.equalsIgnoreCase(id2);
    }

    private IntygUser getUser(Optional<IntygUser> optionalUser) {
        return optionalUser.orElseGet(webCertUserService::getUser);
    }
}
