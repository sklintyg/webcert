package se.inera.webcert.service.draft;

import java.util.List;
import java.util.Map;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.draft.dto.SignatureTicket;
import se.inera.webcert.service.dto.Lakare;

public interface IntygDraftService {

    String createNewDraft(CreateNewDraftRequest request);

    DraftValidation saveAndValidateDraft(SaveAndValidateDraftRequest request);

    DraftValidation validateDraft(String intygId, String intygType, String draft);

    List<Lakare> getLakareWithDraftsByEnhet(String enhetsId);

    Intyg setForwardOnDraft(String intygsId, Boolean forwarded);

    Map<String, Long> getNbrOfUnsignedDraftsByCareUnits(List<String> careUnitIds);

    void deleteUnsignedDraft(String intygId);

    Intyg getDraft(String intygId);
}
