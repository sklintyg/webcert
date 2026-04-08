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
package se.inera.intyg.webcert.infra.srs.stub.config;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import se.inera.intyg.webcert.infra.srs.stub.GetConsentStub;
import se.inera.intyg.webcert.infra.srs.stub.GetDiagnosisCodesStub;
import se.inera.intyg.webcert.infra.srs.stub.GetPredictionQuestionsStub;
import se.inera.intyg.webcert.infra.srs.stub.GetSRSInformationForDiagnosisStub;
import se.inera.intyg.webcert.infra.srs.stub.GetSrsInformationStub;
import se.inera.intyg.webcert.infra.srs.stub.SetConsentStub;
import se.inera.intyg.webcert.infra.srs.stub.SetOwnOpinionStub;
import se.inera.intyg.webcert.infra.srs.stub.repository.ConsentRepository;

@Configuration
@Profile("dev")
public class SrsStubConfiguration {

  @Autowired private Bus bus;

  @Bean
  public ConsentRepository consentRepository() {
    return new ConsentRepository();
  }

  @Bean
  public GetSrsInformationStub getSrsInformationStub() {
    return new GetSrsInformationStub();
  }

  @Bean
  public GetPredictionQuestionsStub getPredictionQuestionsStub() {
    return new GetPredictionQuestionsStub();
  }

  @Bean
  public GetConsentStub getConsentStub() {
    return new GetConsentStub();
  }

  @Bean
  public SetConsentStub setConsentStub() {
    return new SetConsentStub();
  }

  @Bean
  public GetDiagnosisCodesStub getDiagnosisCodesStub() {
    return new GetDiagnosisCodesStub();
  }

  @Bean
  public GetSRSInformationForDiagnosisStub getSRSInformationForDiagnosisStub() {
    return new GetSRSInformationForDiagnosisStub();
  }

  @Bean
  public SetOwnOpinionStub setOwnOpinionStub() {
    return new SetOwnOpinionStub();
  }

  @Bean
  public EndpointImpl getSrsEndpoint(GetSrsInformationStub stub) {
    EndpointImpl endpoint = new EndpointImpl(bus, stub);
    endpoint.publish("/stubs/getsrs");
    return endpoint;
  }

  @Bean
  public EndpointImpl predictionQuestionsEndpoint(GetPredictionQuestionsStub stub) {
    EndpointImpl endpoint = new EndpointImpl(bus, stub);
    endpoint.publish("/stubs/predictionquestions");
    return endpoint;
  }

  @Bean
  public EndpointImpl getConsentEndpoint(GetConsentStub stub) {
    EndpointImpl endpoint = new EndpointImpl(bus, stub);
    endpoint.publish("/stubs/get-consent");
    return endpoint;
  }

  @Bean
  public EndpointImpl setConsentEndpoint(SetConsentStub stub) {
    EndpointImpl endpoint = new EndpointImpl(bus, stub);
    endpoint.publish("/stubs/set-consent");
    return endpoint;
  }

  @Bean
  public EndpointImpl getDiagnosisCodesEndpoint(GetDiagnosisCodesStub stub) {
    EndpointImpl endpoint = new EndpointImpl(bus, stub);
    endpoint.publish("/stubs/diagnosiscodes");
    return endpoint;
  }

  @Bean
  public EndpointImpl getSrsForDiagnosisEndpoint(GetSRSInformationForDiagnosisStub stub) {
    EndpointImpl endpoint = new EndpointImpl(bus, stub);
    endpoint.publish("/stubs/getsrsfordiagnosis");
    return endpoint;
  }
  // StatisticsImageStub is a @RestController and is auto-discovered by component scan.
}
