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

package se.inera.intyg.webcert.web.web.controller.internalapi.dto;

import java.util.List;
import java.util.Objects;


public class AvailableFunctionDTO {

    private AvailableFunctionTypeDTO type;
    private List<InformationDTO> information;
    private String name;
    private String description;
    private String body;
    private String title;
    private boolean enabled;

    public static AvailableFunctionDTO create(AvailableFunctionTypeDTO type, String title, String name, String body, String description,
        List<InformationDTO> information, boolean enabled) {
        final var availableFunction = new AvailableFunctionDTO();
        availableFunction.setType(type);
        availableFunction.setTitle(title);
        availableFunction.setName(name);
        availableFunction.setBody(body);
        availableFunction.setInformation(information);
        availableFunction.setDescription(description);
        availableFunction.setEnabled(enabled);
        return availableFunction;
    }

    public static AvailableFunctionDTO create(AvailableFunctionTypeDTO type, String title, String name, String body, boolean enabled) {
        final var availableFunction = new AvailableFunctionDTO();
        availableFunction.setType(type);
        availableFunction.setTitle(title);
        availableFunction.setName(name);
        availableFunction.setBody(body);
        availableFunction.setEnabled(enabled);
        return availableFunction;
    }

    public static AvailableFunctionDTO create(AvailableFunctionTypeDTO type, String name, boolean enabled) {
        final var availableFunction = new AvailableFunctionDTO();
        availableFunction.setType(type);
        availableFunction.setName(name);
        availableFunction.setEnabled(enabled);
        return availableFunction;
    }

    public List<InformationDTO> getInformation() {
        return information;
    }

    public void setInformation(List<InformationDTO> information) {
        this.information = information;
    }

    public AvailableFunctionTypeDTO getType() {
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

    public void setType(AvailableFunctionTypeDTO type) {
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AvailableFunctionDTO that = (AvailableFunctionDTO) o;
        return type == that.type && Objects.equals(information, that.information) && Objects.equals(name, that.name)
            && Objects.equals(description, that.description) && Objects.equals(body, that.body) && Objects.equals(
            title, that.title) && Objects.equals(enabled, that.enabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, information, name, description, body, title, enabled);
    }

    @Override
    public String toString() {
        return "AvailableFunctionDTO{"
            + "type=" + type
            + ", information=" + information
            + ", name='" + name + '\''
            + ", description='" + description + '\''
            + ", body='" + body + '\''
            + ", title='" + title + '\''
            + ", enabled='" + enabled + '\''
            + '}';
    }
}
