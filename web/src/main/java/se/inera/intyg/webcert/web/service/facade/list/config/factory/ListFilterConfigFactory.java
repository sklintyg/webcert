package se.inera.intyg.webcert.web.service.facade.list.config.factory;

import se.inera.intyg.webcert.web.service.facade.list.dto.DraftStatus;
import se.inera.intyg.webcert.web.service.facade.list.dto.ForwardedType;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;

import java.util.List;
import java.util.stream.Collectors;

public class ListFilterConfigFactory {
    public static ListFilterPersonIdConfig defaultPersonId() {
        return new ListFilterPersonIdConfig("PATIENT_ID", "Patient", "åååå-mm-dd");
    }

    public static ListFilterDateConfig toDate() {
        return new ListFilterDateConfig("TO", "Till");
    }

    public static ListFilterDateConfig fromDate() {
        return new ListFilterDateConfig("FROM", "Från");
    }

    public static ListFilterDateRangeConfig defaultDateRange() {
        return new ListFilterDateRangeConfig("SAVED", "Sparat datum", toDate(), fromDate());
    }

    public static ListFilterSelectConfig forwardedSelect() {
        return new ListFilterSelectConfig("FORWARDED", "Vidarebefordrat",
                List.of(
                        ListFilterConfigValue.create(ForwardedType.SHOW_ALL.toString(), ForwardedType.SHOW_ALL.getName(), true),
                        ListFilterConfigValue.create(ForwardedType.FORWARDED.toString(), ForwardedType.FORWARDED.getName(), false),
                        ListFilterConfigValue.create(ForwardedType.NOT_FORWARDED.toString(), ForwardedType.NOT_FORWARDED.getName(), false)
                )
        );
    }

    public static ListFilterSelectConfig draftStatusSelect() {
        return new ListFilterSelectConfig("STATUS", "Utkast", List.of(
                ListFilterConfigValue.create(DraftStatus.SHOW_ALL.toString(), DraftStatus.SHOW_ALL.getName(), true),
                ListFilterConfigValue.create(DraftStatus.INCOMPLETE.toString(), DraftStatus.INCOMPLETE.getName(), false),
                ListFilterConfigValue.create(DraftStatus.COMPLETE.toString(), DraftStatus.COMPLETE.getName(), false),
                ListFilterConfigValue.create(DraftStatus.LOCKED.toString(), DraftStatus.LOCKED.getName(), false)
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
        final var pageSizes = new int[]{10, 20, 50, 100};
        return new ListFilterPageSizeConfig("PAGESIZE", "Visa antal träffar", pageSizes);
    }

    public static ListFilterSelectConfig createStaffSelect(String id, String title, List<StaffListInfo> staffInfo, String defaultHsaId) {
        final var convertedSavedByList = staffInfo.stream().map((info) -> convertStaffInfoIntoSelectFilter(info, defaultHsaId)).collect(Collectors.toList());
        return new ListFilterSelectConfig(id, title, convertedSavedByList);
    }

    private static ListFilterConfigValue convertStaffInfoIntoSelectFilter(StaffListInfo staffListInfo, String defaultHsaId) {
        return ListFilterConfigValue.create(staffListInfo.getHsaId(), staffListInfo.getName(), defaultHsaId.equals(staffListInfo.getHsaId()));
    }
}
