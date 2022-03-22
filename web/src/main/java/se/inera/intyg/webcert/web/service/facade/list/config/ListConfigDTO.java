package se.inera.intyg.webcert.web.service.facade.list.config;

import java.util.List;

public class ListConfigDTO {

    private List<ListFilterConfigDTO> filters;
    private String title;
    private List<Integer> pageSizes;

    public ListConfigDTO(){}

    public ListConfigDTO(List<ListFilterConfigDTO> filters, String title, List<Integer> pageSizes) {
        this.filters = filters;
        this.title = title;
        this.pageSizes = pageSizes;
    }

    public List<ListFilterConfigDTO> getFilters() {
        return filters;
    }

    public void setFilters(List<ListFilterConfigDTO> filters) {
        this.filters = filters;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Integer> getPageSizes() {
        return pageSizes;
    }

    public void setPageSizes(List<Integer> pageSizes) {
        this.pageSizes = pageSizes;
    }
}
