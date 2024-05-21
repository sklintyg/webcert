package se.inera.intyg.webcert.web.csintegration.message.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.web.csintegration.message.dto.IncomingMessageRequestDTO.IncomingMessageRequestDTOBuilder;
import se.inera.intyg.webcert.web.csintegration.patient.PersonIdDTO;

@JsonDeserialize(builder = IncomingMessageRequestDTOBuilder.class)
@Value
@Builder
public class IncomingMessageRequestDTO {

    String id;
    String referenceId;
    String certificateId;
    MessageTypeDTO type;
    SentByDTO sentBy;
    LocalDateTime sent;
    PersonIdDTO personId;
    List<String> contactInfo;
    String subject;
    String content;
    String answerMessageId;
    String answerReferenceId;
    String reminderMessageId;
    List<IncomingComplementDTO> complements;
    LocalDate lastDateToAnswer;

    @JsonPOJOBuilder(withPrefix = "")
    public static class IncomingMessageRequestDTOBuilder {

    }
}
