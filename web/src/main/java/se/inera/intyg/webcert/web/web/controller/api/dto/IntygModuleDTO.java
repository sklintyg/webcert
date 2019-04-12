/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import java.util.ArrayList;
import java.util.List;

import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;

public class IntygModuleDTO implements Comparable<IntygModuleDTO> {
    private String id;

    private String label;

    private String description;

    private String detailedDescription;

    private String issuerTypeId;

    private String cssPath;

    private String scriptPath;

    private String dependencyDefinitionPath;

    private String defaultRecipient;

    private boolean deprecated;

    private boolean displayDeprecated;

    private List<ActionLink> links = new ArrayList<>();

    public IntygModuleDTO(IntygModule intygModule) {
        this.id = intygModule.getId();
        this.label = intygModule.getLabel();
        this.description = intygModule.getDescription();
        this.detailedDescription = intygModule.getDetailedDescription();
        this.issuerTypeId = intygModule.getIssuerTypeId();
        this.cssPath = intygModule.getCssPath();
        this.scriptPath = intygModule.getScriptPath();
        this.dependencyDefinitionPath = intygModule.getDependencyDefinitionPath();
        this.defaultRecipient = intygModule.getDefaultRecipient();
        this.deprecated = intygModule.isDeprecated();
        this.displayDeprecated = intygModule.getDisplayDeprecated();
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public String getIssuerTypeId() {
        return issuerTypeId;
    }

    public String getCssPath() {
        return cssPath;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public String getDependencyDefinitionPath() {
        return dependencyDefinitionPath;
    }

    public String getDefaultRecipient() {
        return defaultRecipient;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public boolean isDisplayDeprecated() {
        return displayDeprecated;
    }

    public List<ActionLink> getLinks() {
        return links;
    }

    public void addLink(ActionLink link) {
        this.links.add(link);
    }

    @Override
    public int compareTo(IntygModuleDTO o) {
        return getLabel().compareTo(o.getLabel());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntygModuleDTO) {
            IntygModuleDTO other = (IntygModuleDTO) obj;
            return id.equals(other.id);
        } else {
            return false;
        }
    }
}
