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
package se.inera.intyg.webcert.web.csintegration.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.link.ResourceLinkTypeEnum;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.facade.list.dto.QuestionSenderType;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@Component
public class ListQuestionConverter {

    public ArendeListItem convert(Optional<CertificateDTO> certificate, Question question) {
        if (certificate.isEmpty()) {
            throw new IllegalStateException("Certificate for question was not available");
        }

        final var arendeListItem = new ArendeListItem();
        final var metadata = certificate.get().getMetadata();

        arendeListItem.setIntygId(metadata.getId());
        arendeListItem.setTestIntyg(metadata.isTestCertificate());
        arendeListItem.setPatientId(metadata.getPatient().getPersonId().getId());
        arendeListItem.setAvliden(metadata.getPatient().isDeceased());
        arendeListItem.setSigneratAv(metadata.getIssuedBy().getPersonId());
        arendeListItem.setSekretessmarkering(metadata.getPatient().isProtectedPerson());
        arendeListItem.setAmne(convertSubject(question.getType()).name());
        arendeListItem.setSigneratAvNamn(metadata.getIssuedBy().getFullName());
        arendeListItem.setFragestallare(convertAuthor(question.getAuthor()));
        arendeListItem.setReceivedDate(question.getSent());
        arendeListItem.setPaminnelse(question.getReminders() != null && question.getReminders().length > 0);
        arendeListItem.setStatus(convertStatusQuestion(question));
        arendeListItem.setVidarebefordrad(question.isForwarded());
        arendeListItem.setLinks(getLinks(question));

        return arendeListItem;
    }

    private String convertAuthor(String authorName) {
        if (authorName.equals(QuestionSenderType.FK.getName())) {
            return QuestionSenderType.FK.toString();
        }

        return QuestionSenderType.WC.toString();
    }

    private Status convertStatusQuestion(Question question) {
        if (question.isHandled()) {
            return Status.CLOSED;
        }

        if (isAnswered(question)) {
            return Status.ANSWERED;
        }

        return question.getAuthor() != null && question.getAuthor().equals(QuestionSenderType.FK.getName())
            ? Status.PENDING_INTERNAL_ACTION
            : Status.PENDING_EXTERNAL_ACTION;
    }

    private static boolean isAnswered(final Question question) {
        return question.getAnswer() != null && question.getAnswer().getSent() != null;
    }

    private ArendeAmne convertSubject(QuestionType questionType) {
        switch (questionType) {
            case COORDINATION:
                return ArendeAmne.AVSTMN;
            case CONTACT:
                return ArendeAmne.KONTKT;
            case MISSING:
            case OTHER:
                return ArendeAmne.OVRIGT;
            case COMPLEMENT:
                return ArendeAmne.KOMPLT;
        }
        throw new IllegalArgumentException("Unsupported question type: " + questionType);
    }

    private List<ActionLink> getLinks(Question question) {
        final var links = new ArrayList<ActionLink>();
        links.add(new ActionLink(ActionLinkType.LASA_FRAGA));

        if (question.getLinks().stream().anyMatch(link -> link.getType() == ResourceLinkTypeEnum.FORWARD_QUESTION)) {
            links.add(new ActionLink(ActionLinkType.VIDAREBEFODRA_FRAGA));
        }

        return links;
    }

}
