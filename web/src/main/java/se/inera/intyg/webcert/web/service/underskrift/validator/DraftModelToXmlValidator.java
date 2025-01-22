/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.underskrift.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateXmlResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleValidationException;
import se.inera.intyg.webcert.logging.LogMarkers;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.UtkastModelToXMLConverter;

@Service
public class DraftModelToXmlValidator {

    private static final Logger LOG = LoggerFactory.getLogger(DraftModelToXmlValidator.class);

    @Autowired
    private UtkastModelToXMLConverter draftModelToXMLConverter;

    @Autowired
    private IntygModuleRegistry intygModuleRegistry;

    public ValidateXmlResponse validateDraftModelAsXml(Utkast draft)
        throws ModuleException, ModuleNotFoundException {
        String xml = draftModelToXMLConverter.utkastToXml(draft.getModel(), draft.getIntygsTyp());

        return validateXml(draft, xml);
    }

    public void assertResponse(String certificateId, ValidateXmlResponse validationResponse) throws ModuleValidationException {
        if (validationResponse.hasErrorMessages()) {
            String validationErrors = String.join(";", validationResponse.getValidationErrors());
            LOG.error(LogMarkers.VALIDATION, "Schematron validation failed for certificate: {}. Validation errors: {}",
                certificateId, validationErrors);
            throw new ModuleValidationException(validationResponse.getValidationErrors());
        }
    }

    private ValidateXmlResponse validateXml(Utkast draft, String xml)
        throws ModuleNotFoundException, ModuleException {
        try {
            return intygModuleRegistry.getModuleApi(draft.getIntygsTyp(), draft.getIntygTypeVersion()).validateXml(xml);
        } catch (ModuleNotFoundException | ModuleException e) {
            LOG.error("Certificate with id {} could not be validated.", draft.getIntygsId());
            throw e;
        }
    }

}
