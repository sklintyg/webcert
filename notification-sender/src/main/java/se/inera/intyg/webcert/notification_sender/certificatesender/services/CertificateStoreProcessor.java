/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;

/**
 * Camel message processor responsible for consuming {@link Constants#STORE_MESSAGE} messages,
 * using the ModuleApi to register certificates in intygstjansten.
 */
public class CertificateStoreProcessor {

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    public void process(@Body String utkastAsJson,
        @Header(Constants.INTYGS_TYP) String intygsTyp,
        @Header(Constants.LOGICAL_ADDRESS) String logicalAddress)
        throws TemporaryException {
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygsTyp,
                moduleRegistry.resolveVersionFromUtlatandeJson(intygsTyp, utkastAsJson));

            moduleApi.registerCertificate(utkastAsJson, logicalAddress);

        } catch (Exception e) {
            throw new TemporaryException(e);
        }
    }
}
