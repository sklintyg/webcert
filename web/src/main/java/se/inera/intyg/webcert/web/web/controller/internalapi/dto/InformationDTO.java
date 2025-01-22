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

import java.util.Objects;

public class InformationDTO {

    private String id;
    private String text;
    private InformationTypeDto type;

    public static InformationDTO create(String text, InformationTypeDto type) {
        final var informationType = new InformationDTO();
        informationType.setText(text);
        informationType.setType(type);
        return informationType;
    }

    public static InformationDTO create(String id, String text, InformationTypeDto type) {
        final var informationType = new InformationDTO();
        informationType.setId(id);
        informationType.setText(text);
        informationType.setType(type);
        return informationType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public InformationTypeDto getType() {
        return type;
    }

    public void setType(InformationTypeDto type) {
        this.type = type;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InformationDTO that = (InformationDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(text, that.text) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, type);
    }

    @Override
    public String toString() {
        return "InformationDTO{"
            + "id='" + id + '\''
            + ", text='" + text + '\''
            + ", type=" + type
            + '}';
    }
}
