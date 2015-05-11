package se.inera.webcert.service.utkast;

import java.util.List;
import java.util.Map;

import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.signatur.dto.SignaturTicket;
import se.inera.webcert.service.utkast.dto.CreateNewDraftRequest;
import se.inera.webcert.service.utkast.dto.DraftValidation;
import se.inera.webcert.service.utkast.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.utkast.dto.SaveAndValidateDraftResponse;

public interface UtkastService {

    SignaturTicket clientSignature(String biljettId, String rawSignaturString);

    SignaturTicket createDraftHash(String intygsId, long version);

    String createNewDraft(CreateNewDraftRequest request);

    void deleteUnsignedDraft(String intygId, long version);

    Utkast getDraft(String intygId);

    List<Lakare> getLakareWithDraftsByEnhet(String enhetsId);

    Map<String, Long> getNbrOfUnsignedDraftsByCareUnits(List<String> careUnitIds);

    void logPrintOfDraftToPDL(String intygId);

    SaveAndValidateDraftResponse saveAndValidateDraft(SaveAndValidateDraftRequest request, boolean createPdlLogEvent);

    Utkast setForwardOnDraft(String intygsId, Boolean forwarded);

    SignaturTicket serverSignature(String intygsId, long version);

    SignaturTicket ticketStatus(String biljettId);
}
