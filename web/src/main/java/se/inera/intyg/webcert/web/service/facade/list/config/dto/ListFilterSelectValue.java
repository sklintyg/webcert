package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public class ListFilterSelectValue implements ListFilterValue {
    private String value;

    @Override
    public ListFilterType getType() {
        return ListFilterType.SELECT;
    }

    public ListFilterSelectValue() {}

    public ListFilterSelectValue(String value) {this.value = value;}

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}