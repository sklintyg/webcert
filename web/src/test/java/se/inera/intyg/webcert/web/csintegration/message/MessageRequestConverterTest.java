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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.webcert.common.dto.IncomingComplementDTO;
import se.inera.intyg.webcert.common.dto.MessageTypeDTO;
import se.inera.intyg.webcert.common.dto.PersonIdDTO;
import se.inera.intyg.webcert.common.dto.PersonIdType;
import se.inera.intyg.webcert.common.dto.SentByDTO;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType.Komplettering;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType.SkickatAv;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Part;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.MeddelandeReferens;

class MessageRequestConverterTest {

    private static final String FKASSA = "FKASSA";
    private static final String MESSAGE_ID = "messageId";
    private static final String PATIENT_ID = "191212121212";
    private static final String COORDINATION_PATIENT_ID = "191212721212";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String MESSAGE_TYPE_COMPLEMENT = MessageTypeDTO.KOMPLT.toString();
    private static final String MESSAGE = "message";
    private static final String CONTACT_INFO = "contactInfo";
    private static final String COMPLEMENT_QUESTION_ID = "complementQuestionId";
    private static final int INSTANCE = 1;
    private static final String COMEPLEMENT_CONTENT = "comeplementContent";
    private static final String REMINDER_MESSAGE_ID = "reminderMessageId";
    private static final String REFERENCE_ID = "referenceId";
    private static final String SUBJECT = "subject";
    private static final LocalDate LAST_DATE_TO_ANSWER = LocalDate.now();
    private static final LocalDateTime SENT_TIMESTAMP = LocalDateTime.now();
    private static final String ANSWER_MESSAGE_ID = "answerMessageId";
    private static final String ANSWER_REFERENCE_ID = "answerReferenceId";
    private static final String HEADING = "heading";
    private MessageRequestConverter messageRequestConverter;
    private SendMessageToCareType sendMessageToCareType;

    @BeforeEach
    void setUp() {
        messageRequestConverter = new MessageRequestConverter();
        sendMessageToCareType = getSendMessageToCareTypeRequest();
    }

    @Test
    void shallIncludeId() {
        assertEquals(MESSAGE_ID, messageRequestConverter.convert(sendMessageToCareType).getId());
    }

    @Test
    void shallIncludeContent() {
        assertEquals(MESSAGE, messageRequestConverter.convert(sendMessageToCareType).getContent());
    }

    @Test
    void shallIncludeType() {
        assertEquals(MessageTypeDTO.KOMPLT, messageRequestConverter.convert(sendMessageToCareType).getType());
    }

    @Test
    void shallIncludeCertificateId() {
        assertEquals(CERTIFICATE_ID, messageRequestConverter.convert(sendMessageToCareType).getCertificateId());
    }

    @Test
    void shallIncludeContactInfo() {
        assertEquals(CONTACT_INFO, messageRequestConverter.convert(sendMessageToCareType).getContactInfo().get(0));
    }

    @Test
    void shallIncludeComplements() {
        final var expectedComplement = IncomingComplementDTO.builder()
            .questionId(COMPLEMENT_QUESTION_ID)
            .instance(INSTANCE)
            .content(COMEPLEMENT_CONTENT)
            .build();

        assertEquals(expectedComplement, messageRequestConverter.convert(sendMessageToCareType).getComplements().get(0));
    }

    @Test
    void shallIncludeReminderMessageId() {
        assertEquals(REMINDER_MESSAGE_ID, messageRequestConverter.convert(sendMessageToCareType).getReminderMessageId());
    }

    @Test
    void shallIncludePersonIdWithTypePersonalIdentityNumber() {
        final var expectedPersonId = PersonIdDTO.builder()
            .type(PersonIdType.PERSONAL_IDENTITY_NUMBER)
            .id(PATIENT_ID)
            .build();

        assertEquals(expectedPersonId, messageRequestConverter.convert(sendMessageToCareType).getPersonId());
    }

    @Test
    void shallIncludePersonIdWithTypeCoordinationNumber() {
        final var expectedPersonId = PersonIdDTO.builder()
            .type(PersonIdType.COORDINATION_NUMBER)
            .id(COORDINATION_PATIENT_ID)
            .build();

        sendMessageToCareType.setPatientPersonId(createPersonId(COORDINATION_PATIENT_ID));

        assertEquals(expectedPersonId, messageRequestConverter.convert(sendMessageToCareType).getPersonId());
    }

