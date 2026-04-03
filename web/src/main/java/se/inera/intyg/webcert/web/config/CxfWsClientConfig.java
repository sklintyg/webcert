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

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.List;
import java.util.regex.Pattern;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduitConfigurer;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswer.rivtabp20.v1.SendMedicalCertificateAnswerResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listrelationsforcertificate.v1.ListRelationsForCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.getCertificate.v2.GetCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;

@Configuration
public class CxfWsClientConfig {

  private static final Pattern NTJP_CONDUIT_PATTERN =
      Pattern.compile(
          "\\{urn:riv:(clinicalprocess:healthcond|insuranceprocess:healthreporting|itintegration:monitoring):.*\\.http-conduit");

  // ── NTJP clients ─────────────────────────────────────────────────────────

  @Bean
  public SendMedicalCertificateQuestionResponderInterface sendQuestionToFKClient(
      @Value("${sendquestiontofk.endpoint.url}") String address) {
    return createClient(SendMedicalCertificateQuestionResponderInterface.class, address);
  }

  @Bean
  public SendMedicalCertificateAnswerResponderInterface sendAnswerToFKClient(
      @Value("${sendanswertofk.endpoint.url}") String address) {
    return createClient(SendMedicalCertificateAnswerResponderInterface.class, address);
  }

  @Bean
  public ListCertificatesForCareResponderInterface listCertificatesForCareResponderV3(
      @Value("${intygstjanst.listcertificatesforcare.v3.endpoint.url}") String address) {
    return createClient(ListCertificatesForCareResponderInterface.class, address);
  }

  @Bean
  public SendCertificateToRecipientResponderInterface sendCertificateClient(
      @Value("${intygstjanst.sendcertificate.endpoint.url}") String address) {
    return createClient(SendCertificateToRecipientResponderInterface.class, address);
  }

  @Bean
  public RevokeMedicalCertificateResponderInterface revokeCertificateClient(
      @Value("${intygstjanst.revokecertificate.endpoint.url}") String address) {
    return createClient(RevokeMedicalCertificateResponderInterface.class, address);
  }

  @Bean
  public RevokeCertificateResponderInterface revokeCertificateClientRivta(
      @Value("${intygstjanst.revokecertificaterivta.endpoint.url}") String address) {
    return createClient(RevokeCertificateResponderInterface.class, address);
  }

  @Bean
  public SendMessageToRecipientResponderInterface sendMessageToRecipientClient(
      @Value("${intygstjanst.sendmessagetorecipient.endpoint.url}") String address) {
    return createClient(SendMessageToRecipientResponderInterface.class, address);
  }

  @Bean
  public RegisterCertificateResponderInterface registerCertificateClient(
      @Value("${intygstjanst.registercertificate.v3.endpoint.url}") String address) {
    return createClient(RegisterCertificateResponderInterface.class, address);
  }

  @Bean
  public GetCertificateResponderInterface getCertificateClient(
      @Value("${intygstjanst.getcertificate.endpoint.url}") String address) {
    return createClient(GetCertificateResponderInterface.class, address);
  }

  @Bean
  public ListActiveSickLeavesForCareUnitResponderInterface listActiveSickLeavesForCareUnitClient(
      @Value("${intygstjanst.listactivesickleavesforcareunit.v1.endpoint.url}") String address) {
    return createClient(ListActiveSickLeavesForCareUnitResponderInterface.class, address);
  }

  // ── Non-NTJP clients ──────────────────────────────────────────────────────

  @Bean
  public GetCertificateTypeInfoResponderInterface getCertificateTypeInfoClient(
      @Value("${intygstjanst.getcertificatetypeinfo.endpoint.url}") String address) {
    return createClient(GetCertificateTypeInfoResponderInterface.class, address);
  }

