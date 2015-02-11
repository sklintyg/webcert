package se.inera.webcert.service.utkast;

import se.inera.webcert.service.utkast.dto.CreateNewDraftCopyRequest;
import se.inera.webcert.service.utkast.dto.CreateNewDraftCopyResponse;

public interface CopyUtkastService {

    public abstract CreateNewDraftCopyResponse createCopy(CreateNewDraftCopyRequest copyRequest);

}
