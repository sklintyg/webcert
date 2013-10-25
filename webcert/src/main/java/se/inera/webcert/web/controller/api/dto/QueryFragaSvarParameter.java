package se.inera.webcert.web.controller.api.dto;

import se.inera.webcert.persistence.fragasvar.repository.FragaSvarFilter;

public class QueryFragaSvarParameter {
    private int startFrom;
    private int pageSize;
    private FragaSvarFilter filter;

    public int getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(int startFrom) {
        this.startFrom = startFrom;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public FragaSvarFilter getFilter() {
        return filter;
    }

    public void setFilter(FragaSvarFilter filter) {
        this.filter = filter;
    }

}
