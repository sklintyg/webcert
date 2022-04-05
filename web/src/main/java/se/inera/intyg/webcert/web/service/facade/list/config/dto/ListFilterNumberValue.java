package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public class ListFilterNumberValue implements ListFilterValue {
    private int value;

    public ListFilterNumberValue(){}

    public ListFilterNumberValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public ListFilterType getType() {
        return ListFilterType.NUMBER;
    }
}
