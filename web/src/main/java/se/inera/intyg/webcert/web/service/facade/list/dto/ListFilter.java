package se.inera.intyg.webcert.web.service.facade.list.dto;

import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterValue;

import java.util.HashMap;
import java.util.Map;

public class ListFilter {
    private Map<String, ListFilterValue> values;

    public ListFilter() {
        values = new HashMap<>();
    }

    public ListFilter(Map<String, ListFilterValue> values) {
        this.values = values;
    }

    public Map<String, ListFilterValue> getValues() {
        return values;
    }

    public void setValues(Map<String, ListFilterValue> values) {
        this.values = values;
    }

    public void addValue(ListFilterValue value, String key) {
        values.put(key, value);
    }

    public ListFilterValue getValue(String id) {
        if (values.containsKey(id)) {
            return values.get(id);
        }
        return null;
    }
}