  @Bean
  public ListRelationsForCertificateResponderInterface listRelationsForCertificateClient(
      @Value("${intygstjanst.listrelationsforcertificate.endpoint.url}") String address) {
    return createClient(ListRelationsForCertificateResponderInterface.class, address);
  }

  @Bean
  public ListApprovedReceiversResponderInterface listApprovedReceiversClient(
      @Value("${intygstjanst.listapprovedreceivers.endpoint.url}") String address) {
    return createClient(ListApprovedReceiversResponderInterface.class, address);
  }

  @Bean
  public ListPossibleReceiversResponderInterface listPossibleReceiversClient(
      @Value("${intygstjanst.listpossiblereceivers.endpoint.url}") String address) {
    return createClient(ListPossibleReceiversResponderInterface.class, address);
  }

  @Bean
  public RegisterApprovedReceiversResponderInterface registerApprovedReceiversClient(
      @Value("${intygstjanst.registerapprovedreceivers.endpoint.url}") String address) {
    return createClient(RegisterApprovedReceiversResponderInterface.class, address);
  }

  // ── TLS conduit configuration (non-dev only) ──────────────────────────────

  @Bean
  @Profile("!dev")
  public HTTPConduitConfigurer ntjpTlsConduitConfigurer(
      @Value("${ntjp.ws.key.manager.password}") String keyManagerPassword,
      @Value("${ntjp.ws.certificate.file}") String certificateFile,
      @Value("${ntjp.ws.certificate.password}") String certificatePassword,
      @Value("${ntjp.ws.certificate.type}") String certificateType,
      @Value("${ntjp.ws.truststore.file}") String truststoreFile,
      @Value("${ntjp.ws.truststore.password}") String truststorePassword,
      @Value("${ntjp.ws.truststore.type}") String truststoreType) {

    return (name, address, conduit) -> {
      if (!isNtjpConduit(name)) {
        return;
      }
      try {
        KeyStore keyStore = KeyStore.getInstance(certificateType);
        try (FileInputStream fis = new FileInputStream(certificateFile)) {
          keyStore.load(fis, certificatePassword.toCharArray());
        }
        KeyManagerFactory kmf =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyManagerPassword.toCharArray());

        KeyStore trustStore = KeyStore.getInstance(truststoreType);
        try (FileInputStream fis = new FileInputStream(truststoreFile)) {
          trustStore.load(fis, truststorePassword.toCharArray());
        }
        TrustManagerFactory tmf =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        TLSClientParameters tlsParams = new TLSClientParameters();
        tlsParams.setDisableCNCheck(true);
        tlsParams.setKeyManagers(kmf.getKeyManagers());
        tlsParams.setTrustManagers(tmf.getTrustManagers());

        FiltersType filters = new FiltersType();
        filters
            .getInclude()
            .addAll(
                List.of(
                    ".*_EXPORT_.*",
                    ".*_EXPORT1024_.*",
                    ".*_WITH_DES_.*",
                    ".*_WITH_AES_.*",
                    ".*_WITH_NULL_.*"));
        filters.getExclude().add(".*_DH_anon_.*");
        tlsParams.setCipherSuitesFilter(filters);
        conduit.setTlsClientParameters(tlsParams);

        HTTPClientPolicy clientPolicy = new HTTPClientPolicy();
        clientPolicy.setAllowChunking(false);
        clientPolicy.setAutoRedirect(true);
        clientPolicy.setConnection(ConnectionType.KEEP_ALIVE);
        conduit.setClient(clientPolicy);
      } catch (Exception e) {
        throw new IllegalStateException("Failed to configure TLS for conduit: " + name, e);
      }
    };
  }

  private boolean isNtjpConduit(String name) {
    return name != null && NTJP_CONDUIT_PATTERN.matcher(name).find();
  }

  @SuppressWarnings("unchecked")
  private <T> T createClient(Class<T> serviceClass, String address) {
    JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(serviceClass);
    factory.setAddress(address);
    return (T) factory.create();
  }
}
