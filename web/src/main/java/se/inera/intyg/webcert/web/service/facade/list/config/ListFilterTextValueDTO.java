package se.inera.intyg.webcert.web.service.facade.list.config;

public class ListFilterTextValueDTO implements ListFilterValueDTO {
    private String value;

    @Override
    public ListFilterTypeDTO getType() {
        return ListFilterTypeDTO.TEXT;
    }

    public ListFilterTextValueDTO() {}

    public ListFilterTextValueDTO(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
