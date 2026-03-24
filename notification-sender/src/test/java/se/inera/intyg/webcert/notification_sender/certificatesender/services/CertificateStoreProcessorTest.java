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
package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.xml.ws.WebServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ExternalServiceCallException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.logging.MdcHelper;

@ExtendWith(MockitoExtension.class)
public class CertificateStoreProcessorTest {

  private static final String LOGICAL_ADDRESS1 = "address-1";
  private static final String BODY = "body";

  ExternalServiceCallException technicalErrorException =
      new ExternalServiceCallException(
          "", ExternalServiceCallException.ErrorIdEnum.TECHNICAL_ERROR);
  ExternalServiceCallException applicationErrorException =
      new ExternalServiceCallException(
          "", ExternalServiceCallException.ErrorIdEnum.APPLICATION_ERROR);
  ExternalServiceCallException validationErrorException =
      new ExternalServiceCallException(
          "", ExternalServiceCallException.ErrorIdEnum.VALIDATION_ERROR);
  ExternalServiceCallException transformationErrorException =
      new ExternalServiceCallException(
          "", ExternalServiceCallException.ErrorIdEnum.TRANSFORMATION_ERROR);

  @Mock private IntygModuleRegistry moduleRegistry;
  @Mock private ModuleApi moduleApi;
  @Spy private MdcHelper mdcHelper;

  @InjectMocks
  private CertificateStoreProcessor certificateStoreProcessor = new CertificateStoreProcessor();

  @BeforeEach
  public void setup() throws ModuleNotFoundException {
    when(moduleRegistry.resolveVersionFromUtlatandeJson(anyString(), anyString()))
        .thenReturn("1.0");
    when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
  }

  @Test
  public void testStoreCertificate() throws Exception {
    // When
    certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);

    // Then
    verify(moduleApi).registerCertificate(eq(BODY), eq(LOGICAL_ADDRESS1));
  }

  @Test
  public void testStoreCertificateThrowsTemporaryOnTechnicalError() {
    assertThrows(TemporaryException.class, () -> {
    // Given
    doThrow(technicalErrorException).when(moduleApi).registerCertificate(anyString(), anyString());

    // When
    certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);
      });
  }

  @Test
  public void testStoreCertificateThrowsTemporaryOnApplicationError() {
    assertThrows(TemporaryException.class, () -> {
    // Given
    doThrow(applicationErrorException)
        .when(moduleApi)
        .registerCertificate(anyString(), anyString());

    // When
    certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);
      });
  }

  @Test
  public void testStoreCertificateThrowsTemporaryOnValidationError() {
    assertThrows(TemporaryException.class, () -> {
    // Given
    doThrow(validationErrorException).when(moduleApi).registerCertificate(anyString(), anyString());

    // When
    certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);
      });
  }

  @Test
  public void testStoreCertificateThrowsTemporaryOnTransformationError() {
    assertThrows(TemporaryException.class, () -> {
    // Given
    doThrow(transformationErrorException)
        .when(moduleApi)
        .registerCertificate(anyString(), anyString());

    // When
    certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);
      });
  }

  @Test
  public void testStoreCertificateThrowsTemporaryOnModuleException() {
    assertThrows(TemporaryException.class, () -> {
    // Given
    doThrow(new ModuleException()).when(moduleApi).registerCertificate(anyString(), anyString());

    // When
    certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);
      });
  }

  @Test
  public void testStoreCertificateThrowsTemporaryOnWebServiceException() {
    assertThrows(TemporaryException.class, () -> {
    // Given
    doThrow(new WebServiceException())
        .when(moduleApi)
        .registerCertificate(anyString(), anyString());

    // When
    certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);
      });
  }

  @Test
  public void testStoreCertificateThrowsTemporaryOnException() {
    assertThrows(TemporaryException.class, () -> {
    // Given
    doThrow(new RuntimeException()).when(moduleApi).registerCertificate(anyString(), anyString());

    // When
    certificateStoreProcessor.process(BODY, "fk7263", LOGICAL_ADDRESS1);
      });
  }
}
