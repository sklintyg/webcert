package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public class ListFilterConfig {
    private ListFilterType type;
    private String id;
    private String title;

    public ListFilterConfig(ListFilterType type, String id, String title) {
        this.type = type;
        this.id = id;
        this.title = title;
    }

    public ListFilterType getType() {
        return type;
    }

    public void setType(ListFilterType type) {
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
