/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.internalapi.dto;

import java.util.Objects;


public class ResourceLinkDTO {

    private ResourceLinkTypeDTO type;
    private String name;
    private String description;
    private String body;
    private String title;

    public static ResourceLinkDTO create(ResourceLinkTypeDTO type, String title, String name, String description, String body) {
        final var resourceLink = new ResourceLinkDTO();
        resourceLink.setType(type);
        resourceLink.setTitle(title);
        resourceLink.setName(name);
        resourceLink.setDescription(description);
        resourceLink.setBody(body);
        return resourceLink;
    }

    public ResourceLinkTypeDTO getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public void setType(ResourceLinkTypeDTO type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResourceLinkDTO that = (ResourceLinkDTO) o;
        return type == that.type && Objects.equals(name, that.name) && Objects.equals(description, that.description)
            && Objects.equals(body, that.body) && Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, description, body, title);
    }

    @Override
    public String toString() {
        return "ResourceLinkDTO{"
            + "type=" + type
            + ", name='" + name + '\''
            + ", description='" + description + '\''
            + ", body='" + body + '\''
            + ", title='" + title + '\''
            + '}';
    }
}