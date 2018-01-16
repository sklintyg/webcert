/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The global configuration of Webcert")
public class ConfigResponse {

    @ApiModelProperty(name = "VERSION", dataType = "String")
    private String version;

    @ApiModelProperty(name = "BUILD_NUMBER", dataType = "String")
    private String buildNumber;

    @ApiModelProperty(name = "PP_HOST", dataType = "String")
    private String ppHost;

    @ApiModelProperty(name = "DASHBOARD_URL", dataType = "String")
    private String dashboardUrl;

    @ApiModelProperty(name = "JS_MINIFIED", dataType = "Boolean")
    private Boolean jsMinified;

    @ApiModelProperty(name = "SAKERHETSTJANST_IDP_URL", dataType = "String")
    private String sakerhetstjanstIdpUrl;

    @ApiModelProperty(name = "CGI_FUNKTIONSTJANSTER_IDP_URL", dataType = "String")
    private String cgiFunktionstjansterIdpUrl;

    public ConfigResponse(String version, String buildNumber, String ppHost, String dashboardUrl, Boolean jsMinified, String sakerhetstjanstIdpUrl, String cgiFunktionstjansterIdpUrl) {
        this.version = version;
        this.buildNumber = buildNumber;
        this.ppHost = ppHost;
        this.dashboardUrl = dashboardUrl;
        this.jsMinified = jsMinified;
        this.sakerhetstjanstIdpUrl = sakerhetstjanstIdpUrl;
        this.cgiFunktionstjansterIdpUrl = cgiFunktionstjansterIdpUrl;
    }

    @JsonProperty("VERSION")
    public String getVersion() {
        return version;
    }

    @JsonProperty("BUILD_NUMBER")
    public String getBuildNumber() {
        return buildNumber;
    }

    @JsonProperty("PP_HOST")
    public String getPpHost() {
        return ppHost;
    }

    @JsonProperty("DASHBOARD_URL")
    public String getDashboardUrl() {
        return dashboardUrl;
    }

    @JsonProperty("JS_MINIFIED")
    public Boolean getJsMinified() {
        return jsMinified;
    }

    @JsonProperty("SAKERHETSTJANST_IDP_URL")
    public String getSakerhetstjanstIdpUrl() {
        return sakerhetstjanstIdpUrl;
    }

    @JsonProperty("CGI_FUNKTIONSTJANSTER_IDP_URL")
    public String getCgiFunktionstjansterIdpUrl() {
        return cgiFunktionstjansterIdpUrl;
    }
}
