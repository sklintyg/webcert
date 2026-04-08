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
package se.inera.intyg.webcert.web.web.controller.legacyintegration;

import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.webcert.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.csintegration.aggregate.GetIssuingUnitIdAggregator;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;
import se.inera.intyg.webcert.web.web.controller.integration.BaseIntegrationController;

/**
 * Controller to enable an external user to access certificates directly from a link in an external
 * patient care system.
 *
 * @author nikpet
 */
@Controller
@RequestMapping("/webcert/web/user/certificate")
public class FragaSvarUthoppController extends BaseIntegrationController {

  private static final Logger LOG = LoggerFactory.getLogger(FragaSvarUthoppController.class);

  private static final String[] GRANTED_ROLES =
      new String[] {
        AuthoritiesConstants.ROLE_ADMIN,
        AuthoritiesConstants.ROLE_LAKARE,
        AuthoritiesConstants.ROLE_TANDLAKARE,
        AuthoritiesConstants.ROLE_SJUKSKOTERSKA,
        AuthoritiesConstants.ROLE_BARNMORSKA
      };
  private static final UserOriginType GRANTED_ORIGIN = UserOriginType.NORMAL;

  @Autowired private GetIssuingUnitIdAggregator getIssuingUnitIdAggregator;

  @Autowired private ReactUriFactory reactUriFactory;

  @Autowired private CommonAuthoritiesResolver commonAuthoritiesResolver;

  /**
   * Fetches a certificate from IT and then performs a redirect to the view that displays the
   * certificate. Can be used for all types of certificates.
   *
   * @param intygId The id of the certificate to view.
   */
  @GetMapping("/{type}/{intygId}/questions")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "fragasvar-redirect-to-certificate-with-type",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Void> redirectToIntyg(
      HttpServletRequest request,
      @PathVariable("type") String type,
      @PathVariable("intygId") String intygId,
      @RequestParam(value = "enhet", required = false) String enhetHsaId) {

    super.validateParameter("type", type);
    super.validateParameter("intygId", intygId);
    super.validateAuthorities();
    this.validateAndChangeEnhet(intygId, enhetHsaId);

    LOG.debug("Redirecting to view intyg {} of type {}", intygId, type);
    return buildRedirectResponse(request, intygId);
  }

  @GetMapping("/{intygId}/questions")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "fragasvar-redirect-to-certificate-without-type",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Void> redirectToIntyg(
      HttpServletRequest request,
      @PathVariable("intygId") String intygId,
      @RequestParam(value = "enhet", required = false) String enhetHsaId) {

    super.validateParameter("intygId", intygId);
    super.validateAuthorities();
    this.validateAndChangeEnhet(intygId, enhetHsaId);

    LOG.debug("Redirecting to view intyg {}", intygId);
    return buildRedirectResponse(request, intygId);
  }

  @Override
  protected String[] getGrantedRoles() {
    return GRANTED_ROLES;
  }

  @Override
  protected UserOriginType getGrantedRequestOrigin() {
    return GRANTED_ORIGIN;
  }

  /**
   * Makes sure we change (if possible) the current vardEnhet to the one either specified in the URL
   * or to the one the intyg was issued on.
   */
  private void validateAndChangeEnhet(String intygsId, String enhetHsaId) {
    WebCertUser user = webCertUserService.getUser();
    if (user == null) {
      LOG.error("No user in session, cannot continue");
      throw new WebCertServiceException(
          WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
          "No user session, cannot view questions for intyg " + intygsId);
    }

    if (!Strings.nullToEmpty(enhetHsaId).trim().isEmpty()) {
      // Link contained not empty ?enhet= query param, try to set on user!
      if (!user.changeValdVardenhet(enhetHsaId)) {
        throw new WebCertServiceException(
            WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
            "User does not have access to enhet " + enhetHsaId);
      }
    } else {
      // No enhet on link (legacy fallback for pre WC 5.0 links)
      String enhet = getIssuingUnitIdAggregator.get(intygsId);
      if (!user.changeValdVardenhet(enhet)) {
        throw new WebCertServiceException(
            WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
            "User does not have access to enhet " + enhetHsaId);
      }
    }

    user.setFeatures(
        commonAuthoritiesResolver.getFeatures(
            Arrays.asList(user.getValdVardenhet().getId(), user.getValdVardgivare().getId())));
  }

  private ResponseEntity<Void> buildRedirectResponse(
      HttpServletRequest request, String certificateId) {
    final var uri = reactUriFactory.uriForCertificate(request, certificateId);
    return ResponseEntity.status(HttpStatus.SEE_OTHER).location(uri).build();
  }
}
