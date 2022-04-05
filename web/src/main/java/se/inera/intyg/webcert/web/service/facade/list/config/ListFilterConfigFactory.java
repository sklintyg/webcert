package se.inera.intyg.webcert.web.service.facade.list.config;

import se.inera.intyg.webcert.web.service.facade.list.DraftStatusDTO;
import se.inera.intyg.webcert.web.service.facade.list.ForwardedTypeDTO;

import java.util.List;
import java.util.stream.Collectors;

public class ListFilterConfigFactory {
    public static ListFilterPersonIdConfigDTO defaultPersonId() {
        return new ListFilterPersonIdConfigDTO("PATIENT_ID", "Patient", "åååå-mm-dd");
    }

    public static ListFilterDateConfigDTO toDate() {
        return new ListFilterDateConfigDTO("TO", "Till");
    }

    public static ListFilterDateConfigDTO fromDate() {
        return new ListFilterDateConfigDTO("FROM", "Från");
    }

    public static ListFilterDateRangeConfigDTO defaultDateRange() {
        return new ListFilterDateRangeConfigDTO("SAVED", "Sparat datum", toDate(), fromDate());
    }

    public static ListFilterSelectConfigDTO forwardedSelect() {
        return new ListFilterSelectConfigDTO("FORWARDED", "Vidarebefordrat",
                List.of(
                        ListFilterConfigValueDTO.create(ForwardedTypeDTO.SHOW_ALL.toString(), ForwardedTypeDTO.SHOW_ALL.getName(), true),
                        ListFilterConfigValueDTO.create(ForwardedTypeDTO.FORWARDED.toString(), ForwardedTypeDTO.FORWARDED.getName(), false),
                        ListFilterConfigValueDTO.create(ForwardedTypeDTO.NOT_FORWARDED.toString(),ForwardedTypeDTO.NOT_FORWARDED.getName(), false)
                )
        );
    }

    public static ListFilterSelectConfigDTO draftStatusSelect() {
        return new ListFilterSelectConfigDTO("STATUS", "Utkast", List.of(
                ListFilterConfigValueDTO.create(DraftStatusDTO.SHOW_ALL.toString(), DraftStatusDTO.SHOW_ALL.getName(), true),
                ListFilterConfigValueDTO.create(DraftStatusDTO.INCOMPLETE.toString(), DraftStatusDTO.INCOMPLETE.getName(), false),
                ListFilterConfigValueDTO.create(DraftStatusDTO.COMPLETE.toString(), DraftStatusDTO.COMPLETE.getName(), false),
                ListFilterConfigValueDTO.create(DraftStatusDTO.LOCKED.toString(), DraftStatusDTO.LOCKED.getName(), false)
        )
        );
    }

    public static ListFilterOrderConfigDTO orderBy(ListColumnTypeDTO defaultOrder) {
        return new ListFilterOrderConfigDTO("ORDER_BY", "", defaultOrder);
    }

    public static ListFilterBooleanConfigDTO ascending() {
        return new ListFilterBooleanConfigDTO("ASCENDING", "", false);
    }
    
    public static ListFilterPageSizeConfigDTO pageSize() {
        final var pageSizes = new int[]{10, 20, 50, 100};
        return new ListFilterPageSizeConfigDTO("PAGESIZE", "Visa antal träffar", pageSizes);
    }

    public static ListFilterSelectConfigDTO createStaffSelect(String id, String title, List<StaffListInfoDTO> staffInfo, String defaultHsaId) {
        final var convertedSavedByList = staffInfo.stream().map((info) -> convertStaffInfoIntoSelectFilter(info, defaultHsaId)).collect(Collectors.toList());
        return new ListFilterSelectConfigDTO(id, title, convertedSavedByList);
    }

    private static ListFilterConfigValueDTO convertStaffInfoIntoSelectFilter(StaffListInfoDTO staffListInfoDTO, String defaultHsaId) {
        return ListFilterConfigValueDTO.create(staffListInfoDTO.getHsaId(), staffListInfoDTO.getName(), defaultHsaId.equals(staffListInfoDTO.getHsaId()));
    }
}
