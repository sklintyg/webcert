package se.inera.intyg.webcert.web.service.facade.list.config.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ListFilterTextValue.class, name = "TEXT"),
        @JsonSubTypes.Type(value = ListFilterPersonIdValue.class, name = "PERSON_ID"),
        @JsonSubTypes.Type(value = ListFilterDateRangeValue.class, name = "DATE_RANGE"),
        @JsonSubTypes.Type(value = ListFilterSelectValue.class, name = "SELECT"),
        @JsonSubTypes.Type(value = ListFilterTextValue.class, name = "ORDER"), //CHANGE THIS TYPE!!!!!!!
        @JsonSubTypes.Type(value = ListFilterBooleanValue.class, name = "BOOLEAN"),
        @JsonSubTypes.Type(value = ListFilterNumberValue.class, name = "NUMBER")


})

public interface ListFilterValue {
    ListFilterType getType();
}
