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

package se.inera.intyg.webcert.integration.servicenow.config;

import static se.inera.intyg.webcert.integration.api.subscription.ServiceNowIntegrationConstants.SERVICENOW_INTEGRATION_PROFILE_V2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
@Profile(SERVICENOW_INTEGRATION_PROFILE_V2)
@ComponentScan(basePackages = {
    "se.inera.intyg.webcert.integration.servicenow.v2",
    "se.inera.intyg.webcert.integration.servicenow.service"})
public class ServiceNowIntegrationConfigV2 {

    public ServiceNowIntegrationConfigV2() {
        log.info("Setting 'servicenow-integration-v2' to be used for subscription queries to kundportal.inera.se.");
    }
}
