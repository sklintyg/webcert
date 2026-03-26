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
package se.inera.intyg.webcert.infra.ia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import se.inera.intyg.webcert.infra.ia.services.IABannerService;
import se.inera.intyg.webcert.infra.ia.services.IABannerServiceImpl;
import se.inera.intyg.webcert.infra.ia.stub.IABannerServiceStub;

@Configuration
public class IAServicesConfiguration {

  @Bean
  @Profile("ia-stub")
  public IABannerService iaBannerServiceStub() {
    return new IABannerServiceStub();
  }

  @Bean
  @Profile("!ia-stub")
  public IABannerService iaBannerService() {
    return new IABannerServiceImpl();
  }
}
