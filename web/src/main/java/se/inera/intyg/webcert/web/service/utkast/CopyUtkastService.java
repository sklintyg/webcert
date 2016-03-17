/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyResponse;

public interface CopyUtkastService {

    /**
     * Create a copy of a signed Intyg or an unsigned Utkast.
     * @param copyRequest
     * @return {@link CreateNewDraftCopyResponse}
     */
    CreateNewDraftCopyResponse createCopy(CreateNewDraftCopyRequest copyRequest);

    /**
     * Create a completion, which is essentially a copy with a relation to the Intyg on which the copy is based.
     * @param copyRequest
     * @return {@link CreateNewDraftCopyResponse}
     */
    CreateCompletionCopyResponse createCompletion(CreateCompletionCopyRequest copyRequest);

}
