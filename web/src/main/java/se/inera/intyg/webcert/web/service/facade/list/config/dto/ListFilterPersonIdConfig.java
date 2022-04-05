package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public class ListFilterPersonIdConfig extends ListFilterConfig {
    private String placeholder;

    public ListFilterPersonIdConfig(String id, String title, String placeholder) {
        super(ListFilterType.PERSON_ID, id, title);
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }
}
