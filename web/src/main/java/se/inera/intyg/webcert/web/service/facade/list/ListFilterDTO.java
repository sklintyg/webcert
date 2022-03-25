package se.inera.intyg.webcert.web.service.facade.list;

import se.inera.intyg.webcert.web.service.facade.list.config.ListFilterValueDTO;

import java.util.Map;

public class ListFilterDTO {
    private Map<String, ListFilterValueDTO> values;

    public ListFilterDTO() {}

    public ListFilterDTO(Map<String, ListFilterValueDTO> values) {
        this.values = values;
    }

    public Map<String, ListFilterValueDTO> getValues() {
        return values;
    }

    public void setValues(Map<String, ListFilterValueDTO> values) {
        this.values = values;
    }

    public ListFilterValueDTO getValue(String id) {
        if (values.containsKey(id)) {
            return values.get(id);
        }
        return null;
    }
}
