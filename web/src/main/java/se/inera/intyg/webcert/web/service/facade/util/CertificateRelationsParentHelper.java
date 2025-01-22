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
package se.inera.intyg.webcert.web.service.facade.util;

import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;

public interface CertificateRelationsParentHelper {

    /**
     * Checks if the certificate has a parent relation and retrieves the parent information
     * from Intygstjanst (IT).
     *
     * @param certificateId Id of certificate
     * @return If parent relation exits a {@link WebcertCertificateRelation} is returned. If not it returns null.
     */
    WebcertCertificateRelation getParentFromITIfExists(String certificateId);
}
