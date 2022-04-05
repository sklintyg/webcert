package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public class ListFilterOrderConfig extends ListFilterConfig {
    private ListColumnType defaultValue;

    public ListFilterOrderConfig(String id, String title, ListColumnType defaultValue) {
        super(ListFilterType.ORDER, id, title);
        this.defaultValue = defaultValue;
    }

    public ListColumnType getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(ListColumnType defaultValue) {
        this.defaultValue = defaultValue;
    }
}
