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
package se.inera.intyg.webcert.web.web.controller.facade;

import jakarta.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.webcert.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.facade.ComplementCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.CopyCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFromTemplateFacadeService;
import se.inera.intyg.webcert.web.service.facade.DeleteCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ForwardCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCandidateMesssageForCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateEventsFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateResourceLinks;
import se.inera.intyg.webcert.web.service.facade.GetRelatedCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ReadyForSignFacadeService;
import se.inera.intyg.webcert.web.service.facade.RenewCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ReplaceCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.RevokeCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SaveCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SendCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SignCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.UpdateCertificateFromCandidateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ValidateCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.impl.CreateCertificateException;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateEventResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ComplementCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CopyCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateFromCandidateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateFromTemplateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ForwardCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.GetCandidateMessageForCertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.GetRelatedCertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.NewCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RenewCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ReplaceCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SendCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateCertificateResponseDTO;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

  private static final Logger LOG = LoggerFactory.getLogger(CertificateController.class);

  private static final String UTF_8_CHARSET = ";charset=utf-8";

  public static final String LAST_SAVED_DRAFT = "lastSavedDraft";

  @Autowired
  @Qualifier("getCertificateAggregator") private GetCertificateFacadeService getCertificateFacadeService;

  @Autowired
  @Qualifier("saveCertificateAggregator") private SaveCertificateFacadeService saveCertificateFacadeService;

  @Autowired
  @Qualifier("validateCertificateAggregator") private ValidateCertificateFacadeService validationCertificateFacadeService;

  @Autowired private SignCertificateFacadeService signCertificateFacadeService;

  @Autowired
  @Qualifier("deleteCertificateAggregator") private DeleteCertificateFacadeService deleteCertificateFacadeService;

  @Autowired
  @Qualifier("revokeCertificateAggregator") private RevokeCertificateFacadeService revokeCertificateFacadeService;

  @Autowired
  @Qualifier("replaceCertificateAggregator") private ReplaceCertificateFacadeService replaceCertificateFacadeService;

  @Autowired private CopyCertificateFacadeService copyCertificateFacadeService;

  @Autowired
  @Qualifier("renewCertificateAggregator") private RenewCertificateFacadeService renewCertificateFacadeService;

  @Autowired
  @Qualifier("forwardCertificateAggregator") private ForwardCertificateFacadeService forwardCertificateFacadeService;

  @Autowired private ReadyForSignFacadeService readyForSignAggregator;

  @Autowired
  @Qualifier("getCertificateEventsAggregator") private GetCertificateEventsFacadeService getCertificateEventsFacadeService;

  @Autowired private GetCertificateResourceLinks getCertificateResourceLinks;

  @Autowired
  @Qualifier("sendCertificateAggregator") private SendCertificateFacadeService sendCertificateFacadeService;

  @Autowired
  @Qualifier("complementCertificateAggregator") private ComplementCertificateFacadeService complementCertificateFacadeService;

  @Autowired
  @Qualifier("createCertificateFromTemplateAggregator") private CreateCertificateFromTemplateFacadeService createCertificateFromTemplateFacadeService;

  @Autowired
  @Qualifier("updateCertificateFromCandidateAggregator") private UpdateCertificateFromCandidateFacadeService updateCertificateFromCandidateFacadeService;

  @Autowired
  @Qualifier("createCertificateAggregator") private CreateCertificateFacadeService createCertificateFacadeService;

  @Autowired private GetRelatedCertificateFacadeService getRelatedCertificateFacadeService;

  @Autowired
  private GetCandidateMesssageForCertificateFacadeService
      getCandidateMesssageForCertificateFacadeService;

  @GetMapping("/{certificateId}")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-get-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<CertificateResponseDTO> getCertificate(
      @PathVariable("certificateId") @NotNull String certificateId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Getting certificate with id: '{}'", certificateId);
    }
    final var certificate = getCertificateFacadeService.getCertificate(certificateId, true, true);
    final var resourceLinks = getCertificateResourceLinks.get(certificate);
    final var certificateDTO = CertificateDTO.create(certificate, resourceLinks);
    return ResponseEntity.ok(CertificateResponseDTO.create(certificateDTO));
  }

  @PutMapping("/{certificateId}")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-save-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_CREATION)
  public ResponseEntity<SaveCertificateResponseDTO> saveCertificate(
      @PathVariable("certificateId") String certificateId,
      @RequestBody @NotNull Certificate certificate,
      HttpServletRequest request) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Saving certificate with id: '{}'", certificate.getMetadata().getId());
    }
    final var pdlLog = isFirstTimeSavedDuringSession(certificateId, request);
    final var version = saveCertificateFacadeService.saveCertificate(certificate, pdlLog);
    return ResponseEntity.ok(SaveCertificateResponseDTO.create(version));
  }

  private boolean isFirstTimeSavedDuringSession(String certificateId, HttpServletRequest request) {
    final var session = request.getSession(true);
    final var lastSavedDraft = (String) session.getAttribute(LAST_SAVED_DRAFT);
    session.setAttribute(LAST_SAVED_DRAFT, certificateId);
    return !certificateId.equals(lastSavedDraft);
  }

  @PostMapping("/{certificateId}/validate")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-validate-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<ValidateCertificateResponseDTO> validateCertificate(
      @PathVariable("certificateId") String certificateId,
      @RequestBody @NotNull Certificate certificate) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Validating certificate with id: '{}'", certificateId);
    }

    final var validationErrors = validationCertificateFacadeService.validate(certificate);
    return ResponseEntity.ok(ValidateCertificateResponseDTO.create(validationErrors));
  }

  @PostMapping("/{certificateId}/sign")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-sign-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<CertificateResponseDTO> signCertificate(
      @PathVariable("certificateId") String certificateId,
      @RequestBody @NotNull Certificate certificate,
      HttpServletRequest request) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Signing certificate with id: '{}'", certificateId);
    }
    final var signedCertificate =
        signCertificateFacadeService.signCertificate(certificate, request.getRemoteAddr());
    final var resourceLinks = getCertificateResourceLinks.get(signedCertificate);
    final var certificateDTO = CertificateDTO.create(signedCertificate, resourceLinks);
    return ResponseEntity.ok(CertificateResponseDTO.create(certificateDTO));
  }

  @DeleteMapping("/{certificateId}/{version}")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-delete-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_DELETION)
  public ResponseEntity<Void> deleteCertificate(
      @PathVariable("certificateId") @NotNull String certificateId,
      @PathVariable("version") @NotNull long version) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Deleting certificate with id: '{}' and version: '{}'", certificateId, version);
    }
    deleteCertificateFacadeService.deleteCertificate(certificateId, version);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{certificateId}/revoke")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-revoke-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<CertificateResponseDTO> revokeCertificate(
      @PathVariable("certificateId") @NotNull String certificateId,
      @RequestBody @NotNull RevokeCertificateRequestDTO revokeCertificate) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "Revoking certificate with id: '{}' and reason: '{}' and message: '{}'",
          certificateId,
          revokeCertificate.getReason(),
          revokeCertificate.getMessage());
    }
    final var certificate =
        revokeCertificateFacadeService.revokeCertificate(
            certificateId, revokeCertificate.getReason(), revokeCertificate.getMessage());

    final var resourceLinks = getCertificateResourceLinks.get(certificate);
    final var certificateDTO = CertificateDTO.create(certificate, resourceLinks);
    return ResponseEntity.ok(CertificateResponseDTO.create(certificateDTO));
  }

  @PostMapping("/{certificateId}/replace")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-replace-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_CREATION)
  public ResponseEntity<ReplaceCertificateResponseDTO> replaceCertificate(
      @PathVariable("certificateId") @NotNull String certificateId,
      @RequestBody(required = false) @NotNull NewCertificateRequestDTO newCertificateRequestDTO) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Replacing certificate with id: '{}'", certificateId);
    }
    final var newCertificateId = replaceCertificateFacadeService.replaceCertificate(certificateId);
    return ResponseEntity.ok(ReplaceCertificateResponseDTO.create(newCertificateId));
  }

  @PostMapping("/{certificateId}/renew")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-renew-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_CREATION)
  public ResponseEntity<RenewCertificateResponseDTO> renewCertificate(
      @PathVariable("certificateId") @NotNull String certificateId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Renewing certificate with id: '{}'", certificateId);
    }
    final var newCertificateId = renewCertificateFacadeService.renewCertificate(certificateId);
    return ResponseEntity.ok(RenewCertificateResponseDTO.create(newCertificateId));
  }

  @PostMapping("/{certificateId}/template")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-create-certificate-from-template",
      eventType = MdcLogConstants.EVENT_TYPE_CREATION)
  public ResponseEntity<CreateCertificateFromTemplateResponseDTO> createCertificateFromTemplate(
      @PathVariable("certificateId") @NotNull String certificateId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Creating draft from template with id: '{}'", certificateId);
    }
    final var newCertificateId =
        createCertificateFromTemplateFacadeService.createCertificateFromTemplate(certificateId);
    return ResponseEntity.ok(CreateCertificateFromTemplateResponseDTO.create(newCertificateId));
  }

  @PostMapping("/{certificateId}/candidate")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "update-certificate-from-candidate",
      eventType = MdcLogConstants.EVENT_TYPE_CREATION)
  public ResponseEntity<CreateCertificateFromCandidateResponseDTO> updateCertificateFromCandidate(
      @PathVariable("certificateId") @NotNull String certificateId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Filling draft of id: '{}' with candidate", certificateId);
    }
    final var newCertificateId = updateCertificateFromCandidateFacadeService.update(certificateId);
    return ResponseEntity.ok(CreateCertificateFromCandidateResponseDTO.create(newCertificateId));
  }

  @PostMapping("/{certificateId}/complement")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-complement-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<CertificateResponseDTO> complementCertificate(
      @PathVariable("certificateId") @NotNull String certificateId,
      @RequestBody @NotNull ComplementCertificateRequestDTO complementCertificateRequest) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Complementing certificate with id: '{}'", certificateId);
    }
    final var complementCertificate =
        complementCertificateFacadeService.complement(
            certificateId, complementCertificateRequest.getMessage());

    final var resourceLinks = getCertificateResourceLinks.get(complementCertificate);
    final var certificateDTO = CertificateDTO.create(complementCertificate, resourceLinks);
    return ResponseEntity.ok(CertificateResponseDTO.create(certificateDTO));
  }

  @PostMapping("/{certificateId}/answercomplement")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-answer-complement-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<CertificateResponseDTO> answerComplementCertificate(
      @PathVariable("certificateId") @NotNull String certificateId,
      @RequestBody @NotNull ComplementCertificateRequestDTO complementCertificateRequest) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Answer complement certificate with id: '{}'", certificateId);
    }
    final var answeredComplementCertificate =
        complementCertificateFacadeService.answerComplement(
            certificateId, complementCertificateRequest.getMessage());

    final var resourceLinks = getCertificateResourceLinks.get(answeredComplementCertificate);
    final var certificateDTO = CertificateDTO.create(answeredComplementCertificate, resourceLinks);
    return ResponseEntity.ok(CertificateResponseDTO.create(certificateDTO));
  }

  @PostMapping("/{certificateId}/copy")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-copy-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_CREATION)
  public ResponseEntity<CopyCertificateResponseDTO> copyCertificate(
      @PathVariable("certificateId") @NotNull String certificateId,
      @RequestBody @NotNull NewCertificateRequestDTO copyCertificate) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Copy certificate with id: '{}'", certificateId);
    }
    final var newCertificateId = copyCertificateFacadeService.copyCertificate(certificateId);
    return ResponseEntity.ok(CopyCertificateResponseDTO.create(newCertificateId));
  }

  @PostMapping("/{certificateId}/forward")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-forward-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<CertificateResponseDTO> forwardCertificate(
      @PathVariable("certificateId") @NotNull String certificateId,
      @RequestBody @NotNull ForwardCertificateRequestDTO forwardCertificate) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "Forward certificate with id: '{}' and forwarded: '{}'",
          certificateId,
          forwardCertificate.isForwarded());
    }
    final var certificate =
        forwardCertificateFacadeService.forwardCertificate(
            certificateId, forwardCertificate.isForwarded());

    final var resourceLinks = getCertificateResourceLinks.get(certificate);
    final var certificateDTO = CertificateDTO.create(certificate, resourceLinks);
    return ResponseEntity.ok(CertificateResponseDTO.create(certificateDTO));
  }

  @PostMapping("/{certificateId}/readyforsign")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-ready-for-sign",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<CertificateResponseDTO> readyForSign(
      @PathVariable("certificateId") @NotNull String certificateId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Ready for sign certificate with id: '{}' ", certificateId);
    }
    final var certificate = readyForSignAggregator.readyForSign(certificateId);

    final var resourceLinks = getCertificateResourceLinks.get(certificate);
    final var certificateDTO = CertificateDTO.create(certificate, resourceLinks);
    return ResponseEntity.ok(CertificateResponseDTO.create(certificateDTO));
  }

  @PostMapping("/{certificateId}/send")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-send-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<SendCertificateResponseDTO> sendCertificate(
      @PathVariable("certificateId") @NotNull String certificateId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Sending certificate with id: '{}'", certificateId);
    }
    final var result = sendCertificateFacadeService.sendCertificate(certificateId);
    return ResponseEntity.ok(SendCertificateResponseDTO.create(certificateId, result));
  }

  @GetMapping("/{certificateId}/events")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-get-certificate-events",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<CertificateEventResponseDTO> getCertificateEvents(
      @PathVariable("certificateId") @NotNull String certificateId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Retrieving events for certificate with id: '{}'", certificateId);
    }
    try {
      final var certificateEvents =
          getCertificateEventsFacadeService.getCertificateEvents(certificateId);
      return ResponseEntity.ok(CertificateEventResponseDTO.create(certificateEvents));
    } catch (Exception e) {
      return ResponseEntity.ok(CertificateEventResponseDTO.create(new CertificateEventDTO[0]));
    }
  }

  @PostMapping
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-create-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_CREATION)
  public ResponseEntity<CreateCertificateResponseDTO> createCertificate(
      @RequestBody @NotNull CreateCertificateRequestDTO createCertificateRequest) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Creating certificate with type: '{}'", createCertificateRequest.certificateType());
    }
    try {
      final var certificateId =
          createCertificateFacadeService.create(
              createCertificateRequest.certificateType(), createCertificateRequest.patientId());
      return ResponseEntity.ok(new CreateCertificateResponseDTO(certificateId));
    } catch (CreateCertificateException e) {
      return ResponseEntity.badRequest().body(null);
    }
  }

  @GetMapping("/{certificateId}/related")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-get-related-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<GetRelatedCertificateDTO> getRelatedCertificate(
      @PathVariable("certificateId") @NotNull String certificateId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Get related certificate for certificateId: '{}'", certificateId);
    }
    final var relatedCertificateId = getRelatedCertificateFacadeService.get(certificateId);
    return ResponseEntity.ok(GetRelatedCertificateDTO.create(relatedCertificateId));
  }

  @GetMapping("/{certificateId}/candidatemessage")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "certificate-get-candidate-message-for-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<GetCandidateMessageForCertificateDTO> getCandidateMessageForCertificate(
      @PathVariable("certificateId") @NotNull String certificateId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Get candidate message for certificateId: '{}'", certificateId);
    }
    return ResponseEntity.ok(getCandidateMesssageForCertificateFacadeService.get(certificateId));
  }
}
