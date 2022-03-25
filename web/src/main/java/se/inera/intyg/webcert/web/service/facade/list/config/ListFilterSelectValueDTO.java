package se.inera.intyg.webcert.web.service.facade.list.config;

public class ListFilterSelectValueDTO implements ListFilterValueDTO {
    private String value;

    @Override
    public ListFilterTypeDTO getType() {
        return ListFilterTypeDTO.SELECT;
    }

    public ListFilterSelectValueDTO() {}

    public ListFilterSelectValueDTO(String value) {this.value = value;}

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
