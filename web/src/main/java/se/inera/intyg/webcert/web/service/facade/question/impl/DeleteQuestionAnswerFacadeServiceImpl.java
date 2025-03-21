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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.question.DeleteQuestionAnswerFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionFacadeService;

@Service("deleteAnswerFromWC")
public class DeleteQuestionAnswerFacadeServiceImpl implements DeleteQuestionAnswerFacadeService {

    private final ArendeService arendeService;
    private final ArendeDraftService arendeDraftService;
    private final GetQuestionFacadeService getQuestionFacadeService;

    @Autowired
    public DeleteQuestionAnswerFacadeServiceImpl(ArendeService arendeService,
        ArendeDraftService arendeDraftService,
        GetQuestionFacadeService getQuestionFacadeService) {
        this.arendeService = arendeService;
        this.arendeDraftService = arendeDraftService;
        this.getQuestionFacadeService = getQuestionFacadeService;
    }

    @Override
    public Question delete(String questionId) {
        final var question = getQuestion(questionId);
        deleteAnswerDraftOnQuestion(question);
        return getQuestionFacadeService.get(questionId);
    }

    private Arende getQuestion(String questionId) {
        return arendeService.getArende(questionId);
    }

    private void deleteAnswerDraftOnQuestion(Arende question) {
        arendeDraftService.delete(question.getIntygsId(), question.getMeddelandeId());
    }
}
