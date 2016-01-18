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

import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.webcert.integration.hsa.client.AuthorizationManagementService;
import se.inera.intyg.webcert.integration.hsa.client.EmployeeService;
import se.inera.intyg.webcert.integration.hsa.stub.Medarbetaruppdrag;
import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.v1.CommissionType;
import se.riv.infrastructure.directory.v1.PersonInformationType;

/**
 * Provides person related services using TJK over NTjP.
 *
 * infrastructure:directory:employee and
 * infrastructure:directory:authorizationmanagement
 *
 * @author eriklupander
 */
@Service
public class HsaPersonServiceImpl implements HsaPersonService {

    private static final Logger LOG = LoggerFactory.getLogger(HsaPersonServiceImpl.class);

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AuthorizationManagementService authorizationManagementService;

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.integration.hsa.services.HsaPersonService#getHsaPersonInfo(java.lang.String)
     */
    @Override
    public List<PersonInformationType> getHsaPersonInfo(String personHsaId) {

        LOG.debug("Getting info from HSA for person '{}'", personHsaId);

        GetEmployeeIncludingProtectedPersonResponseType responseType = employeeService.getEmployee(personHsaId, null,  null);
        return responseType.getPersonInformation();
    }

    public List<CommissionType> checkIfPersonHasMIUsOnUnit(String hosPersonHsaId, final String unitHsaId) {

        LOG.debug("Checking if person with HSA id '{}' has MIUs on unit '{}'", hosPersonHsaId, unitHsaId);

        GetCredentialsForPersonIncludingProtectedPersonResponseType response = authorizationManagementService.getAuthorizationsForPerson(hosPersonHsaId, null, null);
        List<CommissionType> commissions = response.getCredentialInformation().stream()
                .flatMap(ci -> ci.getCommission().stream())
                .collect(Collectors.toList());

        List<CommissionType> filteredMuisOnUnit = commissions.stream()
                .filter(ct -> ct.getHealthCareUnitHsaId().equals(unitHsaId))
                .filter(ct -> ct.getHealthCareUnitEndDate() == null || ct.getHealthCareUnitEndDate().isAfter(LocalDateTime.now()))
                .filter(ct -> (ct.getCommissionPurpose() != null) && Medarbetaruppdrag.VARD_OCH_BEHANDLING.equalsIgnoreCase(ct.getCommissionPurpose()))
                .collect(Collectors.toList());

        LOG.debug("Person has {} MIUs on unit '{}'", filteredMuisOnUnit.size(), hosPersonHsaId);

        return filteredMuisOnUnit;
    }
}
