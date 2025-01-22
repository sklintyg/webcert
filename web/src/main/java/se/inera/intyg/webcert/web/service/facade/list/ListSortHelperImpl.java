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
package se.inera.intyg.webcert.web.service.facade.list;

import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class ListSortHelperImpl implements ListSortHelper {

    @Override
    public List<CertificateListItem> sort(List<CertificateListItem> list, String order, boolean ascending) {
        final var isOrderType = Arrays.stream(ListColumnType.values()).anyMatch((value) -> value.toString().equals(order));
        final var comparator = getCertificateComparator(isOrderType ? ListColumnType.valueOf(order) : ListColumnType.SAVED, ascending);
        list.sort(comparator);
        return list;
    }

    private Comparator<CertificateListItem> getCertificateComparator(ListColumnType orderBy, Boolean ascending) {
        Comparator<CertificateListItem> comparator;
        switch (orderBy) {
            case CERTIFICATE_TYPE_NAME:
            case STATUS:
            case SAVED_BY:
            case SAVED_SIGNED_BY:
                comparator = Comparator.comparing((item) -> item.valueAsString(orderBy));
                break;
            case PATIENT_ID:
                comparator = Comparator.comparing(CertificateListItem::valueAsPatientId);
                break;
            case FORWARDED:
                comparator = Comparator.comparing((item) -> item.valueAsBoolean(orderBy));
                break;
            case SAVED:
            case SIGNED:
            default:
                comparator = Comparator.comparing((item) -> item.valueAsDate(orderBy));
                break;
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }
        return comparator;
    }
}
