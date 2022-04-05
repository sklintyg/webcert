package se.inera.intyg.webcert.web.service.facade.list;

import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterValue;

import java.util.Map;

public class ListFilterDTO {
    private Map<String, ListFilterValue> values;

    public ListFilterDTO() {}

    public ListFilterDTO(Map<String, ListFilterValue> values) {
        this.values = values;
    }

    public Map<String, ListFilterValue> getValues() {
        return values;
    }

    public void setValues(Map<String, ListFilterValue> values) {
        this.values = values;
    }

    public ListFilterValue getValue(String id) {
        if (values.containsKey(id)) {
            return values.get(id);
        }
        return null;
    }
}
