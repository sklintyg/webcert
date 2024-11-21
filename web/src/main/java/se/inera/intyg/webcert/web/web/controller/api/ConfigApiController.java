/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import se.inera.intyg.infra.dynamiclink.model.DynamicLink;
import se.inera.intyg.infra.dynamiclink.service.DynamicLinkService;
import se.inera.intyg.infra.integration.ia.services.IABannerService;
import se.inera.intyg.infra.integration.postnummer.service.PostnummerService;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.ConfigResponse;

@Path("/config")
@Api(value = "config", description = "REST API för konfigurationsparametrar", produces = MediaType.APPLICATION_JSON)
public class ConfigApiController extends AbstractApiController {

    @Value("${project.version}")
    private String version;

    @Value("${buildNumber}")
    private String build;

    @Value("${privatepractitioner.portal.registration.url}")
    private String ppHost;

    @Value("${certificate.view.url.base}")
    private String dashboardUrl;

    @Autowired
    private Environment environment;

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

    @Autowired
    private DynamicLinkService dynamicLinkService;

    @Autowired
    private PostnummerService postnummerService;

    @Autowired
    private IABannerService iaBannerService;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get module configuration for Webcert", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "config-get-config", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getConfig() {
        Boolean useMinifiedJavaScript = Boolean.parseBoolean(environment.getProperty("useMinifiedJavaScript", "true"));
        ConfigResponse configResponse = new ConfigResponse(version, build, ppHost, dashboardUrl, useMinifiedJavaScript,
            sakerhetstjanstIdpUrl, cgiFunktionstjansterIdpUrl, webcertUserSurveyUrl, iaBannerService.getCurrentBanners(),
            webcertUserSurveyDateTo, webcertUserSurveyDateFrom, webcertUserSurveyVersion);

        return Response.ok(configResponse).build();
    }

    @GET
    @Path("/links")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get dynamic links for Webcert", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "config-get-dynamic-links", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Map<String, DynamicLink> getDynamicLinks() {
        return dynamicLinkService.getAllAsMap();
    }

    @GET
    @Path("/kommuner")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @ApiOperation(value = "Get list of kommuner from postnummerservice", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "config-get-kommun-list", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public List<String> getKommunList() {
        return postnummerService.getKommunList();
    }

    @PostConstruct
    public void init() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
}
