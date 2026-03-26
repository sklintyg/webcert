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
package se.inera.intyg.webcert.infra.srs.config;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.getdiagnosiscodes.v1.GetDiagnosisCodesResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.getpredictionquestions.v1.GetPredictionQuestionsResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.GetSRSInformationResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformationfordiagnosis.v1.GetSRSInformationForDiagnosisResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionResponderInterface;
import se.inera.intyg.webcert.infra.srs.services.SrsInfraService;
import se.inera.intyg.webcert.infra.srs.services.SrsInfraServiceImpl;

@Configuration
public class SrsServicesConfiguration {

  @Bean
  public GetSRSInformationResponderInterface srsClient(
      @Value("${srs.getsrsinformation.endpoint.url}") String address) {
    return createClient(GetSRSInformationResponderInterface.class, address);
  }

  @Bean
  public GetPredictionQuestionsResponderInterface prediktionQuestionBean(
      @Value("${srs.questions.endpoint.url}") String address) {
    return createClient(GetPredictionQuestionsResponderInterface.class, address);
  }

  @Bean
  public GetConsentResponderInterface getConsentBean(
      @Value("${srs.getconsent.endpoint.url}") String address) {
    return createClient(GetConsentResponderInterface.class, address);
  }

  @Bean
  public SetConsentResponderInterface setConsentBean(
      @Value("${srs.setconsent.endpoint.url}") String address) {
    return createClient(SetConsentResponderInterface.class, address);
  }

  @Bean
  public GetDiagnosisCodesResponderInterface getDiagnosisCodesBean(
      @Value("${srs.getdiagnosiscodes.endpoint.url}") String address) {
    return createClient(GetDiagnosisCodesResponderInterface.class, address);
  }

  @Bean
  public GetSRSInformationForDiagnosisResponderInterface getSrsForDiagnosisBean(
      @Value("${srs.getsrsfordiagnosis.endpoint.url}") String address) {
    return createClient(GetSRSInformationForDiagnosisResponderInterface.class, address);
  }

  @Bean
  public SetOwnOpinionResponderInterface setOwnOpinionBean(
      @Value("${srs.setownopinion.endpoint.url}") String address) {
    return createClient(SetOwnOpinionResponderInterface.class, address);
  }

  @Bean
  public SrsInfraService srsService() {
    return new SrsInfraServiceImpl();
  }

  @SuppressWarnings("unchecked")
  private <T> T createClient(Class<T> serviceClass, String address) {
    JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(serviceClass);
    factory.setAddress(address);
    return (T) factory.create();
  }
}
