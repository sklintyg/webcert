package se.inera.intyg.webcert.web.service.facade.list.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ListFilterTextValueDTO.class, name = "TEXT"),
        @JsonSubTypes.Type(value = ListFilterPersonIdValueDTO.class, name = "PERSON_ID"),
        @JsonSubTypes.Type(value = ListFilterDateRangeValueDTO.class, name = "DATE_RANGE"),
        @JsonSubTypes.Type(value = ListFilterSelectValueDTO.class, name = "SELECT"),
        @JsonSubTypes.Type(value = ListFilterTextValueDTO.class, name = "ORDER"), //CHANGE THIS TYPE!!!!!!!
        @JsonSubTypes.Type(value = ListFilterBooleanValueDTO.class, name = "BOOLEAN")


})

public interface ListFilterValueDTO {
    ListFilterTypeDTO getType();
}
