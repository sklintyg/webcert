/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.tak.service;

import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.integration.tak.model.TakResult;

/**
 * Service for checking whether operations are routed correctly in NTJP.
 */
public interface TakService {
    /**
     * Determines if new drafts are allowed to be created.<br/>
     * This method utilizes a timeout configured in webcert.properties, if a result is not found a warning is logged
     * and draft creation is allowed despite the lack of a valid TAKning.
     *
     * @param careUnitId hsaId for the Careunit to check.
     * @param intygsTyp the id for the certificate, as found in {@link Fk7263EntryPoint#getModuleId()} etc.
     * @param schemaVersion V1 or V3 of interaction
     * @param user Implementor of {@link se.inera.intyg.infra.security.common.model.UserDetails} used to check if the
     *            certificate is applicable for issue handling.
     * @return {@link TakResult} containing the actual result as boolean and any errorMessages.
     */
    TakResult verifyTakningForCareUnit(String careUnitId, String intygsTyp, String schemaVersion, IntygUser user);
}
