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

package se.inera.intyg.webcert.web.csintegration.certificate;

import com.google.common.collect.Streams;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent.Source;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEventType;
import se.inera.intyg.infra.intyginfo.dto.WcIntygInfo;
import se.inera.intyg.webcert.web.service.intyginfo.GetIntygInfoEventsService;

@Component
@RequiredArgsConstructor
public class CertificateToIntygInfoConverter {

    private final GetIntygInfoEventsService getIntygInfoEventsService;
    private final CertificateRelationsToIntygInfoEventsConverter certificateRelationsToIntygInfoEventsConverter;

    public WcIntygInfo convert(Certificate certificate, List<Question> questions) {
        final var metadata = certificate.getMetadata();

        final var wcIntygInfo = new WcIntygInfo();

        wcIntygInfo.setIntygId(metadata.getId());
        wcIntygInfo.setIntygType(metadata.getType());
        wcIntygInfo.setIntygVersion(metadata.getTypeVersion());
        wcIntygInfo.setSignedDate(metadata.getSigned());
        wcIntygInfo.setSentToRecipient(metadata.isSent() && metadata.getRecipient() != null
            ? metadata.getRecipient().getSent() : null);
        wcIntygInfo.setNumberOfRecipients(metadata.getRecipient() != null ? 1 : 0);
        wcIntygInfo.setSignedByName(metadata.getIssuedBy().getFullName());
        wcIntygInfo.setSignedByHsaId(metadata.getIssuedBy().getPersonId());
        wcIntygInfo.setCareUnitName(metadata.getCareUnit().getUnitName());
        wcIntygInfo.setCareUnitHsaId(metadata.getCareUnit().getUnitId());
        wcIntygInfo.setCareGiverName(metadata.getCareProvider().getUnitName());
        wcIntygInfo.setCareGiverHsaId(metadata.getCareProvider().getUnitId());
        wcIntygInfo.setTestCertificate(metadata.isTestCertificate());
        wcIntygInfo.setCreatedInWC(true);
        wcIntygInfo.setDraftCreated(metadata.getCreated());

        final var complements = getComplements(questions);
        final var complementsAnswered = getComplementsAnswered(questions);
        final var adminQuestionsSent = getAdminQuestionsSent(questions);
        final var adminQuestionsReceived = getAdminQuestionsReceived(questions);
        final var adminQuestionsSentAnswered = getAdminQuestionsSentAnswered(questions);
        final var adminQuestionsReceivedAnswered = getAdminQuestionsReceivedAnswered(questions);

        wcIntygInfo.setKompletteringar(complements.size());
        wcIntygInfo.setKompletteringarAnswered(complementsAnswered.size());
        wcIntygInfo.setAdministrativaFragorSent(adminQuestionsSent.size());
        wcIntygInfo.setAdministrativaFragorSentAnswered(adminQuestionsSentAnswered.size());
        wcIntygInfo.setAdministrativaFragorReceived(adminQuestionsReceived.size());
        wcIntygInfo.setAdministrativaFragorReceivedAnswered(adminQuestionsReceivedAnswered.size());

        final var notificationEvents = getIntygInfoEventsService.get(metadata.getId());
        final var relationEvents = certificateRelationsToIntygInfoEventsConverter.convert(certificate);
        final var createdEvent = new IntygInfoEvent(Source.WEBCERT, certificate.getMetadata().getCreated(), IntygInfoEventType.IS001);
        createdEvent.addData("hsaId", certificate.getMetadata().getCreatedBy().getPersonId());
        createdEvent.addData("name", certificate.getMetadata().getCreatedBy().getFullName());

        IntygInfoEvent signedEvent = null;
        if (certificate.getMetadata().getSigned() != null) {
            signedEvent = new IntygInfoEvent(Source.WEBCERT, certificate.getMetadata().getSigned(), IntygInfoEventType.IS004);
            signedEvent.addData("hsaId", certificate.getMetadata().getIssuedBy().getPersonId());
            signedEvent.addData("name", certificate.getMetadata().getIssuedBy().getFullName());
        }
        IntygInfoEvent sentEvent = null;
        if (certificate.getMetadata().isSent() && certificate.getMetadata().getRecipient() != null) {
            sentEvent = new IntygInfoEvent(Source.WEBCERT, certificate.getMetadata().getRecipient().getSent(), IntygInfoEventType.IS006);
            sentEvent.addData("intygsmottagare", certificate.getMetadata().getRecipient().getName());

        }
        IntygInfoEvent revokedEvent = null;
        if (certificate.getMetadata().getRevokedAt() != null) {
            revokedEvent = new IntygInfoEvent(Source.WEBCERT, certificate.getMetadata().getRevokedAt(), IntygInfoEventType.IS009);
            revokedEvent.addData("hsaId", certificate.getMetadata().getRevokedBy().getPersonId());
            revokedEvent.addData("name", certificate.getMetadata().getRevokedBy().getFullName());
        }
        IntygInfoEvent readyForSignEvent = null;
        if (certificate.getMetadata().getReadyForSign() != null) {
            readyForSignEvent = new IntygInfoEvent(Source.WEBCERT, certificate.getMetadata().getReadyForSign(), IntygInfoEventType.IS018);
        }

        wcIntygInfo.setEvents(
            Streams.concat(notificationEvents.stream(), relationEvents.stream()).collect(Collectors.toList())
        );
        wcIntygInfo.getEvents().add(createdEvent);
        if (signedEvent != null) {
            wcIntygInfo.getEvents().add(signedEvent);
        }
        if (sentEvent != null) {
            wcIntygInfo.getEvents().add(sentEvent);
        }
        if (revokedEvent != null) {
            wcIntygInfo.getEvents().add(revokedEvent);
        }
        if (readyForSignEvent != null) {
            wcIntygInfo.getEvents().add(readyForSignEvent);
        }

        return wcIntygInfo;
    }

    private static List<Question> getComplements(List<Question> questions) {
        return questions.stream()
            .filter(question -> question.getType() == QuestionType.COMPLEMENT)
            .toList();
    }

    private static List<Question> getComplementsAnswered(List<Question> questions) {
        return questions.stream()
            .filter(question -> question.getType() == QuestionType.COMPLEMENT
                && question.isHandled())
            .toList();
    }

    private static List<Question> getAdminQuestionsSent(List<Question> questions) {
        return questions.stream()
            .filter(question -> question.getType() != QuestionType.COMPLEMENT
                && question.getAuthor().equalsIgnoreCase("WC"))
            .toList();
    }

    private static List<Question> getAdminQuestionsReceived(List<Question> questions) {
        return questions.stream()
            .filter(question -> question.getType() != QuestionType.COMPLEMENT
                && !question.getAuthor().equalsIgnoreCase("WC"))
            .toList();
    }

    private static List<Question> getAdminQuestionsSentAnswered(List<Question> questions) {
        return questions.stream()
            .filter(question -> question.getType() != QuestionType.COMPLEMENT
                && question.getAuthor().equalsIgnoreCase("WC")
                && question.getAnswer() != null
                && question.getAnswer().getSent() != null)
            .toList();
    }

    private static List<Question> getAdminQuestionsReceivedAnswered(List<Question> questions) {
        return questions.stream()
            .filter(question -> question.getType() != QuestionType.COMPLEMENT
                && !question.getAuthor().equalsIgnoreCase("WC")
                && question.getAnswer() != null
                && question.getAnswer().getSent() != null)
            .toList();
    }

}