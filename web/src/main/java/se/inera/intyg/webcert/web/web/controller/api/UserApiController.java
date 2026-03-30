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

import io.swagger.annotations.Api;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.webcert.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.infra.security.common.model.Feature;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignatureService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.ChangeSelectedUnitRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.WebUserFeaturesRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.WebUserPreferenceStorageRequest;

/**
 * Controller for accessing the users security context.
 *
 * @author npet
 */
@RestController
@RequestMapping("/api/anvandare")
@Api(
    value = "anvandare",
    description = "REST API för användarhantering",
    produces = "application/json")
public class UserApiController extends AbstractApiController {

  private static final Logger LOG = LoggerFactory.getLogger(UserApiController.class);

  @Autowired private AvtalService avtalService;

  @Autowired private CommonAuthoritiesResolver commonAuthoritiesResolver;

  @Autowired private DssSignatureService dssSignatureService;

  /** Retrieves the security context of the logged in user as JSON. */
  @GetMapping
  @PrometheusTimeMethod
  @PerformanceLogging(eventAction = "user-get-user", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<String> getUser() {
    WebCertUser user = getWebCertUserService().getUser();

    var valdVardenhet = user.getValdVardenhet();
    if (valdVardenhet != null) {
      user.setUseSigningService(dssSignatureService.shouldUseSigningService(valdVardenhet.getId()));
    }
    return ResponseEntity.ok(user.getAsJson());
  }

  @PutMapping("/features")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-set-user-features",
      eventType = MdcLogConstants.EVENT_TYPE_USER)
  public ResponseEntity<Map<String, Feature>> userFeatures(
      @RequestBody WebUserFeaturesRequest webUserFeaturesRequest) {
    WebCertUser user = getWebCertUserService().getUser();
    Map<String, Feature> mutFeatures = new HashMap<>(user.getFeatures());
    updateFeatures(
        webUserFeaturesRequest.isJsLoggning(),
        AuthoritiesConstants.FEATURE_JS_LOGGNING,
        mutFeatures);
    user.setFeatures(mutFeatures);
    return ResponseEntity.ok(mutFeatures);
  }

  /** Changes the selected care unit in the security context for the logged in user. */
  @PostMapping("/andraenhet")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-change-selected-unit",
      eventType = MdcLogConstants.EVENT_TYPE_USER)
  public ResponseEntity<String> changeSelectedUnitOnUser(
      @RequestBody ChangeSelectedUnitRequest request) {

    WebCertUser user = getWebCertUserService().getUser();

    LOG.debug(
        "Attempting to change selected unit for user '{}', currently selected unit is '{}'",
        user.getHsaId(),
        user.getValdVardenhet() != null ? user.getValdVardenhet().getId() : "(none)");

    boolean changeSuccess = user.changeValdVardenhet(request.getId());

    if (!changeSuccess) {
      LOG.error(
          "Unit '{}' is not present in the MIUs for user '{}'", request.getId(), user.getHsaId());
      return ResponseEntity.badRequest().body("Unit change failed");
    }

    var valdVardenhet = user.getValdVardenhet();
    if (valdVardenhet != null) {
      user.setUseSigningService(dssSignatureService.shouldUseSigningService(valdVardenhet.getId()));
    }

    user.setFeatures(
        commonAuthoritiesResolver.getFeatures(
            Arrays.asList(user.getValdVardenhet().getId(), user.getValdVardgivare().getId())));

    LOG.debug("Seleced vardenhet is now '{}'", user.getValdVardenhet().getId());

    return ResponseEntity.ok(user.getAsJson());
  }

  /** Retrieves the security context of the logged in user as JSON. */
  @PutMapping("/godkannavtal")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-approve-agreement",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<Void> godkannAvtal() {
    WebCertUser user = getWebCertUserService().getUser();
    if (user != null) {
      avtalService.approveLatestAvtal(user.getHsaId(), user.getPersonId());
      user.setUserTermsApprovedOrSubscriptionInUse(true);
    }
    return ResponseEntity.ok().build();
  }

  /** Deletes privatlakaravtal approval for the specified user. */
  @DeleteMapping("/privatlakaravtal")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-remove-agreement-approval",
      eventType = MdcLogConstants.EVENT_TYPE_DELETION)
  public ResponseEntity<Void> taBortAvtalsGodkannande() {
    WebCertUser user = getWebCertUserService().getUser();
    if (user != null) {
      avtalService.removeApproval(user.getHsaId());
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
  }

  @GetMapping("/latestavtal")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-get-agreement",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Avtal> getAvtal() {
    Optional<Avtal> avtal = avtalService.getLatestAvtal();
    return ResponseEntity.ok(avtal.orElse(null));
  }

  @GetMapping("/ping")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-client-ping",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Void> clientPing() {
    // Any active user session will be extended just by accessing an endpoint.
    LOG.debug("wc-client pinged server");
    return ResponseEntity.ok().build();
  }

  @PutMapping("/preferences")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-store-metadata-entry",
      eventType = MdcLogConstants.EVENT_TYPE_USER)
  public ResponseEntity<Void> storeUserMetdataEntry(
      @RequestBody WebUserPreferenceStorageRequest request) {
    LOG.debug("User stored user preference entry for key: " + request.getKey());
    getWebCertUserService().storeUserPreference(request.getKey(), request.getValue());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/preferences/{key}")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-delete-user-preference-entry",
      eventType = MdcLogConstants.EVENT_TYPE_USER)
  public ResponseEntity<Void> deleteUserPreferenceEntry(@PathVariable("key") String prefKey) {
    LOG.debug("User deleted user preference entry for key: " + prefKey);
    getWebCertUserService().deleteUserPreference(prefKey);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/logout")
  @PrometheusTimeMethod
  @PerformanceLogging(eventAction = "user-logout", eventType = MdcLogConstants.EVENT_TYPE_USER)
  public ResponseEntity<Void> logoutUserAfterTimeout(HttpServletRequest request) {
    HttpSession session = request.getSession();

    getWebCertUserService().scheduleSessionRemoval(session);

    return ResponseEntity.ok().build();
  }

  @GetMapping("/logout/cancel")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-cancel-logout",
      eventType = MdcLogConstants.EVENT_TYPE_USER)
  public ResponseEntity<Void> cancelLogout(HttpServletRequest request) {
    HttpSession session = request.getSession();

    getWebCertUserService().cancelScheduledLogout(session);

    return ResponseEntity.ok().build();
  }

  private void updateFeatures(boolean active, String name, Map<String, Feature> features) {
    if (active) {
      Feature feature = new Feature();
      feature.setName(name);
      feature.setGlobal(true);
      features.put(name, feature);
    } else {
      features.remove(name);
    }
  }
}
