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
package se.inera.intyg.webcert.web.config;

import jakarta.xml.ws.Endpoint;
import java.util.List;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import se.inera.intyg.common.util.integration.interceptor.SoapFaultToSoapResponseTransformerInterceptor;
import se.inera.intyg.webcert.notificationstub.config.NotificationStubConfig;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3.CreateDraftCertificateResponderImpl;
import se.inera.intyg.webcert.web.integration.interactions.getcertificateadditions.GetCertificateAdditionsResponderImpl;
import se.inera.intyg.webcert.web.integration.interactions.listcertificatesforcarewithqa.ListCertificatesForCareWithQAResponderImpl;
import se.inera.intyg.webcert.web.integration.interactions.receivemedicalcertificate.ReceiveAnswerResponderImpl;
import se.inera.intyg.webcert.web.integration.interactions.receivemedicalcertificate.ReceiveQuestionResponderImpl;
import se.inera.intyg.webcert.web.integration.interactions.sendmessagetocare.SendMessageToCareResponderImpl;

@Configuration
@ComponentScan("se.inera.intyg.webcert.infra.srs.stub.config")
@ComponentScan("se.inera.intyg.webcert.infra.ia.stub.config")
@Import(NotificationStubConfig.class)
public class CxfEndpointConfig {

  @Bean
  public Endpoint createDraftCertificateEndpoint(
      CreateDraftCertificateResponderImpl implementor, Bus bus) {
    EndpointImpl endpoint = new EndpointImpl(bus, implementor);
    endpoint.setSchemaLocations(
        List.of(
            "classpath:/core_components/clinicalprocess_healthcond_certificate_3.3.xsd",
            "classpath:/core_components/clinicalprocess_healthcond_certificate_3.2_ext.xsd",
            "classpath:/core_components/clinicalprocess_healthcond_certificate_3.3_ext.xsd",
            "classpath:/core_components/clinicalprocess_healthcond_certificate_3.4_ext.xsd",
            "classpath:/core_components/clinicalprocess_healthcond_certificate_types_3.2.xsd",
            "classpath:/core_components/xmldsig-core-schema_0.1.xsd",
            "classpath:/core_components/xmldsig-filter2.xsd",
            "classpath:/interactions/CreateDraftCertificateInteraction/CreateDraftCertificateResponder_3.3.xsd"));
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/clinicalprocess-healthcond-3/create-draft-certificate.xslt"));
    endpoint.publish("/create-draft-certificate/v3.0");
    return endpoint;
  }

  @Bean
  public Endpoint receiveQuestionEndpoint(ReceiveQuestionResponderImpl implementor, Bus bus) {
    EndpointImpl endpoint = new EndpointImpl(bus, implementor);
    endpoint
        .getOutFaultInterceptors()
        .add(new SoapFaultToSoapResponseTransformerInterceptor("transform/receive-question.xslt"));
    endpoint.publish("/receive-question/v1.0");
    return endpoint;
  }

  @Bean
  public Endpoint receiveAnswerEndpoint(ReceiveAnswerResponderImpl implementor, Bus bus) {
    EndpointImpl endpoint = new EndpointImpl(bus, implementor);
    endpoint
        .getOutFaultInterceptors()
        .add(new SoapFaultToSoapResponseTransformerInterceptor("transform/receive-answer.xslt"));
    endpoint.publish("/receive-answer/v1.0");
    return endpoint;
  }

  @Bean
  public Endpoint sendMessageToCareEndpoint(SendMessageToCareResponderImpl implementor, Bus bus) {
    EndpointImpl endpoint = new EndpointImpl(bus, implementor);
    endpoint.setSchemaLocations(
        List.of(
            "classpath:/core_components/clinicalprocess_healthcond_certificate_3.3.xsd",
            "classpath:/core_components/clinicalprocess_healthcond_certificate_3.2_ext.xsd",
            "classpath:/core_components/clinicalprocess_healthcond_certificate_3.4_ext.xsd",
            "classpath:/core_components/clinicalprocess_healthcond_certificate_types_3.2.xsd",
            "classpath:/core_components/xmldsig-core-schema_0.1.xsd",
            "classpath:/core_components/xmldsig-filter2.xsd",
            "classpath:/interactions/SendMessageToCareInteraction/SendMessageToCareResponder_2.0.xsd"));
    endpoint
        .getOutFaultInterceptors()
        .add(
            new SoapFaultToSoapResponseTransformerInterceptor(
                "transform/clinicalprocess-healthcond-3/send-message-to-care.xslt"));
    endpoint.publish("/send-message-to-care/v2.0");
    return endpoint;
  }

  @Bean
  public Endpoint listCertificatesForCareWithQaEndpoint(
      ListCertificatesForCareWithQAResponderImpl implementor, Bus bus) {
    EndpointImpl endpoint = new EndpointImpl(bus, implementor);
    endpoint.setSchemaLocations(
        List.of(
            "classpath:/core_components/clinicalprocess_healthcond_certificate_3.3.xsd",
            "classpath:/core_components/clinicalprocess_healthcond_certificate_3.4_ext.xsd",
            "classpath:/core_components/clinicalprocess_healthcond_certificate_3.2_ext.xsd",
            "classpath:/core_components/clinicalprocess_healthcond_certificate_types_3.2.xsd",
            "classpath:/core_components/xmldsig-core-schema_0.1.xsd",
            "classpath:/core_components/xmldsig-filter2.xsd",
            "classpath:/interactions/ListCertificatesForCareWithQAInteraction/ListCertificatesForCareWithQAResponder_3.3.xsd"));
    endpoint.publish("/list-certificates-for-care-with-qa/v3.0");
    return endpoint;
  }

  @Bean
  public Endpoint getCertificateAdditionsEndpoint(
      GetCertificateAdditionsResponderImpl implementor, Bus bus) {
    EndpointImpl endpoint = new EndpointImpl(bus, implementor);
    endpoint.setSchemaLocations(
        List.of(
            "classpath:/core_components/intyg_clinicalprocess_healthcond_certificate_3.2.xsd",
            "classpath:/core_components/intyg_clinicalprocess_healthcond_certificate_3.2_ext.xsd",
            "classpath:/core_components/intyg_clinicalprocess_healthcond_certificate_types_3.2.xsd",
            "classpath:/core_components/xmldsig-core-schema_0.1.xsd",
            "classpath:/core_components/xmldsig-filter2.xsd",
            "classpath:/interactions/GetCertificateAdditionsInteraction/GetCertificateAdditionsResponder_1.1.xsd"));
    endpoint.publish("/get-certificate-additions/v1.1");
    return endpoint;
  }
}
