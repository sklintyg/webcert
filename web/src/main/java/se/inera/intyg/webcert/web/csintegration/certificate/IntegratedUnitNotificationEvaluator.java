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

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntegratedUnitNotificationEvaluator {

    private final GetUnitNotificationConfig getUnitNotificationConfig;

    public boolean mailNotification(String careProviderId, String issuedOnUnitId, String certificateId, LocalDateTime issuingDate) {
        final var regionNotificationConfigs = getUnitNotificationConfig.get();
        if (regionNotificationConfigs.isEmpty()) {
            return false;
        }

        if (evaluateMailNotification(careProviderId, issuedOnUnitId, regionNotificationConfigs, issuingDate)) {
            log.info(
                "Certificate with id '{}' has been evaluated and should receive mail notification. CareProviderId: '{}',"
                    + " IssuedOnUnitId: '{}', IssuingDate: '{}'", certificateId, careProviderId, issuedOnUnitId, issuingDate);
            return true;
        }

        log.info(
            "Certificate with id '{}' has been evaluated and should not receive mail notification. CareProviderId: '{}',"
                + " IssuedOnUnitId: '{}', IssuingDate: '{}'", certificateId, careProviderId, issuedOnUnitId, issuingDate);
        return false;
    }

    private boolean evaluateMailNotification(String careProviderId, String issuedOnUnitId,
        List<RegionNotificationConfig> regionNotificationConfigs, LocalDateTime issuingDate) {
        return regionNotificationConfigs.stream()
            .map(RegionNotificationConfig::getConfiguration)
            .flatMap(List::stream)
            .anyMatch(config ->
                evaluateMailNotification(
                    config.getIssuedOnUnit(),
                    config.getCareProviders(),
                    config.getDatetime(),
                    issuedOnUnitId,
                    careProviderId,
                    issuingDate
                )
            );
    }

    private boolean evaluateMailNotification(List<String> unitIds, List<String> careProviderIds, LocalDateTime activateFrom, String unitId,
        String careProviderId, LocalDateTime issuingDate) {
        if ((careProviderIds != null && !careProviderIds.contains(careProviderId)) && (unitIds != null && !unitIds.contains(unitId))) {
            return false;
        }

        if (activateFrom.isAfter(LocalDateTime.now())) {
            return false;
        }

        return issuingDate.isBefore(activateFrom);
    }
}
