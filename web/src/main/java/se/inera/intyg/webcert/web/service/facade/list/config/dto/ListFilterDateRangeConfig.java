package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public class ListFilterDateRangeConfig extends ListFilterConfig {
    private ListFilterDateConfig to;
    private ListFilterDateConfig from;

    public ListFilterDateRangeConfig(String id, String title, ListFilterDateConfig to, ListFilterDateConfig from) {
        super(ListFilterType.DATE_RANGE, id, title);
        this.to = to;
        this.from = from;
    }

    public ListFilterDateConfig getTo() {
        return to;
    }

    public void setTo(ListFilterDateConfig to) {
        this.to = to;
    }

    public ListFilterDateConfig getFrom() {
        return from;
    }

    public void setFrom(ListFilterDateConfig from) {
        this.from = from;
    }
}
