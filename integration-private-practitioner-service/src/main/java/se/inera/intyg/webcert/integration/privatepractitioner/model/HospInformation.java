package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@JsonDeserialize(builder = HospInformation.HospInformationBuilder.class)
public class HospInformation {

    String personId;
    String personalPrescriptionCode;
    List<Code> licensedHealthcareProfessions;
    List<Code> specialities;

    @JsonPOJOBuilder(withPrefix = "")
    public static class HospInformationBuilder {

    }

}
