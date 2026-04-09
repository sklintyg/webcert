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

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.infra.dynamiclink.model.DynamicLink;
import se.inera.intyg.webcert.infra.dynamiclink.service.DynamicLinkService;
import se.inera.intyg.webcert.infra.ia.services.IABannerService;
import se.inera.intyg.webcert.infra.postnummer.service.PostnummerService;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.Area;
import se.inera.intyg.webcert.web.web.controller.api.dto.ConfigResponse;

@RestController
@RequestMapping("/api/config")
public class ConfigApiController extends AbstractApiController {

  @Value("${project.version}")
  private String version;

  @Value("${buildNumber}")
  private String build;

  @Value("${privatepractitioner.portal.registration.url}")
  private String ppHost;

  @Value("${certificate.view.url.base}")
  private String dashboardUrl;

  @Autowired private Environment environment;

  @Value("${sakerhetstjanst.saml.idp.metadata.url}")
  private String sakerhetstjanstIdpUrl;

  @Value("${cgi.funktionstjanster.saml.idp.metadata.url}")
  private String cgiFunktionstjansterIdpUrl;

  @Value("${webcert.user.survey.url:}")
  private String webcertUserSurveyUrl;

  @Value("${webcert.user.survey.date.to:}")
  private String webcertUserSurveyDateTo;

  @Value("${webcert.user.survey.date.from:}")
  private String webcertUserSurveyDateFrom;

  @Value("${webcert.user.survey.version:}")
  private String webcertUserSurveyVersion;

  @Autowired private DynamicLinkService dynamicLinkService;

  @Autowired private PostnummerService postnummerService;

  @Autowired private IABannerService iaBannerService;

  @GetMapping
  @PerformanceLogging(
      eventAction = "config-get-config",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<ConfigResponse> getConfig() {
    Boolean useMinifiedJavaScript =
        Boolean.parseBoolean(environment.getProperty("useMinifiedJavaScript", "true"));
    ConfigResponse configResponse =
        new ConfigResponse(
            version,
            build,
            ppHost,
            dashboardUrl,
            useMinifiedJavaScript,
            sakerhetstjanstIdpUrl,
            cgiFunktionstjansterIdpUrl,
            webcertUserSurveyUrl,
            iaBannerService.getCurrentBanners(),
            webcertUserSurveyDateTo,
            webcertUserSurveyDateFrom,
            webcertUserSurveyVersion);

    return ResponseEntity.ok(configResponse);
  }

  @GetMapping("/links")
  @PerformanceLogging(
      eventAction = "config-get-dynamic-links",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public Map<String, DynamicLink> getDynamicLinks() {
    return dynamicLinkService.getAllAsMap();
  }

  @GetMapping("/kommuner")
  @PerformanceLogging(
      eventAction = "config-get-kommun-list",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public List<String> getKommunList() {
    return postnummerService.getKommunList();
  }

  @GetMapping("area/{zipcode}")
  @PerformanceLogging(
      eventAction = "config-get-area-by-zid-code",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public List<Area> getAreaByZipCode(@PathVariable("zipcode") String zipCode) {
    final var result = postnummerService.getOmradeByPostnummer(zipCode);
    if (result == null || result.isEmpty()) {
      return List.of();
    }
    return result.stream()
        .map(
            o ->
                Area.builder()
                    .zipCode(o.getPostnummer())
                    .city(o.getPostort())
                    .municipality(o.getKommun())
                    .county(o.getLan())
                    .build())
        .toList();
  }
}
