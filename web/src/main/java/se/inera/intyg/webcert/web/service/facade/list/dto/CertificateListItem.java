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
package se.inera.intyg.webcert.web.service.facade.list.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import lombok.EqualsAndHashCode;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;

@EqualsAndHashCode
public class CertificateListItem {

    private final Map<String, Object> values;

    public CertificateListItem() {
        values = new TreeMap<>();
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

    public String valueAsString(ListColumnType type) {
        return getValue(type).toString();
    }

    public String valueAsPatientId() {
        return ((PatientListInfo) getValue(ListColumnType.PATIENT_ID)).getId();
    }

    public Boolean valueAsBoolean(ListColumnType type) {
        return (Boolean) getValue(type);
    }

    public LocalDateTime valueAsDate(ListColumnType type) {
        return (LocalDateTime) getValue(type);
    }
}
