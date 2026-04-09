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

import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.CAREGIVER_ID;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.CARE_UNIT_ID;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.CONNECTIVITY;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.ERROR_MESSAGE;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.HEIGHT;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.INTYG_ID;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.INTYG_TYPE;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.IP;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.MAIN_DIAGNOSIS_CODE;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.NET_ID_VERSION;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.USER_CLIENT_CONTEXT;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.WIDTH;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.infra.monitoring.logging.UserAgentInfo;
import se.inera.intyg.webcert.infra.monitoring.logging.UserAgentParser;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest;

/** Controller that logs messages from JavaScript to the normal log. */
@RestController
@RequestMapping("/api/jslog")
public class JsLogApiController extends AbstractApiController {

  private static final Logger LOG = LoggerFactory.getLogger(JsLogApiController.class);

  @Autowired private MonitoringLogService monitoringService;

  @Autowired private UserAgentParser userAgentParser;

  @PostMapping("/debug")
  @PerformanceLogging(eventAction = "js-log-debug", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Void> debug(@RequestBody(required = false) String message) {
    LOG.debug(message);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/monitoring")
  @PerformanceLogging(
      eventAction = "js-log-monitoring",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Void> monitoring(
      @RequestBody(required = false) MonitoringRequest request,
      @RequestHeader(value = "User-Agent", required = false) String userAgent) {
    if (request == null || !request.isValid()) {
      return ResponseEntity.badRequest().build();
    }

    switch (request.getEvent()) {
      case BROWSER_INFO:
        final UserAgentInfo userAgentInfo = userAgentParser.parse(userAgent);
        monitoringService.logBrowserInfo(
            userAgentInfo.getBrowserName(),
            userAgentInfo.getBrowserVersion(),
            userAgentInfo.getOsFamily(),
            userAgentInfo.getOsVersion(),
            request.getInfo().get(WIDTH),
            request.getInfo().get(HEIGHT),
            request.getInfo().get(NET_ID_VERSION));
        break;
      case DIAGNOSKODVERK_CHANGED:
        monitoringService.logDiagnoskodverkChanged(
            request.getInfo().get(INTYG_ID), request.getInfo().get(INTYG_TYPE));
        break;
      case SIGNING_FAILED:
        monitoringService.logUtkastSignFailed(
            request.getInfo().get(ERROR_MESSAGE), request.getInfo().get(INTYG_ID));
        break;
      case IDP_CONNECTIVITY_CHECK:
        monitoringService.logIdpConnectivityCheck(
            request.getInfo().get(IP), request.getInfo().get(CONNECTIVITY));
        break;
      default:
        return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok().build();
  }

  @PostMapping("/srs")
  @PerformanceLogging(
      eventAction = "js-log-srs-monitoring",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Void> srsMonitoring(
      @RequestBody(required = false) MonitoringRequest request) {
    if (request == null || !request.isValid()) {
      return ResponseEntity.badRequest().build();
    }
    switch (request.getEvent()) {
      case SRS_LOADED:
        monitoringService.logSrsLoaded(
            request.getInfo().get(USER_CLIENT_CONTEXT),
            request.getInfo().get(INTYG_ID),
            request.getInfo().get(CAREGIVER_ID),
            request.getInfo().get(CARE_UNIT_ID),
            request.getInfo().get(MAIN_DIAGNOSIS_CODE));
        break;
      case SRS_PANEL_ACTIVATED:
        monitoringService.logSrsPanelActivated(
            request.getInfo().get(USER_CLIENT_CONTEXT),
            request.getInfo().get(INTYG_ID),
            request.getInfo().get(CAREGIVER_ID),
            request.getInfo().get(CARE_UNIT_ID));
        break;
      case SRS_CONSENT_ANSWERED:
        monitoringService.logSrsConsentAnswered(
            request.getInfo().get(USER_CLIENT_CONTEXT),
            request.getInfo().get(INTYG_ID),
            request.getInfo().get(CAREGIVER_ID),
            request.getInfo().get(CARE_UNIT_ID));
        break;
      case SRS_QUESTION_ANSWERED:
        monitoringService.logSrsQuestionAnswered(
            request.getInfo().get(USER_CLIENT_CONTEXT),
            request.getInfo().get(INTYG_ID),
            request.getInfo().get(CAREGIVER_ID),
            request.getInfo().get(CARE_UNIT_ID));
        break;
      case SRS_CALCULATE_CLICKED:
        monitoringService.logSrsCalculateClicked(
            request.getInfo().get(USER_CLIENT_CONTEXT),
            request.getInfo().get(INTYG_ID),
            request.getInfo().get(CAREGIVER_ID),
            request.getInfo().get(CARE_UNIT_ID));
        break;
      case SRS_HIDE_QUESTIONS_CLICKED:
        monitoringService.logSrsHideQuestionsClicked(
            request.getInfo().get(USER_CLIENT_CONTEXT),
            request.getInfo().get(INTYG_ID),
            request.getInfo().get(CAREGIVER_ID),
            request.getInfo().get(CARE_UNIT_ID));
        break;
      case SRS_SHOW_QUESTIONS_CLICKED:
        monitoringService.logSrsShowQuestionsClicked(
            request.getInfo().get(USER_CLIENT_CONTEXT),
            request.getInfo().get(INTYG_ID),
            request.getInfo().get(CAREGIVER_ID),
            request.getInfo().get(CARE_UNIT_ID));
        break;
      case SRS_MEASURES_SHOW_MORE_CLICKED:
        monitoringService.logSrsMeasuresShowMoreClicked(
            request.getInfo().get(USER_CLIENT_CONTEXT),
            request.getInfo().get(INTYG_ID),
            request.getInfo().get(CAREGIVER_ID),
            request.getInfo().get(CARE_UNIT_ID));
        break;
      case SRS_MEASURES_EXPAND_ONE_CLICKED:
        monitoringService.logSrsMeasuresExpandOneClicked(
            request.getInfo().get(USER_CLIENT_CONTEXT),
            request.getInfo().get(INTYG_ID),
            request.getInfo().get(CAREGIVER_ID),
            request.getInfo().get(CARE_UNIT_ID));
        break;
      case SRS_MEASURES_LINK_CLICKED:
        monitoringService.logSrsMeasuresLinkClicked(
            request.getInfo().get(USER_CLIENT_CONTEXT),
            request.getInfo().get(INTYG_ID),
            request.getInfo().get(CAREGIVER_ID),
            request.getInfo().get(CARE_UNIT_ID));
        break;
      case SRS_STATISTICS_ACTIVATED:
        monitoringService.logSrsStatisticsActivated(
            request.getInfo().get(USER_CLIENT_CONTEXT),
            request.getInfo().get(INTYG_ID),
            request.getInfo().get(CAREGIVER_ID),
            request.getInfo().get(CARE_UNIT_ID));
        break;
      case SRS_STATISTICS_LINK_CLICKED:
        monitoringService.logSrsStatisticsLinkClicked(
            request.getInfo().get(USER_CLIENT_CONTEXT),
            request.getInfo().get(INTYG_ID),
            request.getInfo().get(CAREGIVER_ID),
            request.getInfo().get(CARE_UNIT_ID));
        break;
      case SRS_MEASURES_DISPLAYED:
        monitoringService.logSrsMeasuresDisplayed(
            request.getInfo().get(USER_CLIENT_CONTEXT),
            request.getInfo().get(INTYG_ID),
            request.getInfo().get(CAREGIVER_ID),
            request.getInfo().get(CARE_UNIT_ID));
        break;
      default:
        return ResponseEntity.badRequest().build();
    }

    return ResponseEntity.ok().build();
  }
}
