package se.inera.intyg.webcert.web.service.facade.list.config.dto;

import java.util.List;

public class ListFilterSelectConfig extends ListFilterConfig {
    private List<ListFilterConfigValue> values;

    public ListFilterSelectConfig(String id, String title, List<ListFilterConfigValue> values) {
        super(ListFilterType.SELECT, id, title);
        this.values = values;
    }

    public List<ListFilterConfigValue> getValues() {
        return values;
    }

    public void setValues(List<ListFilterConfigValue> values) {
        this.values = values;
    }
}
