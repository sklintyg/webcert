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
package se.inera.intyg.webcert.notification_sender.certificatesender.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponderInterface;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.common.client.SendCertificateServiceClient;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.notification_sender.certificatesender.routes.CertificateRouteBuilder;
import se.inera.intyg.webcert.notification_sender.certificatesender.services.CertificateRevokeProcessor;
import se.inera.intyg.webcert.notification_sender.certificatesender.services.CertificateSendProcessor;
import se.inera.intyg.webcert.notification_sender.certificatesender.services.CertificateStoreProcessor;
import se.inera.intyg.webcert.notification_sender.certificatesender.services.RegisterApprovedReceiversProcessor;
import se.inera.intyg.webcert.notification_sender.certificatesender.services.SendMessageToRecipientProcessor;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;

/**
 * Replaces certificates/beans-context.xml and certificates/camel-context.xml. Defines all
 * certificate processor beans; routes are registered in the single CamelContext managed by
 * camel-spring-boot-starter.
 */
@Configuration
public class CertificateCamelConfig {

  @Bean
  public CertificateStoreProcessor certificateStoreProcessor(
      IntygModuleRegistry moduleRegistry, MdcHelper mdcHelper) {
    return new CertificateStoreProcessor(moduleRegistry, mdcHelper);
  }

  @Bean
  public CertificateSendProcessor certificateSendProcessor(
      SendCertificateServiceClient sendServiceClient, MdcHelper mdcHelper) {
    return new CertificateSendProcessor(sendServiceClient, mdcHelper);
  }

  @Bean
  public CertificateRevokeProcessor certificateRevokeProcessor(
      IntygModuleRegistry registry, MdcHelper mdcHelper) {
    return new CertificateRevokeProcessor(registry, mdcHelper);
  }

  @Bean
  public SendMessageToRecipientProcessor sendMessageToRecipientProcessor(
      SendMessageToRecipientResponderInterface sendMessageToRecipientResponder,
      MdcHelper mdcHelper) {
    return new SendMessageToRecipientProcessor(sendMessageToRecipientResponder, mdcHelper);
  }

  @Bean
  public RegisterApprovedReceiversProcessor registerApprovedReceiversProcessor(
      RegisterApprovedReceiversResponderInterface registerApprovedReceiversClient,
      MdcHelper mdcHelper) {
    return new RegisterApprovedReceiversProcessor(registerApprovedReceiversClient, mdcHelper);
  }

  @Bean
  public CertificateRouteBuilder certificateRouteBuilder() {
    return new CertificateRouteBuilder();
  }
}
