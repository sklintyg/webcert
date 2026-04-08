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

import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.facade.ChangeUnitService;
import se.inera.intyg.webcert.web.service.facade.GetUserResourceLinks;
import se.inera.intyg.webcert.web.service.facade.impl.ChangeUnitException;
import se.inera.intyg.webcert.web.service.facade.user.UserService;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsDTO;
import se.inera.intyg.webcert.web.service.facade.user.UserStatisticsService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.UserResponseDTO;

@RestController
@RequestMapping("/api/user")
public class UserController {

  private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

  private static final String UTF_8_CHARSET = ";charset=utf-8";

  private final UserService userService;

  private final GetUserResourceLinks getUserResourceLinks;

  private final WebCertUserService webCertUserService;

  private final UserStatisticsService userStatisticsService;
  private final ChangeUnitService changeUnitService;

  @Autowired
  public UserController(
      UserService userService,
      GetUserResourceLinks getUserResourceLinks,
      UserStatisticsService userStatisticsService,
      WebCertUserService webCertUserService,
      ChangeUnitService changeUnitService) {
    this.userService = userService;
    this.getUserResourceLinks = getUserResourceLinks;
    this.webCertUserService = webCertUserService;
    this.userStatisticsService = userStatisticsService;
    this.changeUnitService = changeUnitService;
  }

  @GetMapping
  @PrometheusTimeMethod
  @PerformanceLogging(eventAction = "user-get-user", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<UserResponseDTO> getUser() {
    LOG.debug("Getting logged in user");
    final var loggedInUser = userService.getLoggedInUser();
    final var resourceLinks = getUserResourceLinks.get(webCertUserService.getUser());
    return ResponseEntity.ok(UserResponseDTO.create(loggedInUser, resourceLinks));
  }

  @GetMapping("/statistics")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-get-user-tabs",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<UserStatisticsDTO> getUserTabs() {
    LOG.debug("Getting user statistics");
    final var result = userStatisticsService.getUserStatistics();
    return ResponseEntity.ok(result);
  }

  @PostMapping("/unit/{unitHsaId}")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "user-change-unit",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public ResponseEntity<UserResponseDTO> changeUnit(
      @PathVariable("unitHsaId") @NotNull String unitHsaId) {
    LOG.debug("Changing care unit to {}", unitHsaId);
    try {
      final var updatedUser = changeUnitService.change(unitHsaId);
      final var resourceLinks = getUserResourceLinks.get(webCertUserService.getUser());
      return ResponseEntity.ok(UserResponseDTO.create(updatedUser, resourceLinks));
    } catch (ChangeUnitException e) {
      return ResponseEntity.badRequest().body(null);
    }
  }
}
