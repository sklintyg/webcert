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
package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public enum ListColumnType {
    PATIENT_ID("Patient"),
    SAVED_BY("Sparat av"),
    SAVED("Senast sparat"),
    CERTIFICATE_ID(""),
    CERTIFICATE_TYPE_NAME("Typ av intyg"),
    STATUS("Status"),
    FORWARDED("Vidarebefordrad");

    private final String name;

    ListColumnType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
