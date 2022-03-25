package se.inera.intyg.webcert.web.service.facade.list.config;

import java.util.List;

public class ListFilterSelectConfigDTO extends ListFilterConfigDTO {
    private List<ListFilterConfigValueDTO> values;

    public ListFilterSelectConfigDTO(String id, String title, List<ListFilterConfigValueDTO> values) {
        super(ListFilterTypeDTO.SELECT, id, title);
        this.values = values;
    }

    public List<ListFilterConfigValueDTO> getValues() {
        return values;
    }

    public void setValues(List<ListFilterConfigValueDTO> values) {
        this.values = values;
    }
}
