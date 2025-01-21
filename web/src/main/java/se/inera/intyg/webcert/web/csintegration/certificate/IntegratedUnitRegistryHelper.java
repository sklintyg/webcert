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

package se.inera.intyg.webcert.web.csintegration.certificate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Component
@RequiredArgsConstructor
public class IntegratedUnitRegistryHelper {

    private final WebCertUserService webCertUserService;
    private final IntegreradeEnheterRegistry integreradeEnheterRegistry;

    public void addUnit(IntygUser user) {
        integreradeEnheterRegistry.putIntegreradEnhet(
            new IntegreradEnhetEntry(
                user.getValdVardenhet().getId(),
                user.getValdVardenhet().getNamn(),
                user.getValdVardgivare().getId(),
                user.getValdVardgivare().getNamn()
            ),
            false,
            true);
    }


    public void addUnitForCopy(Certificate certificate, Certificate copy) {
        if (webCertUserService.getUser().getOrigin().equals(UserOriginType.DJUPINTEGRATION.toString())) {
            IntegreradEnhetEntry newEntry = new IntegreradEnhetEntry(
                copy.getMetadata().getCareUnit().getUnitId(),
                copy.getMetadata().getCareUnit().getUnitName(),
                copy.getMetadata().getCareProvider().getUnitId(),
                copy.getMetadata().getCareProvider().getUnitName()
            );

            integreradeEnheterRegistry.addIfSameVardgivareButDifferentUnits(
                certificate.getMetadata().getCareUnit().getUnitId(),
                newEntry,
                copy.getMetadata().getType()
            );
        }
    }
}
