package se.inera.intyg.webcert.web.service.facade.list.config;

import java.util.List;

public class ListConfigDTO {

    private List<ListFilterConfigDTO> filters;
    private String title;
    private List<Integer> pageSizes;
    private String openCertificateTooltip;
    private String searchCertificateTooltip;
    private TableHeadingDTO[] tableHeadings;

    public ListConfigDTO(){}

    public ListConfigDTO(List<ListFilterConfigDTO> filters, String title, List<Integer> pageSizes, String openCertificateTooltip, String searchCertificateTooltip, TableHeadingDTO[] tableHeadings) {
        this.filters = filters;
        this.title = title;
        this.pageSizes = pageSizes;
        this.openCertificateTooltip = openCertificateTooltip;
        this.searchCertificateTooltip = searchCertificateTooltip;
        this.tableHeadings = tableHeadings;
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

    public String getOpenCertificateTooltip() {
        return openCertificateTooltip;
    }

    public void setOpenCertificateTooltip(String openCertificateTooltip) {
        this.openCertificateTooltip = openCertificateTooltip;
    }

    public TableHeadingDTO[] getTableHeadings() {
        return tableHeadings;
    }

    public void setTableHeadings(TableHeadingDTO[] tableHeadings) {
        this.tableHeadings = tableHeadings;
    }


    public String getSearchCertificateTooltip() {
        return searchCertificateTooltip;
    }

    public void setSearchCertificateTooltip(String searchCertificateTooltip) {
        this.searchCertificateTooltip = searchCertificateTooltip;
    }
}
