package se.inera.webcert.service.utkast;

import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.pu.model.Person;
import se.inera.webcert.service.utkast.dto.CreateNewDraftCopyRequest;

public interface CopyUtkastBuilder {

    public abstract Utkast populateCopyUtkastFromSignedIntyg(CreateNewDraftCopyRequest copyRequest, Person patientDetails)
            throws ModuleNotFoundException,
            ModuleException;

    public abstract Utkast populateCopyUtkastFromOrignalUtkast(CreateNewDraftCopyRequest copyRequest, Person patientDetails)
            throws ModuleNotFoundException,
            ModuleException;

}
