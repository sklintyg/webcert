package se.inera.intyg.webcert.web.service.facade.list.config;

import java.time.LocalDateTime;

public class ListFilterDateRangeValueDTO implements ListFilterValueDTO {
    private LocalDateTime to;
    private LocalDateTime from;

    public ListFilterDateRangeValueDTO(){}

    public ListFilterDateRangeValueDTO(LocalDateTime to, LocalDateTime from) {
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
    public ListFilterTypeDTO getType() {
        return ListFilterTypeDTO.DATE_RANGE;
    }
}
