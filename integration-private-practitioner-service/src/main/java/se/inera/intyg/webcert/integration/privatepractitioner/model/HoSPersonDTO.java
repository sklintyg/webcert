package se.inera.intyg.webcert.integration.privatepractitioner.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = HoSPersonDTO.HoSPersonTypeBuilder.class)
public class HoSPersonDTO {

    HsaIdDTO hsaId;
    PersonIdDTO personId;
    String name;
    List<Position> positions = new ArrayList<>();
    List<Speciality> specialities = new ArrayList<>();
    List<LicensedHealthcareProfession> licensedHealthcareProfessions = new ArrayList<>();
    String personalPrescriptionCode;
    boolean godkandAnvandare;
    EnhetsTyp enhet;

    @JsonPOJOBuilder(withPrefix = "")
    public static class HoSPersonTypeBuilder {

    }

}
