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

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import se.inera.intyg.webcert.infra.driftbannerdto.Application;
import se.inera.intyg.webcert.infra.dynamiclink.model.DynamicLink;
import se.inera.intyg.webcert.infra.dynamiclink.service.DynamicLinkService;
import se.inera.intyg.webcert.infra.ia.services.IABannerService;
import se.inera.intyg.webcert.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ConfigurationDTO;

@RestController
@RequestMapping("/api/configuration")
@Slf4j
public class ConfigController {

  private static final String UTF_8_CHARSET = ";charset=utf-8";

  @Value("${project.version}")
  private String version;

  @Value("${sakerhetstjanst.saml.idp.metadata.url}")
  private String sakerhetstjanstIdpUrl;

  @Value("${cgi.funktionstjanster.saml.idp.metadata.url}")
  private String cgiFunktionstjansterIdpUrl;

  @Value("${privatepractitioner.portal.registration.url}")
  private String ppHost;

  @Value("${forward.draft.or.question.url}")
  private String forwardDraftOrQuestionUrl;

  @Value("${idp.connect.urls:}")
  private String idpConnectUrls;

  @Value("${webcert.frontend.api.timeout:30000}")
  private Integer webcertFrontendApiTimeout;

  @Autowired private DynamicLinkService dynamicLinkService;

  @Autowired private IABannerService iaBannerService;

  @PostConstruct
  public void init() {
    SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
  }

  @GetMapping
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "config-get-configuration",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<ConfigurationDTO> getConfiguration() {
    if (log.isDebugEnabled()) {
      log.debug("Getting configuration");
    }

    final var banners =
        iaBannerService.getCurrentBanners().stream()
            .filter((banner -> banner.getApplication() == Application.WEBCERT))
            .toList();

    return Response.ok(
            ConfigurationDTO.builder()
                .version(version)
                .banners(banners)
                .ppHost(ppHost)
                .sakerhetstjanstIdpUrl(sakerhetstjanstIdpUrl)
                .cgiFunktionstjansterIdpUrl(cgiFunktionstjansterIdpUrl)
                .forwardDraftOrQuestionUrl(forwardDraftOrQuestionUrl)
                .idpConnectUrls(
                    idpConnectUrls == null
                        ? List.of()
                        : Arrays.stream(idpConnectUrls.split(","))
                            .filter(url -> !url.isBlank())
                            .toList())
                .webcertFrontendApiTimeout(webcertFrontendApiTimeout)
                .build())
        .build();
  }

  @GetMapping("/links")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "config-get-dynamic-links",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public Map<String, DynamicLink> getDynamicLinks() {
    return dynamicLinkService.getAllAsMap();
  }
}
