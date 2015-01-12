package se.inera.webcert.service.draft;

import java.util.List;
import java.util.Map;

import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.service.draft.dto.CreateNewDraftCopyRequest;
import se.inera.webcert.service.draft.dto.CreateNewDraftCopyResponse;
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

    Utkast setForwardOnDraft(String intygsId, Boolean forwarded);

    Map<String, Long> getNbrOfUnsignedDraftsByCareUnits(List<String> careUnitIds);

    void deleteUnsignedDraft(String intygId);

    Utkast getDraft(String intygId);

    SignatureTicket createDraftHash(String intygsId);

    SignatureTicket serverSignature(String intygsId);

    CreateNewDraftCopyResponse createNewDraftCopy(CreateNewDraftCopyRequest request);
}
