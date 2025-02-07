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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.common.util.integration.json.CustomObjectMapper;

public class Period {

    private LocalDate from;
    private LocalDate tom;
    private int nedsattning;

    public Period() {

    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public int getNedsattning() {
        return nedsattning;
    }

    public void setNedsattning(int nedsattning) {
        this.nedsattning = nedsattning;
    }

    public static Period valueOf(String value) {
        try {
            final ObjectMapper objectMapper = new CustomObjectMapper();
            return objectMapper.readValue(value, Period.class);
        } catch (Exception ex) {
            return null;
        }
    }
}
