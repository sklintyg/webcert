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
package se.inera.intyg.webcert.integration.pp.services;

import com.google.common.base.Strings;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitioner.v1.rivtabp21.GetPrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.ResultCodeEnum;

/**
 * Interfaces with the get and validate private practitioner services.
 *
 * Created by Magnus Ekstrand on 18/06/15.
 */
public class PPServiceImpl implements PPService {

    private static final Logger LOG = LoggerFactory.getLogger(PPServiceImpl.class);

    @Autowired
    private GetPrivatePractitionerResponderInterface getPrivatePractitionerService;


    @Override
    public HoSPersonType getPrivatePractitioner(String logicalAddress, String hsaIdentityNumber, String personalIdentityNumber) {

        LOG.debug("Getting person information from Privatläkarportalen.");

        // Exakt ett av fälten hsaIdentityNumber och personalIdentityNumber ska anges.
        validateIdentifier(hsaIdentityNumber, personalIdentityNumber);

        GetPrivatePractitionerType request = createPrivatePractitionerType(hsaIdentityNumber, personalIdentityNumber);
        return getPrivatePractitioner(logicalAddress, request);
    }

    private void validateIdentifier(String hsaIdentityNumber, String personalIdentityNumber) {
        // Exakt ett av fälten hsaIdentityNumber och personalIdentityNumber ska anges.
        if (Strings.isNullOrEmpty(hsaIdentityNumber) && Strings.isNullOrEmpty(personalIdentityNumber)) {
            throw new IllegalArgumentException(
                "Inget av argumenten hsaIdentityNumber och personalIdentityNumber är satt. Ett av dem måste ha ett värde.");
        }

        if (!Strings.isNullOrEmpty(hsaIdentityNumber) && !Strings.isNullOrEmpty(personalIdentityNumber)) {
            throw new IllegalArgumentException("Endast ett av argumenten hsaIdentityNumber och personalIdentityNumber får vara satt.");
        }
    }

    private HoSPersonType getPrivatePractitioner(String logicalAddress, GetPrivatePractitionerType parameters) {

        try {
            GetPrivatePractitionerResponseType response = getPrivatePractitionerService.getPrivatePractitioner(logicalAddress, parameters);

            if (response.getHoSPerson() == null) {
                LOG.info("No private practitioner with the requested identifiers exist.");
                return null;
            }

            if (response.getResultCode() == ResultCodeEnum.ERROR) {
                LOG.error(response.getResultText());
            }

            if (response.getResultCode() == ResultCodeEnum.INFO) {
                LOG.info(response.getResultText());
            }

            return response.getHoSPerson();

        } catch (SOAPFaultException e) {
            throw new WebServiceException(e);
        }
    }

    private GetPrivatePractitionerType createPrivatePractitionerType(String hsaIdentityNumber, String personalIdentityNumber) {
        GetPrivatePractitionerType ppType = new GetPrivatePractitionerType();
        ppType.setPersonHsaId(hsaIdentityNumber);
        ppType.setPersonalIdentityNumber(personalIdentityNumber);

        return ppType;
    }

}
