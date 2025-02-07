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
package se.inera.intyg.webcert.web.service.facade.question.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsAvailableFunctionsService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Service
public class GetQuestionsAvailableFunctionsServiceImpl implements GetQuestionsAvailableFunctionsService {

    private static final String FORWARD_DESCRIPTION_CERTIFICATE =
        "Skapar ett e-postmeddelande med länk till intyget.";

    @Override
    public List<ResourceLinkDTO> get(Question question) {
        final var availableFunctions = new ArrayList<ResourceLinkDTO>();

        if (isQuestionRecieved(question) && isAdministrativeQuestion(question) && isQuestionUnanswered(question)
            && isQuestionUnhandled(question)) {
            availableFunctions.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.ANSWER_QUESTION,
                    "Svara",
                    "Svara på fråga",
                    true
                )
            );
        }

        if ((isQuestionSent(question) || isQuestionUnanswered(question))
            && (isQuestionUnhandled(question) || isAdministrativeQuestion(question))) {
            availableFunctions.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.HANDLE_QUESTION,
                    "Hantera",
                    "Hantera fråga",
                    true
                )
            );
        }

        if (isComplementQuestion(question) && isQuestionUnhandled(question) && isNotAnsweredByCertificate(question)) {
            availableFunctions.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.COMPLEMENT_CERTIFICATE,
                    "Komplettera",
                    "Öppnar ett nytt intygsutkast.",
                    true
                )
            );

            availableFunctions.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.CANNOT_COMPLEMENT_CERTIFICATE,
                    "Kan ej komplettera",
                    "Öppnar en dialogruta med mer information.",
                    true
                )
            );
        }

        if (isUnhandled(question)) {
            availableFunctions.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.FORWARD_QUESTION,
                    "Vidarebefordra",
                    FORWARD_DESCRIPTION_CERTIFICATE,
                    true
                )
            );
        }
        return availableFunctions;
    }

    private boolean isNotAnsweredByCertificate(Question question) {
        return question.getAnsweredByCertificate() == null;
    }

    private boolean isAdministrativeQuestion(Question question) {
        return question.getType() != QuestionType.COMPLEMENT;
    }

    private boolean isComplementQuestion(Question question) {
        return question.getType() == QuestionType.COMPLEMENT;
    }

    private boolean isQuestionUnhandled(Question question) {
        return !question.isHandled();
    }

    private boolean isQuestionUnanswered(Question question) {
        return question.getAnswer() == null || question.getAnswer().getSent() == null;
    }

    private boolean isQuestionRecieved(Question question) {
        return question.getAuthor().equalsIgnoreCase("Försäkringskassan");
    }

    private boolean isQuestionSent(Question question) {
        return !isQuestionRecieved(question);
    }

    private boolean isUnhandled(Question question) {
        if (question != null) {
            return !question.isHandled();
        }
        return false;
    }
}
