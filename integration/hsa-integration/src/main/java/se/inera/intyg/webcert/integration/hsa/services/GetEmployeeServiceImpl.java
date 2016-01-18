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

package se.inera.intyg.webcert.integration.hsa.services;

import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.webcert.integration.hsa.client.EmployeeService;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;

/**
 * Created by Magnus Ekstrand on 28/05/15.
 */
@Service
public class GetEmployeeServiceImpl implements GetEmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(GetEmployeeServiceImpl.class);

    @Autowired
    private EmployeeService employeeService;

    @Override
    public GetEmployeeIncludingProtectedPersonResponseType getEmployee(String personHsaId, String personalIdentityNumber) throws WebServiceException {
        return employeeService.getEmployee(personHsaId, personalIdentityNumber, null);
    }

    @Override
    public GetEmployeeIncludingProtectedPersonResponseType getEmployee(String personHsaId, String personalIdentityNumber, String searchBase) throws WebServiceException {
        return employeeService.getEmployee(personHsaId, personalIdentityNumber, searchBase);
    }


}
