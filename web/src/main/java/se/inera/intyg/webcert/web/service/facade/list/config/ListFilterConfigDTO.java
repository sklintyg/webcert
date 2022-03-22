package se.inera.intyg.webcert.web.service.facade.list.config;

import java.util.List;

public class ListFilterConfigDTO {
    private ListFilterTypeDTO type;
    private String id;
    private String title;

    public ListFilterConfigDTO(ListFilterTypeDTO type, String id, String title) {
        this.type = type;
        this.id = id;
        this.title = title;
    }

    public ListFilterTypeDTO getType() {
        return type;
    }

    public void setType(ListFilterTypeDTO type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
