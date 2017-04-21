package se.inera.intyg.webcert.web.service.arende;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeDraftRepository;

@Service
@Transactional("jpaTransactionManager")
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
