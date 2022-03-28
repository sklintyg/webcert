package se.inera.intyg.webcert.web.service.facade.list.config;

public class ListFilterBooleanConfigDTO extends ListFilterConfigDTO {
    private boolean defaultValue;

    public ListFilterBooleanConfigDTO(String id, String title, boolean defaultValue) {
        super(ListFilterTypeDTO.BOOLEAN, id, title);
        this.defaultValue = defaultValue;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }
}
