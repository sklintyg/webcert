package se.inera.webcert.service.draft;

import se.inera.webcert.service.draft.dto.DraftValidation;

public interface IntygDraftService {

    public abstract String createNewDraft(String patientId, String intygType);

    public abstract DraftValidation validateDraft(String intygId, String intygType, String draft);

}
