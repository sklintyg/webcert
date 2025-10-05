/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.csintegration.integration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.common.dto.PersonIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.MessageQueryCriteriaDTO.MessageQueryCriteriaDTOBuilder;
import se.inera.intyg.webcert.web.service.facade.list.dto.QuestionSenderType;

@JsonDeserialize(builder = MessageQueryCriteriaDTOBuilder.class)
@Value
@Builder
public class MessageQueryCriteriaDTO {

    List<String> issuedOnUnitIds;
    Boolean forwarded;
    QuestionSenderType senderType;
    LocalDateTime sentDateFrom;
    LocalDateTime sentDateTo;
    String issuedByStaffId;
    PersonIdDTO patientId;

    @JsonPOJOBuilder(withPrefix = "")
    public static class MessageQueryCriteriaDTOBuilder {

    }
}
