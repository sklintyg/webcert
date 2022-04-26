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

package se.inera.intyg.webcert.web.service.facade.list.config.factory;

import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateStatus;
import se.inera.intyg.webcert.web.service.facade.list.dto.FilterStatusType;
import se.inera.intyg.webcert.web.service.facade.list.dto.ForwardedType;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ListFilterConfigFactory {
    private static final String SIGNED_DESCRIPTION =
            "Av prestanda skäl är det är ej möjligt att välja datum längre än 3 månader bakåt i tiden.";

    public static ListFilterPersonIdConfig defaultPersonId() {
        return new ListFilterPersonIdConfig("PATIENT_ID", "Patient", "åååå-mm-dd");
    }

    public static ListFilterDateConfig toDate() {
        return new ListFilterDateConfig("TO", "Till");
    }

    public static ListFilterDateConfig toDateWithLimits(LocalDateTime max, LocalDateTime min) {
        return new ListFilterDateConfig("TO", "Till", max, min, null);
    }

    public static ListFilterDateConfig fromDate() {
        return new ListFilterDateConfig("FROM", "Från");
    }

    public static ListFilterDateConfig fromDateWithLimits(LocalDateTime max, LocalDateTime min, LocalDateTime defaultValue) {
        return new ListFilterDateConfig("FROM", "Från", max, min, defaultValue);
    }


    public static ListFilterDateRangeConfig savedDateRange() {
        return new ListFilterDateRangeConfig("SAVED", "Sparat datum", toDate(), fromDate(), true);
    }

    public static ListFilterDateRangeConfig signedDateRange() {
        final var min = LocalDateTime.now().minusMonths(3);
        return new ListFilterDateRangeConfig("SIGNED", "Signeringsdatum", toDate(),
                fromDateWithLimits(null, min, min),true, SIGNED_DESCRIPTION);
    }

    public static ListFilterSelectConfig forwardedSelect() {
        return new ListFilterSelectConfig("FORWARDED", "Vidarebefordrat",
                List.of(
                        ListFilterConfigValue.create(
                                ForwardedType.SHOW_ALL.toString(), ForwardedType.SHOW_ALL.getName(), true
                        ),
                        ListFilterConfigValue.create(
                                ForwardedType.FORWARDED.toString(), ForwardedType.FORWARDED.getName(), false
                        ),
                        ListFilterConfigValue.create(
                                ForwardedType.NOT_FORWARDED.toString(), ForwardedType.NOT_FORWARDED.getName(), false
                        )
                )
        );
    }

    public static ListFilterSelectConfig draftStatusSelect() {
        return new ListFilterSelectConfig("STATUS", "Utkast", List.of(
                ListFilterConfigValue.create(CertificateStatus.SHOW_ALL.toString(), CertificateStatus.SHOW_ALL.getName(), true),
                ListFilterConfigValue.create(CertificateStatus.INCOMPLETE.toString(), "Uppgifter saknas", false),
                ListFilterConfigValue.create(CertificateStatus.COMPLETE.toString(), "Kan signeras", false),
                ListFilterConfigValue.create(CertificateStatus.LOCKED.toString(), "Låsta", false)
            )
        );
    }

    public static ListFilterOrderConfig orderBy(ListColumnType defaultOrder) {
        return new ListFilterOrderConfig("ORDER_BY", "", defaultOrder);
    }

    public static ListFilterBooleanConfig ascending() {
        return new ListFilterBooleanConfig("ASCENDING", "", false);
    }
    
    public static ListFilterPageSizeConfig pageSize() {
        final var pageSizes = new int[]{10, 25, 50, 100};
        return new ListFilterPageSizeConfig("PAGESIZE", "Visa antal träffar", pageSizes);
    }

    public static ListFilterSelectConfig createStaffSelect(
            String id, String title, List<StaffListInfo> staffInfo, String defaultHsaId) {
        final var isShowAllDefault = defaultHsaId.length() == 0;
        final var convertedSavedByList = staffInfo.stream().map(
                (info) -> convertStaffInfoIntoSelectFilter(info, defaultHsaId)).collect(Collectors.toList()
        );
        convertedSavedByList.add(0, ListFilterConfigValue.create(
                "SHOW_ALL", "Visa alla", isShowAllDefault
                )
        );
        return new ListFilterSelectConfig(id, title, convertedSavedByList, !isShowAllDefault);
    }

    private static ListFilterConfigValue convertStaffInfoIntoSelectFilter(
            StaffListInfo staffListInfo, String defaultHsaId) {
        return ListFilterConfigValue.create(
                staffListInfo.getHsaId(), staffListInfo.getName(), defaultHsaId.equals(staffListInfo.getHsaId())
        );
    }

    public static ListFilterRadioConfig certificateStatusRadio() {
        return new ListFilterRadioConfig("STATUS", "", List.of(
                ListFilterConfigValue.create(FilterStatusType.CURRENT_CERTIFICATES.toString(), FilterStatusType.CURRENT_CERTIFICATES.getName(), true),
                ListFilterConfigValue.create(FilterStatusType.MODIFIED_CERTIFICATES.toString(), FilterStatusType.MODIFIED_CERTIFICATES.getName(), false),
                ListFilterConfigValue.create(FilterStatusType.ALL_CERTIFICATES.toString(), FilterStatusType.ALL_CERTIFICATES.getName(), false)
            )
        );
    }
}
