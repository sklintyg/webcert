/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;

public interface ArendeDraftService {

    boolean saveDraft(String intygId, String questionId, String text, String amne);

    boolean delete(String intygId, String questionId);

    List<ArendeDraft> listAnswerDrafts(String intygId);

    ArendeDraft getQuestionDraft(String intygId);

    ArendeDraft getAnswerDraft(String certificateId, String questionId);

    ArendeDraft getQuestionDraftById(long id);

    ArendeDraft create(String certificateId, String subject, String message);

    ArendeDraft save(ArendeDraft arendeDraft);
}
