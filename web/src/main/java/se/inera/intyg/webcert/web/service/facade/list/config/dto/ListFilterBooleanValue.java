package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public class ListFilterBooleanValue implements ListFilterValue {
    private boolean value;

    public ListFilterBooleanValue() {}

    public ListFilterBooleanValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public ListFilterType getType() {
        return ListFilterType.BOOLEAN;
    }
}
