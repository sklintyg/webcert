package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public class ListFilterPageSizeConfig extends ListFilterConfig {
    private int[] pageSizes;

    public ListFilterPageSizeConfig(String id, String title, int[] pageSizes) {
        super(ListFilterType.PAGESIZE, id, title);
        this.pageSizes = pageSizes;
    }

    public int[] getPageSizes() {
        return pageSizes;
    }

    public void setPageSizes(int[] pageSizes) {
        this.pageSizes = pageSizes;
    }
}
