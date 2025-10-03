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

package se.inera.intyg.webcert.web.csintegration.message;

import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.validate.SamordningsnummerValidator;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.dto.IncomingComplementDTO;
import se.inera.intyg.webcert.common.dto.IncomingMessageRequestDTO;
import se.inera.intyg.webcert.common.dto.MessageTypeDTO;
import se.inera.intyg.webcert.common.dto.PersonIdDTO;
import se.inera.intyg.webcert.common.dto.PersonIdType;
import se.inera.intyg.webcert.common.dto.SentByDTO;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType.Komplettering;

@Component
public class MessageRequestConverter {

    public IncomingMessageRequestDTO convert(SendMessageToCareType messageToCareType) {
        return IncomingMessageRequestDTO.builder()
            .id(messageToCareType.getMeddelandeId())
            .content(messageToCareType.getMeddelande())
            .type(MessageTypeDTO.valueOf(messageToCareType.getAmne().getCode()))
            .certificateId(messageToCareType.getIntygsId().getExtension())
            .contactInfo(messageToCareType.getSkickatAv().getKontaktInfo())
            .complements(
                messageToCareType.getKomplettering().stream()
                    .map(MessageRequestConverter::toComplementDTO)
                    .collect(Collectors.toList())
            )
            .reminderMessageId(messageToCareType.getPaminnelseMeddelandeId())
            .personId(
                PersonIdDTO.builder()
                    .id(messageToCareType.getPatientPersonId().getExtension())
                    .type(getPersonIdType(messageToCareType))
                    .build()
            )
            .referenceId(messageToCareType.getReferensId())
            .subject(messageToCareType.getRubrik() != null ? messageToCareType.getRubrik() : messageToCareType.getAmne().getDisplayName())
            .lastDateToAnswer(messageToCareType.getSistaDatumForSvar())
            .sentBy(SentByDTO.getByCode(messageToCareType.getSkickatAv().getPart().getCode()))
            .sent(messageToCareType.getSkickatTidpunkt())
            .answerMessageId(messageToCareType.getSvarPa() != null ? messageToCareType.getSvarPa().getMeddelandeId() : null)
            .answerReferenceId(messageToCareType.getSvarPa() != null ? messageToCareType.getSvarPa().getReferensId() : null)
            .build();
    }

    private PersonIdType getPersonIdType(SendMessageToCareType messageToCareType) {
        return SamordningsnummerValidator.isSamordningsNummer(
            Optional.of(toPatientId(messageToCareType.getPatientPersonId().getExtension())))
            ? PersonIdType.COORDINATION_NUMBER : PersonIdType.PERSONAL_IDENTITY_NUMBER;
    }

    private Personnummer toPatientId(String patientId) {
        return Personnummer.createPersonnummer(patientId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    String.format("PatientId has wrong format: '%s'", patientId)
                )
            );
    }

    private static IncomingComplementDTO toComplementDTO(Komplettering complement) {
        return IncomingComplementDTO.builder()
            .questionId(complement.getFrageId())
            .instance(complement.getInstans())
            .content(complement.getText())
            .build();
    }
}
