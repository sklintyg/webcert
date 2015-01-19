package se.inera.webcert.service.utkast;

import java.util.List;
import java.util.Map;

import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.signatur.dto.SignaturTicket;
import se.inera.webcert.service.utkast.dto.CreateNewDraftCopyRequest;
import se.inera.webcert.service.utkast.dto.CreateNewDraftCopyResponse;
import se.inera.webcert.service.utkast.dto.CreateNewDraftRequest;
import se.inera.webcert.service.utkast.dto.DraftValidation;
import se.inera.webcert.service.utkast.dto.SaveAndValidateDraftRequest;

public interface UtkastService {

    String createNewDraft(CreateNewDraftRequest request);

    DraftValidation saveAndValidateDraft(SaveAndValidateDraftRequest request);

    DraftValidation validateDraft(String intygId, String intygType, String draft);

    List<Lakare> getLakareWithDraftsByEnhet(String enhetsId);

    Utkast setForwardOnDraft(String intygsId, Boolean forwarded);

    Map<String, Long> getNbrOfUnsignedDraftsByCareUnits(List<String> careUnitIds);

    void deleteUnsignedDraft(String intygId);

    Utkast getDraft(String intygId);

    SignaturTicket createDraftHash(String intygsId);

    SignaturTicket serverSignature(String intygsId);

    CreateNewDraftCopyResponse createNewDraftCopy(CreateNewDraftCopyRequest request);
}
