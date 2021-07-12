/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;

@Service
public class SaveQuestionFacadeServiceImpl implements SaveQuestionFacadeService {

    private final ArendeDraftService arendeDraftService;

    @Autowired
    public SaveQuestionFacadeServiceImpl(ArendeDraftService arendeDraftService) {
        this.arendeDraftService = arendeDraftService;
    }

    @Override
    public void save(Question question) {
        var questionDraft = arendeDraftService.getQuestionDraftById(Long.parseLong(question.getId()));
        questionDraft.setText(question.getMessage());
        questionDraft.setAmne(getSubject(question.getType()).toString());
        arendeDraftService.saveDraft(questionDraft);
    }

    private ArendeAmne getSubject(QuestionType type) {
        switch (type) {
            case COORDINATION:
                return ArendeAmne.AVSTMN;
            case CONTACT:
                return ArendeAmne.KONTKT;
            case OTHER:
                return ArendeAmne.OVRIGT;
            default:
                throw new IllegalArgumentException("Type not supported: " + type);
        }
    }
}
