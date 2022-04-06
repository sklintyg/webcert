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
package se.inera.intyg.webcert.web.service.facade.list.dto;

import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CertificateListItem {
    private final Map<String, Object> values;

    public CertificateListItem() {
        values = new HashMap<>();
    }

    public void addValue(ListColumnType type, Object value) {
        values.put(type.toString(), value);
    }

    public Object getValue(String key) {
        return values.get(key);
    }

    public Object getValue(ListColumnType type) {
        return values.get(type.toString());
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public String getValueAsString(ListColumnType type) {
        return getValue(type).toString();
    }

    public String getValueAsPatientId() {
        final var value = getValue(ListColumnType.PATIENT_ID);
        return ((PatientListInfo) value).getId();
    }

    public Boolean getValueAsBoolean(ListColumnType type) {
        return (Boolean) getValue(type);
    }

    public LocalDateTime getValueAsDate(ListColumnType type) {
        return (LocalDateTime) getValue(type);
    }
}
