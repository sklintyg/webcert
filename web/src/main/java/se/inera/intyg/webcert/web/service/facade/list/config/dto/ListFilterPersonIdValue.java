package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public class ListFilterPersonIdValue implements ListFilterValue {
    private String value;

    @Override
    public ListFilterType getType() {
        return ListFilterType.PERSON_ID;
    }

    public ListFilterPersonIdValue() {
    }

    public ListFilterPersonIdValue(String id, String value) { this.value = value; }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
