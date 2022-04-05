package se.inera.intyg.webcert.web.service.facade.list.config.dto;

import java.time.LocalDateTime;

public class ListFilterDateRangeValue implements ListFilterValue {
    private LocalDateTime to;
    private LocalDateTime from;

    public ListFilterDateRangeValue(){}

    public ListFilterDateRangeValue(LocalDateTime to, LocalDateTime from) {
        this.to = to;
        this.from = from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public void setTo(LocalDateTime to) {
        this.to = to;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    @Override
    public ListFilterType getType() {
        return ListFilterType.DATE_RANGE;
    }
}
