package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public class ListFilterTextConfig extends ListFilterConfig {
    private String placeholder;

    public ListFilterTextConfig(String id, String title, String placeholder) {
        super(ListFilterType.TEXT, id, title);
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }
}
