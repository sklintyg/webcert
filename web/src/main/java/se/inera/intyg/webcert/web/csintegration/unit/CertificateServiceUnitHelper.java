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

import java.util.List;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

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
        final var chosenCareProvider = getChosenCareProvider();

        final var units = chosenCareProvider.getVardenheter();

        final var chosenUnit = units.stream()
            .filter(unit -> hasMatchInUnit(unit, user) || hasMatchInSubUnits(unit.getMottagningar(), user.getValdVardenhet().getId()))
            .findFirst()
            .orElseThrow();

        return certificateServiceVardenhetConverter.convert(chosenUnit);
    }

    public CertificateServiceUnitDTO getUnit() {
        final var user = webCertUserService.getUser();
        final var chosenCareProvider = getChosenCareProvider();
        final var units = chosenCareProvider.getVardenheter();

        final var chosenUnit = units.stream()
            .filter(unit -> hasMatchInUnit(unit, user))
            .findFirst()
            .orElseThrow();

        return certificateServiceVardenhetConverter.convert(chosenUnit);
    }

    private Vardgivare getChosenCareProvider() {
        final var user = webCertUserService.getUser();
        return user.getVardgivare().stream()
            .filter(careProvider -> hasMatchInCareProvider(careProvider, user))
            .findFirst()
            .orElseThrow();
    }

    private static boolean hasMatchInCareProvider(Vardgivare careProvider, WebCertUser user) {
        return hasMatch(careProvider.getId(), user.getValdVardgivare().getId());
    }

    private static boolean hasMatchInUnit(Vardenhet unit, WebCertUser user) {
        return hasMatch(unit.getId(), user.getValdVardenhet().getId());
    }

    private static boolean hasMatch(String id1, String id2) {
        return id1.equalsIgnoreCase(id2);
    }

    private boolean hasMatchInSubUnits(List<Mottagning> units, String loggedInUnitId) {
        if (units == null) {
            return false;
        }
        return units.stream().anyMatch(unit -> unit.getId().equalsIgnoreCase(loggedInUnitId));
    }
}
