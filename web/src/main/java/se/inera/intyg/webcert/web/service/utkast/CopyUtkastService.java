/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.utkast;

import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateResponse;

public interface CopyUtkastService {

    /**
     * Create a completion, which is essentially a copy with a relation to the Intyg on which the copy is based.
     *
     * @return {@link CreateCompletionCopyResponse}
     */
    CreateCompletionCopyResponse createCompletion(CreateCompletionCopyRequest copyRequest);

    /**
     * Create a renewal, which is essentially a copy with a relation to the Intyg on which the copy is based.
     *
     * @return {@link CreateRenewalCopyRequest}
     */
    CreateRenewalCopyResponse createRenewalCopy(CreateRenewalCopyRequest copyRequest);

    /**
     * Create a replacement, which is essentially a copy with a relation to the Intyg on which the replacement is based.
     *
     * @return {@link CreateReplacementCopyResponse}
     */
    CreateReplacementCopyResponse createReplacementCopy(CreateReplacementCopyRequest copyRequest);

    /**
     * Create a new utkast from a signed template intyg.
     *
     * @return {@link CreateUtkastFromTemplateRequest}
     */
    CreateUtkastFromTemplateResponse createUtkastFromSignedTemplate(CreateUtkastFromTemplateRequest copyRequest);

    /**
     * Create a new utkast from utkast.
     *
     * @return {@link CreateUtkastFromTemplateRequest}
     */
    CreateUtkastFromTemplateResponse createUtkastCopy(CreateUtkastFromTemplateRequest copyRequest);

}
