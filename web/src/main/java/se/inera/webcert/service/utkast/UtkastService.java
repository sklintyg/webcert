package se.inera.webcert.service.utkast;

import java.util.List;
import java.util.Map;

import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.utkast.dto.CreateNewDraftRequest;
import se.inera.webcert.service.utkast.dto.DraftValidation;
import se.inera.webcert.service.utkast.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.utkast.dto.SaveAndValidateDraftResponse;

public interface UtkastService {

    String createNewDraft(CreateNewDraftRequest request);

    SaveAndValidateDraftResponse saveAndValidateDraft(SaveAndValidateDraftRequest request, boolean createPdlLogEvent);

    DraftValidation validateDraft(String intygId, String intygType, String draft);

    List<Lakare> getLakareWithDraftsByEnhet(String enhetsId);

    Utkast setNotifiedOnDraft(String intygsId, long version, Boolean notified);

    Map<String, Long> getNbrOfUnsignedDraftsByCareUnits(List<String> careUnitIds);

    void deleteUnsignedDraft(String intygId, long version);

    Utkast getDraft(String intygId);

    void logPrintOfDraftToPDL(String intygId);
    
    List<Utkast> filterIntyg(UtkastFilter filter);
    
    int countFilterIntyg(UtkastFilter filter);
    
}
