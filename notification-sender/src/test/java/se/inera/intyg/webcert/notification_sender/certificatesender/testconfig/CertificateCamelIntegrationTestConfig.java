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
package se.inera.intyg.webcert.notification_sender.certificatesender.testconfig;

import static org.mockito.Mockito.mock;

import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponderInterface;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.notification_sender.certificatesender.services.mock.MockSendCertificateServiceClientImpl;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;

@ImportResource(locations = "classpath:certificates/integration-test-certificate-sender-config.xml")
public class CertificateCamelIntegrationTestConfig {

    @Bean
    public IntygModuleRegistry intygModuleRegistry() {
        return null;
    }

    @Bean
    public MdcHelper mdcHelper() {
        return new MdcHelper();
    }

    @Bean
    public SendMessageToRecipientResponderInterface sendMessageToRecipientResponderInterface() {
        return null;
    }

    @Bean
    public RegisterApprovedReceiversResponderInterface registerApprovedReceiversResponderInterface() {
        return null;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return mock(JmsTransactionManager.class);
    }

    @Bean
    public SpringTransactionPolicy txTemplate(PlatformTransactionManager transactionManager) {
        return new SpringTransactionPolicy(transactionManager);
    }

    @Bean
    public MockSendCertificateServiceClientImpl mockSendCertificateServiceClient() {
        return new MockSendCertificateServiceClientImpl();
    }
}
