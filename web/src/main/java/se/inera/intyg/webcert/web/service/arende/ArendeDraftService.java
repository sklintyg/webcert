package se.inera.intyg.webcert.web.service.arende;

import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;

import java.util.List;

public interface ArendeDraftService {
    boolean saveDraft(String intygId, String questionId, String text, String amne);

    boolean delete(String intygId, String questionId);

    List<ArendeDraft> listAnswerDrafts(String intygId);

    ArendeDraft getQuestionDraft(String intygId);
}