    @Test
    void shallIncludeReferenceId() {
        assertEquals(REFERENCE_ID, messageRequestConverter.convert(sendMessageToCareType).getReferenceId());
    }

    @Test
    void shallIncludeSubject() {
        assertEquals(HEADING, messageRequestConverter.convert(sendMessageToCareType).getSubject());
    }

    @Test
    void shallIncludeLastDateToAnswer() {
        assertEquals(LAST_DATE_TO_ANSWER, messageRequestConverter.convert(sendMessageToCareType).getLastDateToAnswer());
    }

    @Test
    void shallIncludeSentBy() {
        assertEquals(SentByDTO.FK, messageRequestConverter.convert(sendMessageToCareType).getSentBy());
    }

    @Test
    void shallIncludeSent() {
        assertEquals(SENT_TIMESTAMP, messageRequestConverter.convert(sendMessageToCareType).getSent());
    }

    @Test
    void shallExcludeAnswerMessageIdIfSvarPaIsNull() {
        assertNull(messageRequestConverter.convert(sendMessageToCareType).getAnswerMessageId());
    }

    @Test
    void shallExcludeAnswerReferenceIdIfSvarPaIsNull() {
        assertNull(messageRequestConverter.convert(sendMessageToCareType).getAnswerReferenceId());
    }

    @Test
    void shallIncludeAnswerMessageIdIfSvarPaIsNotNull() {
        final var messageRef = new MeddelandeReferens();
        messageRef.setMeddelandeId(ANSWER_MESSAGE_ID);
        messageRef.setReferensId(ANSWER_REFERENCE_ID);
        sendMessageToCareType.setSvarPa(messageRef);
        assertEquals(ANSWER_MESSAGE_ID, messageRequestConverter.convert(sendMessageToCareType).getAnswerMessageId());
    }

    @Test
    void shallIncludeAnswerReferenceIdIfSvarPaIsNotNull() {
        final var messageRef = new MeddelandeReferens();
        messageRef.setMeddelandeId(ANSWER_MESSAGE_ID);
        messageRef.setReferensId(ANSWER_REFERENCE_ID);
        sendMessageToCareType.setSvarPa(messageRef);
        assertEquals(ANSWER_REFERENCE_ID, messageRequestConverter.convert(sendMessageToCareType).getAnswerReferenceId());
    }

    private SendMessageToCareType getSendMessageToCareTypeRequest() {
        final var res = new SendMessageToCareType();
        res.setAmne(new Amneskod());
        res.getAmne().setCode(MESSAGE_TYPE_COMPLEMENT);
        res.getAmne().setDisplayName(SUBJECT);
        res.setIntygsId(createIntygsId());
        res.setMeddelandeId(MESSAGE_ID);
        res.setMeddelande(MESSAGE);
        res.setPatientPersonId(createPersonId(PATIENT_ID));
        res.setSkickatAv(createSkickadAv());
        res.getKomplettering().add(createComplement());
        res.setPaminnelseMeddelandeId(REMINDER_MESSAGE_ID);
        res.setReferensId(REFERENCE_ID);
        res.setRubrik(HEADING);
        res.setSistaDatumForSvar(LAST_DATE_TO_ANSWER);
        res.setSkickatTidpunkt(SENT_TIMESTAMP);
        return res;
    }

    private static Komplettering createComplement() {
        final var complement = new Komplettering();
        complement.setFrageId(COMPLEMENT_QUESTION_ID);
        complement.setInstans(INSTANCE);
        complement.setText(COMEPLEMENT_CONTENT);
        return complement;
    }

    private SkickatAv createSkickadAv() {
        final var res = new SkickatAv();
        res.setPart(new Part());
        res.getPart().setCode(FKASSA);
        res.getKontaktInfo().add(CONTACT_INFO);
        return res;
    }

    private PersonId createPersonId(String personId) {
        final var res = new PersonId();
        res.setExtension(personId);
        res.setRoot("");
        return res;
    }

    private IntygId createIntygsId() {
        final var res = new IntygId();
        res.setExtension(CERTIFICATE_ID);
        res.setRoot("");
        return res;
    }
}
