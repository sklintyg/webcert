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
package se.inera.intyg.webcert.web.service.facade.list.config.factory;

import se.inera.intyg.webcert.web.service.facade.list.config.dto.CertificateListItemValueType;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.TableHeading;

public class TableHeadingFactory {

    public static TableHeading text(ListColumnType type) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.TEXT, type.getDescription());
    }

    public static TableHeading text(ListColumnType type, String description) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.TEXT, description);
    }

    public static TableHeading date(ListColumnType type) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.DATE, type.getDescription());
    }

    public static TableHeading date(ListColumnType type, boolean defaultAscending) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.DATE, type.getDescription(), defaultAscending);
    }

    public static TableHeading patientInfo(ListColumnType type) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.PATIENT_INFO, type.getDescription());
    }

    public static TableHeading forwarded(ListColumnType type, String description) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.FORWARD, description);
    }

    public static TableHeading openButton(ListColumnType type) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.OPEN_BUTTON, type.getDescription());
    }

    public static TableHeading forwardButton(ListColumnType type) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.FORWARD_BUTTON, type.getDescription());
    }

    public static TableHeading renewButton(ListColumnType type) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.RENEW_BUTTON, type.getDescription());
    }
}
