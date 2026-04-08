/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.infra.xmldsig.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import se.inera.intyg.webcert.infra.xmldsig.service.FakeSignatureService;
import se.inera.intyg.webcert.infra.xmldsig.service.FakeSignatureServiceBlocked;
import se.inera.intyg.webcert.infra.xmldsig.service.FakeSignatureServiceImpl;
import se.inera.intyg.webcert.infra.xmldsig.service.PrepareSignatureService;
import se.inera.intyg.webcert.infra.xmldsig.service.PrepareSignatureServiceImpl;
import se.inera.intyg.webcert.infra.xmldsig.service.XMLDSigService;
import se.inera.intyg.webcert.infra.xmldsig.service.XMLDSigServiceImpl;

@Configuration
public class XmlDSigConfiguration {

  @Bean
  public XMLDSigService xmldSigService() {
    return new XMLDSigServiceImpl();
  }

  @Bean
  public PrepareSignatureService prepareSignatureService() {
    return new PrepareSignatureServiceImpl();
  }

  @Bean
  @Profile("!prod")
  public FakeSignatureService fakeSignatureServiceDev() {
    return new FakeSignatureServiceImpl();
  }

  @Bean
  @Profile("prod")
  public FakeSignatureService fakeSignatureServiceProd() {
    return new FakeSignatureServiceBlocked();
  }
}
