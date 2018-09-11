/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.arende;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeDraftRepository;

@Service
@Transactional
public class ArendeDraftServiceImpl implements ArendeDraftService {

    @Autowired
    private ArendeDraftRepository arendeDraftRepository;

    @Override
    public boolean saveDraft(String intygId, String questionId, String text, String amne) {
        ArendeDraft draft = arendeDraftRepository.findByIntygIdAndQuestionId(intygId, questionId);
        if (draft != null) {
            draft.setText(text);
            draft.setAmne(amne);
        } else {
            draft = createDraft(intygId, questionId, text, amne);
        }
        arendeDraftRepository.save(draft);
        return true;
    }

    @Override
    public boolean delete(String intygId, String questionId) {
        ArendeDraft draft = arendeDraftRepository.findByIntygIdAndQuestionId(intygId, questionId);
        if (draft != null) {
            arendeDraftRepository.delete(draft);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<ArendeDraft> listAnswerDrafts(String intygId) {
        return arendeDraftRepository.findByIntygId(intygId).stream().filter(d -> d.getQuestionId() != null).collect(Collectors.toList());
    }

    @Override
    public ArendeDraft getQuestionDraft(String intygId) {
        return arendeDraftRepository.findByIntygIdAndQuestionId(intygId, null);
    }

    private ArendeDraft createDraft(String intygId, String questionId, String text, String amne) {
        ArendeDraft draft;
        draft = new ArendeDraft();
        draft.setIntygId(intygId);
        draft.setQuestionId(questionId);
        draft.setText(text);
        draft.setAmne(amne);
        return draft;
    }
}
