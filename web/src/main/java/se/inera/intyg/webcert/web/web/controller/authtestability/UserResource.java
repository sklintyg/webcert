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
package se.inera.intyg.webcert.web.web.controller.authtestability;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.infra.security.common.model.Feature;
import se.inera.intyg.webcert.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

/**
 * Rest interface only used for testing and in dev environments. It seems like it must be in the
 * same Spring context as the rest of the webservices to get access to the security context.
 */
@RestController
@RequestMapping("/authtestability/user")
@Profile("!prod")
public class UserResource {

  private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

  @Autowired private WebCertUserService webCertUserService;

  @GetMapping("/role")
  @JsonPropertyDescription("Get the roles for user in session")
  @PrometheusTimeMethod
  public ResponseEntity<Set<String>> getUserRoles() {
    final WebCertUser user = webCertUserService.getUser();
    final Map<String, Role> roles = user.getRoles();
    final Set<String> roleStrings = roles.keySet();
    return ResponseEntity.ok(roleStrings);
  }

  /**
   * Set the role for current user. Using a GET to change a state might not be recommended. However,
   * it is a very convenient way to change the user role from the browser and it is also the only
   * way I could figure out to invoke it from the browser session in the Fitnesse tests.
   */
  @GetMapping("/role/{role}")
  @JsonPropertyDescription("Set the roles for user in session")
  @PrometheusTimeMethod
  public ResponseEntity<Void> setUserRole(@PathVariable("role") String role) {
    webCertUserService.updateUserRole(role);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/origin")
  @PrometheusTimeMethod
  public ResponseEntity<String> getOrigin() {
    final WebCertUser user = webCertUserService.getUser();
    final String currentOrigin = user.getOrigin();
    return ResponseEntity.ok(currentOrigin);
  }

  /**
   * Set current user's request origin. Using a GET to change a state might not be recommended.
   * However, it is a very convenient way to change the request origin from the browser and it is
   * also the only way I could figure out to invoke it from the browser session in the Fitnesse
   * tests.
   */
  @GetMapping("/origin/{origin}")
  @PrometheusTimeMethod
  public ResponseEntity<Void> setOrigin(@PathVariable("origin") String origin) {
    webCertUserService.updateOrigin(origin);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/preferences/delete")
  @PrometheusTimeMethod
  public ResponseEntity<Void> deleteUserPreferences() {
    webCertUserService.deleteUserPreferences();
    return ResponseEntity.ok().build();
  }

  @GetMapping("/preferences")
  @PrometheusTimeMethod
  public ResponseEntity<Object> getUserPreferences() {
    final var prefs = webCertUserService.getUser().getAnvandarPreference();
    return ResponseEntity.ok(prefs);
  }

  @GetMapping("/parameters")
  @PrometheusTimeMethod
  public ResponseEntity<IntegrationParameters> getParameters() {
    return ResponseEntity.ok(webCertUserService.getUser().getParameters());
  }

  @PostMapping("/parameters/sjf")
  @PrometheusTimeMethod
  public ResponseEntity<Void> setSjf() {
    webCertUserService
        .getUser()
        .setParameters(
            new IntegrationParameters(
                null, null, null, null, null, null, null, null, null, true, false, false, true,
                null));
    return ResponseEntity.ok().build();
  }

  /**
   * Use this endpoint to specify a "reference" DJUPINTEGRATION parameter in tests.
   *
   * @param refValue Whatever string you want to specifiy as reference.
   * @return 200 OK unless there's a problem.
   */
  @GetMapping("/parameters/ref/{refValue}")
  @PrometheusTimeMethod
  public ResponseEntity<Void> setRef(@PathVariable("refValue") String refValue) {
    webCertUserService
        .getUser()
        .setParameters(
            new IntegrationParameters(
                refValue, null, null, null, null, null, null, null, null, true, false, false, true,
                null));
    return ResponseEntity.ok().build();
  }

  @PutMapping("/parameters/launchId/{launchId}")
  @PrometheusTimeMethod
  public ResponseEntity<Void> setLaunchId(@PathVariable("launchId") String launchId) {
    webCertUserService
        .getUser()
        .setParameters(
            new IntegrationParameters(
                null, null, null, null, null, null, null, null, null, true, false, false, true,
                launchId));
    return ResponseEntity.ok().build();
  }

  @GetMapping("/features")
  @PrometheusTimeMethod
  public ResponseEntity<Map<String, Feature>> getFeaturesForUser() {
    Map<String, Feature> features = webCertUserService.getUser().getFeatures();
    return ResponseEntity.ok(features);
  }

  @PutMapping("/personid")
  @PrometheusTimeMethod
  public ResponseEntity<Void> getFeaturesForUser(@RequestBody String personId) {
    String oldPersonId = webCertUserService.getUser().getPersonId();
    webCertUserService.getUser().setPersonId(personId);
    LOG.info("Changed user 'personId' to '{}', was '{}'.", personId, oldPersonId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/subscriptionInfo")
  @PrometheusTimeMethod
  public ResponseEntity<Object> getSubscriptionInfo() {
    final var info = webCertUserService.getUser().getSubscriptionInfo();
    return ResponseEntity.ok(info);
  }
}
