package se.inera.intyg.webcert.web.service.facade.list.config;

public class ListFilterPersonIdValueDTO implements ListFilterValueDTO {
    private String value;

    @Override
    public ListFilterTypeDTO getType() {
        return ListFilterTypeDTO.PERSON_ID;
    }

    public ListFilterPersonIdValueDTO() {
    }

    public ListFilterPersonIdValueDTO(String id, String value) { this.value = value; }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
