package se.inera.intyg.webcert.web.service.facade.list.config;

public class ListFilterBooleanValueDTO implements ListFilterValueDTO {
    private boolean value;

    public ListFilterBooleanValueDTO() {}

    public ListFilterBooleanValueDTO(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public ListFilterTypeDTO getType() {
        return ListFilterTypeDTO.BOOLEAN;
    }
}
