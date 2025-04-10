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
package se.inera.intyg.webcert.web.service.arende;

import static java.util.Comparator.comparing;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeDraftRepository;

@Service
@Transactional
public class ArendeDraftServiceImpl implements ArendeDraftService {

    private static final Logger LOG = LoggerFactory.getLogger(ArendeDraftServiceImpl.class);

    @Autowired
    private ArendeDraftRepository arendeDraftRepository;

    @Override
    public boolean saveDraft(String intygId, String questionId, String text, String amne) {
        create(intygId, amne, text, questionId);
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
        final var drafts = arendeDraftRepository.findByIntygId(intygId);

        if (drafts.size() > 1) {
            LOG.error("Found more than one ArendeDraft for certificateId: '{}'. Returning the latest.", intygId);
        }

        return drafts.stream()
            .filter(draft -> draft.getId() != null)
            .max(comparing(ArendeDraft::getId))
            .orElse(null);
    }

    @Override
    public ArendeDraft getAnswerDraft(String certificateId, String questionId) {
        return arendeDraftRepository.findByIntygIdAndQuestionId(certificateId, questionId);
    }

    @Override
    public ArendeDraft getQuestionDraftById(long id) {
        return arendeDraftRepository.findById(id).orElseThrow();
    }

    @Override
    public ArendeDraft create(String certificateId, String subject, String message, String questionId) {
        ArendeDraft draft = arendeDraftRepository.findByIntygIdAndQuestionId(certificateId, questionId);
        if (draft != null) {
            draft.setText(message);
            draft.setAmne(subject);
        } else {
            draft = createDraft(certificateId, questionId, message, subject);
        }
        return arendeDraftRepository.save(draft);
    }

    @Override
    public ArendeDraft save(ArendeDraft arendeDraft) {
        return arendeDraftRepository.save(arendeDraft);
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
