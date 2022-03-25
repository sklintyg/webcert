package se.inera.intyg.webcert.web.service.facade.list.config;

public class ListFilterOrderConfigDTO extends ListFilterConfigDTO {
    private ListColumnTypeDTO defaultValue;

    public ListFilterOrderConfigDTO(String id, String title, ListColumnTypeDTO defaultValue) {
        super(ListFilterTypeDTO.ORDER, id, title);
        this.defaultValue = defaultValue;
    }

    public ListColumnTypeDTO getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(ListColumnTypeDTO defaultValue) {
        this.defaultValue = defaultValue;
    }
}
