package se.inera.intyg.webcert.web.service.facade.list.config;

public class ListFilterPersonIdConfigDTO extends ListFilterConfigDTO {
    private String placeholder;

    public ListFilterPersonIdConfigDTO(String id, String title, String placeholder) {
        super(ListFilterTypeDTO.PERSON_ID, id, title);
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }
}
