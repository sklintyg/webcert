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
package se.inera.intyg.webcert.web.service.facade.internalapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.PdfResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.BinaryCertificateMetadataConverter;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.BinaryCertificateMetadataDTO;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

@ExtendWith(MockitoExtension.class)
class GetBinaryCertificateFromWCTest {

  private static final String CERTIFICATE_ID = "e5f6a1b2-3c4d-4e5f-8a1b-2c3d4e5f6a1b";
  private static final String CERTIFICATE_TYPE = "lisjp";
  private static final String CERTIFICATE_TYPE_VERSION = "1.3";
  private static final String CONTENTS = "{\"id\":\"" + CERTIFICATE_ID + "\"}";
  private static final String PARENT_CERTIFICATE_ID = "parent-cert-id";
  private static final List<Status> STATUSES =
      List.of(new Status(CertificateState.RECEIVED, "FKASSA", LocalDateTime.now()));
  private static final byte[] PDF_BYTES = new byte[] {1, 2, 3, 4};
  private static final PdfResponse PDF_RESPONSE = new PdfResponse(PDF_BYTES, "fileName");
  private static final BinaryCertificateMetadataDTO METADATA =
      BinaryCertificateMetadataDTO.builder().certificateId(CERTIFICATE_ID).build();

  @Mock private IntygModuleRegistry moduleRegistry;
  @Mock private ModuleApi moduleApi;
  @Mock private ModuleEntryPoint entryPoint;
  @Mock private IntygService intygService;
  @Mock private BinaryCertificateMetadataConverter binaryCertificateMetadataConverter;

  @InjectMocks private GetBinaryCertificateFromWC getBinaryCertificateFromWC;

  private IntygContentHolder content;

  @BeforeEach
  void setUp() {
    content = contentHolder(new Relations(), STATUSES);
  }

  private IntygContentHolder contentHolder(Relations relations, List<Status> statuses) {
    return IntygContentHolder.builder()
        .utlatande(utlatande())
        .contents(CONTENTS)
        .statuses(statuses)
        .relations(relations)
        .build();
  }

  private static Utlatande utlatande() {
    return new Utlatande() {
      @Override
      public String getId() {
        return CERTIFICATE_ID;
      }

      @Override
      public String getTyp() {
        return CERTIFICATE_TYPE;
      }

      @Override
      public GrundData getGrundData() {
        return null;
      }

      @Override
      public String getTextVersion() {
        return CERTIFICATE_TYPE_VERSION;
      }

      @Override
      public String getSignature() {
        return null;
      }
    };
  }

  private void mockContent() {
    when(intygService.fetchIntygDataForInternalUse(CERTIFICATE_ID, true)).thenReturn(content);
  }

  private void mockModuleEntryPoint() throws ModuleNotFoundException {
    when(moduleRegistry.getModuleEntryPoint(CERTIFICATE_TYPE)).thenReturn(entryPoint);
  }

