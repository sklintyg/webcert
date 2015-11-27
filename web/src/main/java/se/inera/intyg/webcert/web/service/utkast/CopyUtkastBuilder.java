package se.inera.intyg.webcert.web.service.utkast;

import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.integration.pu.model.Person;
import se.inera.intyg.webcert.web.service.utkast.dto.CopyUtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyRequest;

public interface CopyUtkastBuilder {

    CopyUtkastBuilderResponse populateCopyUtkastFromSignedIntyg(CreateNewDraftCopyRequest copyRequest, Person patientDetails)
            throws ModuleNotFoundException,
            ModuleException;

    CopyUtkastBuilderResponse populateCopyUtkastFromOrignalUtkast(CreateNewDraftCopyRequest copyRequest, Person patientDetails)
            throws ModuleNotFoundException,
            ModuleException;

}
