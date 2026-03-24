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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.xml.ws.WebServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ExternalServiceCallException;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.logging.MdcHelper;

@ExtendWith(MockitoExtension.class)
public class CertificateRevokeProcessorTest {

  private static final String BODY = "body";
  private static final String INTYGS_ID1 = "intygs-id-1";
  private static final String LOGICAL_ADDRESS1 = "logicalAddress1";
  private static final String INTYGS_TYP = "fk7263";
  private static final String INTYGS_TYP_VERSION = "1.0";

  @Mock private IntygModuleRegistry registry;
  @Spy private MdcHelper mdcHelper;

  @InjectMocks
  CertificateRevokeProcessor certificateRevokeProcessor = new CertificateRevokeProcessor();

  @Test
  public void testRevokeCertificate() throws Exception {
    ModuleApi moduleApi = mock(ModuleApi.class);
    when(registry.getModuleApi(eq(INTYGS_TYP), eq(INTYGS_TYP_VERSION))).thenReturn(moduleApi);

    certificateRevokeProcessor.process(
        BODY, INTYGS_ID1, LOGICAL_ADDRESS1, INTYGS_TYP, INTYGS_TYP_VERSION);

    verify(moduleApi).revokeCertificate(eq(BODY), eq(LOGICAL_ADDRESS1));
  }

  @Test
  public void testRevokeCertificateWhenWebServiceExceptionIsThrown() {
    assertThrows(TemporaryException.class, () -> {
    ModuleApi moduleApi = mock(ModuleApi.class);
    when(registry.getModuleApi(eq(INTYGS_TYP), eq(INTYGS_TYP_VERSION))).thenReturn(moduleApi);
    doThrow(new WebServiceException())
        .when(moduleApi)
        .revokeCertificate(eq(BODY), eq(LOGICAL_ADDRESS1));

    certificateRevokeProcessor.process(
        BODY, INTYGS_ID1, LOGICAL_ADDRESS1, INTYGS_TYP, INTYGS_TYP_VERSION);
      });
  }

  @Test
  public void testRevokeCertificateOnApplicationErrorResponse() {
    assertThrows(TemporaryException.class, () -> {
    ModuleApi moduleApi = mock(ModuleApi.class);
    when(registry.getModuleApi(eq(INTYGS_TYP), eq(INTYGS_TYP_VERSION))).thenReturn(moduleApi);
    doThrow(
            new ExternalServiceCallException(
                "message", ExternalServiceCallException.ErrorIdEnum.APPLICATION_ERROR))
        .when(moduleApi)
        .revokeCertificate(eq(BODY), eq(LOGICAL_ADDRESS1));

    certificateRevokeProcessor.process(
        BODY, INTYGS_ID1, LOGICAL_ADDRESS1, INTYGS_TYP, INTYGS_TYP_VERSION);
      });
  }

  @Test
  public void testRevokeCertificateOnTechnicalErrorResponse() {
    assertThrows(TemporaryException.class, () -> {
    ModuleApi moduleApi = mock(ModuleApi.class);
    when(registry.getModuleApi(eq(INTYGS_TYP), eq(INTYGS_TYP_VERSION))).thenReturn(moduleApi);
    doThrow(
            new ExternalServiceCallException(
                "message", ExternalServiceCallException.ErrorIdEnum.TECHNICAL_ERROR))
        .when(moduleApi)
        .revokeCertificate(eq(BODY), eq(LOGICAL_ADDRESS1));

    certificateRevokeProcessor.process(
        BODY, INTYGS_ID1, LOGICAL_ADDRESS1, INTYGS_TYP, INTYGS_TYP_VERSION);
      });
  }

  @Test
  public void testRevokeCertificateOnValidationErrorResponse() {
    assertThrows(TemporaryException.class, () -> {
    ModuleApi moduleApi = mock(ModuleApi.class);
    when(registry.getModuleApi(eq(INTYGS_TYP), eq(INTYGS_TYP_VERSION))).thenReturn(moduleApi);
    doThrow(
            new ExternalServiceCallException(
                "message", ExternalServiceCallException.ErrorIdEnum.VALIDATION_ERROR))
        .when(moduleApi)
        .revokeCertificate(eq(BODY), eq(LOGICAL_ADDRESS1));

    certificateRevokeProcessor.process(
        BODY, INTYGS_ID1, LOGICAL_ADDRESS1, INTYGS_TYP, INTYGS_TYP_VERSION);
      });
  }

  @Test
  public void testRevokeCertificateOnTransformationErrorResponse() {
    assertThrows(TemporaryException.class, () -> {
    ModuleApi moduleApi = mock(ModuleApi.class);
    when(registry.getModuleApi(eq(INTYGS_TYP), eq(INTYGS_TYP_VERSION))).thenReturn(moduleApi);
    doThrow(
            new ExternalServiceCallException(
                "message", ExternalServiceCallException.ErrorIdEnum.TRANSFORMATION_ERROR))
        .when(moduleApi)
        .revokeCertificate(eq(BODY), eq(LOGICAL_ADDRESS1));

    certificateRevokeProcessor.process(
        BODY, INTYGS_ID1, LOGICAL_ADDRESS1, INTYGS_TYP, INTYGS_TYP_VERSION);
      });
  }

  @Test
  public void testIntygsIdIsMissing() throws Exception {
    try {
      certificateRevokeProcessor.process(
          BODY, null, LOGICAL_ADDRESS1, INTYGS_TYP, INTYGS_TYP_VERSION);
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains(Constants.INTYGS_ID));
    }
  }

  @Test
  public void testIntygsTypVersionIsMissing() throws Exception {
    try {
      certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1, INTYGS_TYP, null);
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains(Constants.INTYGS_TYP_VERSION));
    }
  }

  @Test
  public void testLogicalAddressIsMissing() {
    assertThrows(IllegalArgumentException.class, () -> {
    certificateRevokeProcessor.process(BODY, INTYGS_ID1, null, INTYGS_TYP, INTYGS_TYP_VERSION);
      });
  }
}
