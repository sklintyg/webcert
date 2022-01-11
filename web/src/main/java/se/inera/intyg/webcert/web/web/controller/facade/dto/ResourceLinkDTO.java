/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

public class ResourceLinkDTO {

    private ResourceLinkTypeDTO type;
    private String name;
    private String description;
    private String body;
    private boolean enabled;

    public static ResourceLinkDTO create(ResourceLinkTypeDTO type, String name, String description, boolean enabled) {
        final var resourceLink = new ResourceLinkDTO();
        resourceLink.setType(type);
        resourceLink.setName(name);
        resourceLink.setDescription(description);
        resourceLink.setEnabled(enabled);
        return resourceLink;
    }

    public static ResourceLinkDTO create(ResourceLinkTypeDTO type, String name, String description, String body, boolean enabled) {
        final var resourceLink = new ResourceLinkDTO();
        resourceLink.setType(type);
        resourceLink.setName(name);
        resourceLink.setDescription(description);
        resourceLink.setBody(body);
        resourceLink.setEnabled(enabled);
        return resourceLink;
    }

    public ResourceLinkTypeDTO getType() {
        return type;
    }

    public void setType(ResourceLinkTypeDTO type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
