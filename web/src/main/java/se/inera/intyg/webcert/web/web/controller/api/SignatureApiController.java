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
package se.inera.intyg.webcert.web.web.controller.api;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssMetadataService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignMessageService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignRequestDTO;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.grp.QRCodeService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.SignaturStateDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.SignaturStateDTO.SignaturStateDTOBuilder;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;

@RestController
@RequestMapping("/api/signature")
public class SignatureApiController extends AbstractApiController {

  public static final String SIGNATUR_API_CONTEXT_PATH = "/api/signature";
  public static final String SIGN_SERVICE_RESPONSE_PATH = "/signservice/v1/response";
  public static final String SIGN_SERVICE_METADATA_PATH = "/signservice/v1/metadata";

  @Autowired private ReactUriFactory reactUriFactory;

  @Autowired
  @Qualifier("signAggregator") private UnderskriftService underskriftService;

  @Autowired private MonitoringLogService monitoringLogService;

  @Autowired private DssMetadataService dssMetadataService;

  @Autowired private DssSignatureService dssSignatureService;

  @Autowired private DssSignMessageService dssSignMessageService;

  @Autowired private QRCodeService qrCodeService;

  @PostMapping("/{intygsTyp}/{intygsId}/{version}/signeringshash/{signMethod}")
  @PerformanceLogging(
      eventAction = "signature-sign-draft",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public SignaturStateDTO signeraUtkast(
      @PathVariable("intygsTyp") String intygsTyp,
      @PathVariable("intygsId") String intygsId,
      @PathVariable("version") long version,
      @PathVariable("signMethod") String signMethodStr,
      HttpServletRequest request) {

    SignMethod signMethod;
    try {
      signMethod = SignMethod.valueOf(signMethodStr);
    } catch (IllegalArgumentException e) {
      throw new WebCertServiceException(
          WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
          "Parameter signMethod is missing or has illegal value. Allowed values are: "
              + Arrays.stream(SignMethod.values())
                  .map(SignMethod::name)
                  .collect(Collectors.joining(", ")));
    }

    try {
      String ticketId;

      if (SignMethod.SIGN_SERVICE.equals(signMethod)) {
        ticketId = dssSignatureService.createTransactionID();
      } else {
        ticketId = UUID.randomUUID().toString();
      }

      SignaturBiljett sb =
          underskriftService.startSigningProcess(
              intygsId, intygsTyp, version, signMethod, ticketId, request.getRemoteAddr());

      if (SignMethod.SIGN_SERVICE.equals(signMethod)) {
        DssSignRequestDTO signRequestDTO = dssSignatureService.createSignatureRequestDTO(sb);

        monitoringLogService.logSignRequestCreated(signRequestDTO.getTransactionId(), intygsId);

        return SignaturStateDTOBuilder.aSignaturStateDTO()
            .withId(signRequestDTO.getTransactionId())
            .withActionUrl(signRequestDTO.getActionUrl())
            .withSignRequest(signRequestDTO.getSignRequest())
            .build();

      } else {
        return convertToSignatureStateDTO(sb);
      }
    } catch (OptimisticLockException | OptimisticLockingFailureException e) {
      monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
      throw new WebCertServiceException(
          WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
    }
  }

  @PostMapping(SIGN_SERVICE_RESPONSE_PATH)
  @PerformanceLogging(
      eventAction = "signature-sign-service-response",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Void> signServiceResponse(
      HttpServletRequest request,
      @RequestParam("RelayState") String relayState,
      @RequestParam("EidSignResponse") String eidSignResponse) {
    SignaturBiljett signaturBiljett;
    monitoringLogService.logSignResponseReceived(relayState);

    String signResponseString = "";
    try {
      signResponseString =
          new String(Base64.getDecoder().decode(eidSignResponse), StandardCharsets.UTF_8);
    } catch (Exception e) {
      signaturBiljett = dssSignatureService.updateSignatureTicketWithError(relayState);
      monitoringLogService.logSignResponseInvalid(
          relayState,
          signaturBiljett.getIntygsId(),
          "Could not decode sign response: " + e.getMessage());
      return getRedirectResponseWithReturnUrl(signaturBiljett, request);
    }

    var validationResponse = dssSignMessageService.validateDssMessageSignature(signResponseString);

    if (!validationResponse.isValid()) {
      signaturBiljett = dssSignatureService.updateSignatureTicketWithError(relayState);
      monitoringLogService.logSignResponseInvalid(
          relayState,
          signaturBiljett.getIntygsId(),
          "Validation of sign response signature failed!");
      return getRedirectResponseWithReturnUrl(signaturBiljett, request);
    }

    signaturBiljett = dssSignatureService.receiveSignResponse(relayState, signResponseString);

    logIfSuccess(relayState, signaturBiljett);

    return getRedirectResponseWithReturnUrl(signaturBiljett, request);
  }

  private void logIfSuccess(String relayState, SignaturBiljett signaturBiljett) {
    if (signaturBiljett.getStatus() == SignaturStatus.SIGNERAD) {
      monitoringLogService.logSignResponseSuccess(relayState, signaturBiljett.getIntygsId());
    }
  }

  private ResponseEntity<Void> getRedirectResponseWithReturnUrl(
      SignaturBiljett signaturBiljett, HttpServletRequest request) {
    final var redirectUri = getRedirectUri(signaturBiljett, request);
    return ResponseEntity.status(HttpStatus.SEE_OTHER).location(redirectUri).build();
  }

  private URI getRedirectUri(SignaturBiljett signaturBiljett, HttpServletRequest request) {
    return signaturBiljett.getStatus().equals(SignaturStatus.ERROR)
        ? reactUriFactory.uriForCertificateWithSignError(
            request, signaturBiljett.getIntygsId(), signaturBiljett.getStatus())
        : reactUriFactory.uriForCertificate(request, signaturBiljett.getIntygsId());
  }

  private SignaturStateDTO convertToSignatureStateDTO(SignaturBiljett sb) {
    return SignaturStateDTO.SignaturStateDTOBuilder.aSignaturStateDTO()
        .withId(sb.getTicketId())
        .withIntygsId(sb.getIntygsId())
        .withStatus(sb.getStatus())
        .withVersion(sb.getVersion())
        .withSignaturTyp(sb.getSignaturTyp())
        .withAutoStartToken(sb.getAutoStartToken())
        .withQrCode(qrCodeService.qrCodeForBankId(sb))
        .withHash(sb.getHash()) // This is what you stuff into NetiD SIGN.
        .build();
  }

  @GetMapping(SIGN_SERVICE_METADATA_PATH)
  @PerformanceLogging(
      eventAction = "signature-sign-service-client-metadata",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<String> signServiceClientMetadata() {

    String clientMetadataAsString = dssMetadataService.getClientMetadataAsString();

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/samlmetadata+xml"))
        .header(
            HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"wc_dss_client_metadata.xml\"")
        .body(clientMetadataAsString);
  }

  @GetMapping("/{intygsTyp}/{ticketId}/signeringsstatus")
  @PerformanceLogging(
      eventAction = "signature-sign-status",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public SignaturStateDTO signeringsStatus(
      @PathVariable("intygsTyp") String intygsTyp, @PathVariable("ticketId") String ticketId) {
    final var sb = underskriftService.signeringsStatus(ticketId);
    return convertToSignatureStateDTO(sb);
  }
}
