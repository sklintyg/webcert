package se.inera.intyg.webcert.web.service.facade.list.config;

public class ListFilterNumberValueDTO implements ListFilterValueDTO {
    private int value;

    public ListFilterNumberValueDTO(){}

    public ListFilterNumberValueDTO(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public ListFilterTypeDTO getType() {
        return ListFilterTypeDTO.NUMBER;
    }
}
