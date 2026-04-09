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
package se.inera.intyg.webcert.web.web.controller.testability.facade;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CertificatePatientsResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CertificateTypesResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateQuestionRequestDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateQuestionResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.util.CreateCertificateTestabilityUtil;
import se.inera.intyg.webcert.web.web.controller.testability.facade.util.CreateQuestionTestabilityUtil;
import se.inera.intyg.webcert.web.web.controller.testability.facade.util.SupportedCertificateTypesUtil;
import se.inera.intyg.webcert.web.web.controller.testability.facade.util.SupportedPatientsUtil;

@RestController
@RequestMapping("/testability/certificate")
@Profile({"dev", "testability-api"})
public class CertificateTestabilityController {

  private final CreateCertificateTestabilityUtil createCertificateTestabilityUtil;
  private final CreateQuestionTestabilityUtil createQuestionTestabilityUtil;
  private final SupportedCertificateTypesUtil supportedCertificateTypesUtil;
  private final SupportedPatientsUtil supportedPatientsUtil;

  @Autowired
  public CertificateTestabilityController(
      CreateCertificateTestabilityUtil createCertificateTestabilityUtil,
      CreateQuestionTestabilityUtil createQuestionTestabilityUtil,
      SupportedCertificateTypesUtil supportedCertificateTypesUtil,
      SupportedPatientsUtil supportedPatientsUtil) {
    this.createCertificateTestabilityUtil = createCertificateTestabilityUtil;
    this.createQuestionTestabilityUtil = createQuestionTestabilityUtil;
    this.supportedCertificateTypesUtil = supportedCertificateTypesUtil;
    this.supportedPatientsUtil = supportedPatientsUtil;
  }

  @PostMapping
  public ResponseEntity<CreateCertificateResponseDTO> createCertificate(
      @RequestBody @NotNull CreateCertificateRequestDTO createCertificateRequest) {
    final var certificateId =
        createCertificateTestabilityUtil.createNewCertificate(createCertificateRequest);
    return ResponseEntity.ok(new CreateCertificateResponseDTO(certificateId));
  }

  @PostMapping("/{certificateId}/question")
  public ResponseEntity<CreateQuestionResponseDTO> createQuestion(
      @PathVariable("certificateId") @NotNull String certificateId,
      @RequestBody @NotNull CreateQuestionRequestDTO createQuestionRequest) {
    final var questionId =
        createQuestionTestabilityUtil.createNewQuestion(certificateId, createQuestionRequest);
    return ResponseEntity.ok(new CreateQuestionResponseDTO(questionId));
  }

  @PostMapping("/{certificateId}/questionDraft")
  public ResponseEntity<CreateQuestionResponseDTO> createQuestionDraft(
      @PathVariable("certificateId") @NotNull String certificateId,
      @RequestBody @NotNull CreateQuestionRequestDTO createQuestionRequest) {
    final var questionId =
        createQuestionTestabilityUtil.createNewQuestionDraft(certificateId, createQuestionRequest);
    return ResponseEntity.ok(new CreateQuestionResponseDTO(questionId));
  }

  @GetMapping("/types")
  public ResponseEntity<CertificateTypesResponseDTO> getSupportedCerificateTypes() {
    final var certificateTypes = supportedCertificateTypesUtil.get();
    return ResponseEntity.ok(new CertificateTypesResponseDTO(certificateTypes));
  }

  @GetMapping("/patients")
  public ResponseEntity<CertificatePatientsResponseDTO> getSuppportedPatients() {
    final var patients = supportedPatientsUtil.get();
    return ResponseEntity.ok(new CertificatePatientsResponseDTO(patients));
  }
}
