package se.inera.intyg.webcert.web.service.facade.list.config;

import java.util.List;

public class ListFilterSelectConfigDTO extends ListFilterConfigDTO {
    private List<ListFilterValueDTO> values;

    public ListFilterSelectConfigDTO(String id, String title, List<ListFilterValueDTO> values) {
        super(ListFilterTypeDTO.SELECT, id, title);
        this.values = values;
    }

    public List<ListFilterValueDTO> getValues() {
        return values;
    }

    public void setValues(List<ListFilterValueDTO> values) {
        this.values = values;
    }
}
