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
package se.inera.intyg.webcert.web.service.intyg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderException;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;

@ExtendWith(MockitoExtension.class)
class IntygServiceStoreTest extends AbstractIntygServiceTest {

  @BeforeEach
  @Override
  void setupMocks() throws Exception {
    json =
        Files.readString(
            Path.of(ClassLoader.getSystemResource("IntygServiceTest/utlatande.json").toURI()));
    utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);
    CertificateMetaData metaData = buildCertificateMetaData();
    certificateResponse = new CertificateResponse(json, utlatande, metaData, false);
  }

  @Test
  void testStoreIntyg() throws Exception {

    IntygServiceResult res = intygService.storeIntyg(createUtkast());
    assertEquals(IntygServiceResult.OK, res);

    verify(certificateSenderService, times(1)).storeCertificate(INTYG_ID, INTYG_TYP_FK, json);
    verify(monitoringService).logIntygRegistered(INTYG_ID, INTYG_TYP_FK);
  }

  @Test
  void testStoreIntygThrowsCertificateSenderException() {
    assertThrows(
        WebCertServiceException.class,
        () -> {
          doThrow(new CertificateSenderException(""))
              .when(certificateSenderService)
              .storeCertificate(eq(INTYG_ID), eq(INTYG_TYP_FK), anyString());
          intygService.storeIntyg(createUtkast());
        });
  }

  private Utkast createUtkast() {
    Utkast utkast = new Utkast();
    utkast.setIntygsId(INTYG_ID);
    utkast.setIntygsTyp(INTYG_TYP_FK);
    utkast.setStatus(UtkastStatus.SIGNED);
    utkast.setModel(json);
    return utkast;
  }
}
