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

package se.inera.intyg.webcert.integration.privatepractitioner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
@Profile("private-practitioner-service-active")
@ComponentScan(basePackages = {"se.inera.intyg.webcert.integration.privatepractitioner"})
public class PrivatePractitionerRestClientConfig {

    @Value("${privatepractitionerservice.base.url}")
    private String privatePractitionerServiceBaseUrl;
    public static final String CONFIG_PATH = "/configuration";
    public static final String VALIDATE_PATH = "/validate";
    public static final String HOSP_INFO_PATH = "/hosp";

    @Bean(name = "ppsRestClient")
    public RestClient ppsRestClient() {
        return RestClient.create(privatePractitionerServiceBaseUrl);
    }

}
