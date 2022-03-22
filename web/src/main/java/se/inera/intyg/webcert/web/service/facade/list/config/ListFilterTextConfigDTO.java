package se.inera.intyg.webcert.web.service.facade.list.config;

import java.util.List;

public class ListFilterTextConfigDTO extends ListFilterConfigDTO {
    private String placeholder;

    public ListFilterTextConfigDTO(String id, String title, String placeholder) {
        super(ListFilterTypeDTO.TEXT, id, title);
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }
}
