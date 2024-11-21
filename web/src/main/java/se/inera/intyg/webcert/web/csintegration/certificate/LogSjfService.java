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

package se.inera.intyg.webcert.web.csintegration.certificate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Component
@RequiredArgsConstructor
public class LogSjfService {

    private final MonitoringLogService monitoringLogService;

    public void log(Certificate certificate, WebCertUser user) {
        final var certificateId = certificate.getMetadata().getId();
        final var certificateType = certificate.getMetadata().getType();
        final var certificateCareProviderId = certificate.getMetadata().getCareProvider().getUnitId();
        final var certificateCareUnitId = certificate.getMetadata().getUnit().getUnitId();
        final var userCareProviderId = user.getValdVardgivare().getId();
        final var userCareUnitId = user.getValdVardenhet().getId();
        if (!certificateCareProviderId.equals(userCareProviderId)) {
            monitoringLogService.logIntegratedOtherCaregiver(
                certificateId,
                certificateType,
                certificateCareProviderId,
                certificateCareUnitId,
                userCareProviderId,
                userCareUnitId
            );
        } else if (!user.getValdVardenhet().getHsaIds().contains(certificateCareUnitId)) {
            monitoringLogService.logIntegratedOtherUnit(
                certificateId,
                certificateType,
                certificateCareProviderId,
                certificateCareUnitId,
                userCareProviderId,
                userCareUnitId
            );
        }
    }
}
