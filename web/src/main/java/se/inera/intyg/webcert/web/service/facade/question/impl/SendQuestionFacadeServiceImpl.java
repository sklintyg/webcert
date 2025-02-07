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

import static se.inera.intyg.webcert.web.service.facade.question.util.QuestionUtil.getSubject;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.question.SendQuestionFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.util.QuestionConverter;

@Service("sendQuestionFromWC")
public class SendQuestionFacadeServiceImpl implements SendQuestionFacadeService {

    private final ArendeService arendeService;
    private final ArendeDraftService arendeDraftService;
    private final QuestionConverter questionConverter;

    public SendQuestionFacadeServiceImpl(ArendeService arendeService,
        ArendeDraftService arendeDraftService, QuestionConverter questionConverter) {
        this.arendeService = arendeService;
        this.arendeDraftService = arendeDraftService;
        this.questionConverter = questionConverter;
    }

    @Override
    public Question send(Question question) {
        final var questionDraft = arendeDraftService.getQuestionDraftById(Long.parseLong(question.getId()));
        questionDraft.setText(question.getMessage());
        questionDraft.setAmne(
            getSubject(question.getType()).toString()
        );
        final var arende = arendeService.sendMessage(questionDraft);
        return questionConverter.convert(arende);
    }
}
