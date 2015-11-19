package se.inera.webcert.service.utkast;

import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.utkast.dto.CreateNewDraftRequest;
import se.inera.webcert.service.utkast.dto.DraftValidation;
import se.inera.webcert.service.utkast.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.utkast.dto.SaveAndValidateDraftResponse;

import java.util.List;
import java.util.Map;

public interface UtkastService {

    Utkast createNewDraft(CreateNewDraftRequest request);

    Utkast getDraft(String intygId);

    Utkast setNotifiedOnDraft(String intygsId, long version, Boolean notified);

    SaveAndValidateDraftResponse saveAndValidateDraft(SaveAndValidateDraftRequest request, boolean createPdlLogEvent);

    DraftValidation validateDraft(String intygId, String intygType, String draft);

    List<Lakare> getLakareWithDraftsByEnhet(String enhetsId);

    List<Utkast> filterIntyg(UtkastFilter filter);

    Map<String, Long> getNbrOfUnsignedDraftsByCareUnits(List<String> careUnitIds);

    void deleteUnsignedDraft(String intygId, long version);

    void logPrintOfDraftToPDL(String intygId);

    int countFilterIntyg(UtkastFilter filter);

}
