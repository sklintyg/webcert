package se.inera.intyg.webcert.web.service.facade.list.config;

public class ListFilterPageSizeConfigDTO extends ListFilterConfigDTO {
    private int[] pageSizes;

    public ListFilterPageSizeConfigDTO(String id, String title, int[] pageSizes) {
        super(ListFilterTypeDTO.PAGESIZE, id, title);
        this.pageSizes = pageSizes;
    }

    public int[] getPageSizes() {
        return pageSizes;
    }

    public void setPageSizes(int[] pageSizes) {
        this.pageSizes = pageSizes;
    }
}
