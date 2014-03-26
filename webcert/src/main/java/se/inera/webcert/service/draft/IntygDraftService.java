package se.inera.webcert.service.draft;

import java.util.List;

import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.dto.Lakare;

public interface IntygDraftService {

    public abstract String createNewDraft(CreateNewDraftRequest request);

    public abstract DraftValidation saveAndValidateDraft(SaveAndValidateDraftRequest request);
    
    public abstract DraftValidation validateDraft(String intygId, String intygType, String draft);
    
    public abstract List<Lakare> getLakareWithDraftsByEnhet(String enhetsId);

}
