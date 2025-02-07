/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.facade.dto;

import java.util.List;
import se.inera.intyg.infra.driftbannerdto.Banner;

public class ConfigurationDTO {

    private String ppHost;
    private String version;
    private List<Banner> banners;
    private String sakerhetstjanstIdpUrl;
    private String cgiFunktionstjansterIdpUrl;
    private String forwardDraftOrQuestionUrl;

    public ConfigurationDTO(
        String version,
        List<Banner> banners,
        String ppHost,
        String sakerhetstjanstIdpUrl,
        String cgiFunktionstjansterIdpUrl, String forwardDraftOrQuestionUrl) {
        this.version = version;
        this.banners = banners;
        this.ppHost = ppHost;
        this.sakerhetstjanstIdpUrl = sakerhetstjanstIdpUrl;
        this.cgiFunktionstjansterIdpUrl = cgiFunktionstjansterIdpUrl;
        this.forwardDraftOrQuestionUrl = forwardDraftOrQuestionUrl;
    }

    public ConfigurationDTO() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Banner> getBanners() {
        return banners;
    }

    public String getSakerhetstjanstIdpUrl() {
        return sakerhetstjanstIdpUrl;
    }

    public String getCgiFunktionstjansterIdpUrl() {
        return cgiFunktionstjansterIdpUrl;
    }

    public String getForwardDraftOrQuestionUrl() {
        return forwardDraftOrQuestionUrl;
    }

    public String getPpHost() {
        return ppHost;
    }

    public void setPpHost(String ppHost) {
        this.ppHost = ppHost;
    }
}
