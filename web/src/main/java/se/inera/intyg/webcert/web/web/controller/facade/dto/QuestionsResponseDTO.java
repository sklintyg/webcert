/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.facade.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.inera.intyg.common.support.facade.model.question.Question;

public class QuestionsResponseDTO {

    private List<QuestionDTO> questions;

    public List<QuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDTO> questions) {
        this.questions = questions;
    }

    public static QuestionsResponseDTO create(List<Question> questions, Map<Question, List<ResourceLinkDTO>> links) {
        final var questionsResponseDTO = new QuestionsResponseDTO();
        final var questionDTOList = new ArrayList<QuestionDTO>();
        questions.stream().forEach(
            question -> questionDTOList.add(QuestionDTO.create(question, links.getOrDefault(question, Collections.emptyList())))
        );
        questionsResponseDTO.setQuestions(questionDTOList);
        return questionsResponseDTO;
    }
}
