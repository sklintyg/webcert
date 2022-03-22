package se.inera.intyg.webcert.web.service.facade.list.config;

import java.util.List;

public class ListFilterDateRangeConfigDTO extends ListFilterConfigDTO {
    private ListFilterDateConfigDTO to;
    private ListFilterDateConfigDTO from;

    public ListFilterDateRangeConfigDTO(String id, String title, ListFilterDateConfigDTO to, ListFilterDateConfigDTO from) {
        super(ListFilterTypeDTO.DATE_RANGE, id, title);
        this.to = to;
        this.from = from;
    }

    public ListFilterDateConfigDTO getTo() {
        return to;
    }

    public void setTo(ListFilterDateConfigDTO to) {
        this.to = to;
    }

    public ListFilterDateConfigDTO getFrom() {
        return from;
    }

    public void setFrom(ListFilterDateConfigDTO from) {
        this.from = from;
    }
}
