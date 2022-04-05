package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public class ListFilterBooleanConfig extends ListFilterConfig {
    private boolean defaultValue;

    public ListFilterBooleanConfig(String id, String title, boolean defaultValue) {
        super(ListFilterType.BOOLEAN, id, title);
        this.defaultValue = defaultValue;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }
}