  private void mockModuleApi() throws ModuleNotFoundException {
    when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION))
        .thenReturn(moduleApi);
  }

  private void mockPdf() throws ModuleException {
    when(moduleApi.pdf(CONTENTS, STATUSES, ApplicationOrigin.WEBCERT, UtkastStatus.SIGNED))
        .thenReturn(PDF_RESPONSE);
  }

  private void mockMetadata(IntygContentHolder parentContent) {
    when(binaryCertificateMetadataConverter.toBinaryCertificate(content, entryPoint, parentContent))
        .thenReturn(METADATA);
  }

  private void mockHappyPath() throws ModuleException, ModuleNotFoundException {
    mockContent();
    mockModuleEntryPoint();
    mockModuleApi();
    mockPdf();
    mockMetadata(null);
  }

  @Test
  void shouldReturnPdfDataFromModuleApi() throws Exception {
    mockHappyPath();

    final var response = getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    assertEquals(PDF_BYTES, response.getPdfData());
  }

  @Test
  void shouldReturnMetadataFromConverter() throws Exception {
    mockHappyPath();

    final var response = getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    assertEquals(METADATA, response.getMetadata());
  }

  @Test
  void shouldUseCertificateTypeWhenGettingModuleEntryPoint() throws Exception {
    mockHappyPath();

    getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    verify(moduleRegistry).getModuleEntryPoint(CERTIFICATE_TYPE);
  }

  @Test
  void shouldUseCertificateTypeAndVersionWhenGettingModuleApi() throws Exception {
    mockHappyPath();

    getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    verify(moduleRegistry).getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
  }

  @Test
  void shouldUseContentsStatusesAndSignedStatusWhenGeneratingPdf() throws Exception {
    mockHappyPath();

    getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    verify(moduleApi).pdf(CONTENTS, STATUSES, ApplicationOrigin.WEBCERT, UtkastStatus.SIGNED);
  }

  @Test
  void shouldPassContentEntryPointAndNullParentToConverterWhenNoParentRelationExists()
      throws Exception {
    mockHappyPath();

    getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    verify(binaryCertificateMetadataConverter).toBinaryCertificate(content, entryPoint, null);
  }

  @Test
  void shouldNotFetchParentCertificateWhenParentRelationIsNull() throws Exception {
    mockHappyPath();

    getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    verify(intygService, never()).fetchIntygDataForInternalUse(anyString(), eq(false));
  }

  @Test
  void shouldFetchParentCertificateWhenParentRelationExists() throws Exception {
    final var relations = new Relations();
    final var parentRelation = new WebcertCertificateRelation();
    parentRelation.setIntygsId(PARENT_CERTIFICATE_ID);
    relations.setParent(parentRelation);
    content = contentHolder(relations, STATUSES);
    final var parentContent = contentHolder(new Relations(), STATUSES);

    mockContent();
    mockModuleEntryPoint();
    mockModuleApi();
    mockPdf();
    when(intygService.fetchIntygDataForInternalUse(PARENT_CERTIFICATE_ID, false))
        .thenReturn(parentContent);
    mockMetadata(parentContent);

    getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    verify(intygService).fetchIntygDataForInternalUse(PARENT_CERTIFICATE_ID, false);
  }

  @Test
  void shouldPassParentCertificateToConverterWhenParentRelationExists() throws Exception {
    final var relations = new Relations();
    final var parentRelation = new WebcertCertificateRelation();
    parentRelation.setIntygsId(PARENT_CERTIFICATE_ID);
    relations.setParent(parentRelation);
    content = contentHolder(relations, STATUSES);
    final var parentContent = contentHolder(new Relations(), STATUSES);

    mockContent();
    mockModuleEntryPoint();
    mockModuleApi();
    mockPdf();
    when(intygService.fetchIntygDataForInternalUse(PARENT_CERTIFICATE_ID, false))
        .thenReturn(parentContent);
    mockMetadata(parentContent);

    getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    verify(binaryCertificateMetadataConverter)
        .toBinaryCertificate(content, entryPoint, parentContent);
  }

  @Test
  void shouldReturnNullWhenContentIsNull() {
    when(intygService.fetchIntygDataForInternalUse(CERTIFICATE_ID, true)).thenReturn(null);

    assertThrows(
        WebCertServiceException.class, () -> getBinaryCertificateFromWC.get(CERTIFICATE_ID));
  }

  @Test
  void shouldReturnNullWhenStatusesAreEmpty() {
    content = contentHolder(new Relations(), List.of());
    mockContent();

    assertThrows(
        WebCertServiceException.class, () -> getBinaryCertificateFromWC.get(CERTIFICATE_ID));
  }

  @Test
  void shouldReturnNullWhenModuleEntryPointIsNotFound() throws ModuleNotFoundException {
    mockContent();
    when(moduleRegistry.getModuleEntryPoint(CERTIFICATE_TYPE))
        .thenThrow(new ModuleNotFoundException("Module not found"));

    assertThrows(
        WebCertServiceException.class, () -> getBinaryCertificateFromWC.get(CERTIFICATE_ID));
  }

  @Test
  void shouldReturnNullWhenModuleApiIsNotFoundForPdf() throws ModuleNotFoundException {
    mockContent();
    when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION))
        .thenThrow(new ModuleNotFoundException("Module not found"));

    assertThrows(
        WebCertServiceException.class, () -> getBinaryCertificateFromWC.get(CERTIFICATE_ID));
  }

  @Test
  void shouldReturnNullWhenModuleApiThrowsModuleExceptionDuringPdfGeneration() throws Exception {
    mockContent();
    mockModuleApi();
    when(moduleApi.pdf(CONTENTS, STATUSES, ApplicationOrigin.WEBCERT, UtkastStatus.SIGNED))
        .thenThrow(new ModuleException("Failed to generate pdf", null));

    assertThrows(
        WebCertServiceException.class, () -> getBinaryCertificateFromWC.get(CERTIFICATE_ID));
  }

  @Test
  void shouldNotFetchParentCertificateWhenRelationsIsNull() throws Exception {
    content = contentHolder(null, STATUSES);
    mockContent();
    mockModuleEntryPoint();
    mockModuleApi();
    mockPdf();
    mockMetadata(null);

    getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    verify(intygService, never()).fetchIntygDataForInternalUse(anyString(), eq(false));
  }

  @Test
  void shouldReturnPdfDataWhenRelationsIsNull() throws Exception {
    content = contentHolder(null, STATUSES);
    mockContent();
    mockModuleEntryPoint();
    mockModuleApi();
    mockPdf();
    mockMetadata(null);

    final var response = getBinaryCertificateFromWC.get(CERTIFICATE_ID);

    assertEquals(PDF_BYTES, response.getPdfData());
  }
}
