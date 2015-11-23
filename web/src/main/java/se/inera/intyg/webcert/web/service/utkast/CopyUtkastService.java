package se.inera.intyg.webcert.web.service.utkast;

import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyResponse;

public interface CopyUtkastService {

    CreateNewDraftCopyResponse createCopy(CreateNewDraftCopyRequest copyRequest);

}
