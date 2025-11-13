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

package se.inera.intyg.webcert.web.service.facade.modal.typeinfo;

import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;

public class CertificateTypeInfoModalProviderResolver {

    private CertificateTypeInfoModalProviderResolver() {
        throw new IllegalStateException("Utility class");
    }

    public static CertificateTypeInfoModalProvider getModalProvider(String certificateType) {
        if (DbModuleEntryPoint.MODULE_ID.equalsIgnoreCase(certificateType)) {
            return new DbTypeInfoModalProvider();
        }
        
        if (DoiModuleEntryPoint.MODULE_ID.equalsIgnoreCase(certificateType)) {
            return new DoiTypeInfoModalProvider();
        }

        return null;
    }
}

