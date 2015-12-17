/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.integration.hsa.services;

import javax.xml.ws.WebServiceException;

import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;

/**
 * Created by Magnus Ekstrand on 27/05/15.
 */
public interface GetEmployeeService {

    /**
     * Returnerar information, som kontaktinformation samt legitimerad yrkesgrupp och specialitet, för sökt person.
     * Exakt ett av fälten personHsaId och personalIdentityNumber ska anges.
     *
     * @param personHsaId Sökt persons HSA-id.
     * @param personalIdentityNumber Sökt persons Person-id (personnummer eller samordningsnummer).
     *
     * @return Information om sökt person.
     *
     * @throws WebServiceException
     */
    GetEmployeeIncludingProtectedPersonResponseType getEmployee(String personHsaId, String personalIdentityNumber) throws WebServiceException;

    /**
     * Returnerar information, som kontaktinformation samt legitimerad yrkesgrupp och specialitet, för sökt person.
     * Exakt ett av fälten personHsaId och personalIdentityNumber ska anges.
     *
     * @param personHsaId Sökt persons HSA-id.
     * @param personalIdentityNumber Sökt persons Person-id (personnummer eller samordningsnummer).
     * @param searchBase Sökbas. Om ingen sökbas anges används c=SE som sökbas.
     *
     * @return Information om sökt person.
     *
     * @throws WebServiceException
     */
    GetEmployeeIncludingProtectedPersonResponseType getEmployee(String personHsaId, String personalIdentityNumber, String searchBase) throws WebServiceException;

}
