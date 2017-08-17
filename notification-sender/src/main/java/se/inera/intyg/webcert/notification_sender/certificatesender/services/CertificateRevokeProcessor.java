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
package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import static com.google.common.base.Preconditions.checkArgument;

import javax.xml.ws.WebServiceException;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ExternalServiceCallException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;

public class CertificateRevokeProcessor {

    @Autowired
    private IntygModuleRegistry registry;

    public void process(@Body String xmlBody, @Header(Constants.INTYGS_ID) String intygsId,
            @Header(Constants.LOGICAL_ADDRESS) String logicalAddress,
            @Header(Constants.INTYGS_TYP) String intygsTyp) throws TemporaryException, PermanentException {

        checkArgument(!Strings.isNullOrEmpty(intygsId), "Message of type %s does not have a %s header.", Constants.REVOKE_MESSAGE,
                Constants.INTYGS_ID);
        checkArgument(!Strings.isNullOrEmpty(logicalAddress), "Message of type %s does not have a %s header.", Constants.REVOKE_MESSAGE,
                Constants.LOGICAL_ADDRESS);
        checkArgument(!Strings.isNullOrEmpty(intygsTyp), "Message of type %s does not have a %s header.", Constants.REVOKE_MESSAGE,
                Constants.INTYGS_TYP);

        try {
            ModuleApi moduleApi = registry.getModuleApi(intygsTyp);
            moduleApi.revokeCertificate(xmlBody, logicalAddress);
        } catch (ExternalServiceCallException e) {
            switch (e.getErroIdEnum()) {
            case TECHNICAL_ERROR:
            case APPLICATION_ERROR:
                throw new TemporaryException(e.getMessage());
            default:
                throw new PermanentException(e.getMessage());
            }
        } catch (ModuleException e) {
            throw new PermanentException(e.getMessage());
        } catch (WebServiceException e) {
            throw new TemporaryException(e.getMessage());
        } catch (Exception e) {
            throw new PermanentException(e.getMessage());
        }
    }
}
