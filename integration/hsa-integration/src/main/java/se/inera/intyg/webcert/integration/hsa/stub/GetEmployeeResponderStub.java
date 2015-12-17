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

package se.inera.intyg.webcert.integration.hsa.stub;

import org.springframework.beans.factory.annotation.Autowired;

import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedperson.v1.rivtabp21.GetEmployeeIncludingProtectedPersonResponderInterface;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonType;
import se.riv.infrastructure.directory.v1.PaTitleType;
import se.riv.infrastructure.directory.v1.PersonInformationType;
import se.riv.infrastructure.directory.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-12-03.
 */
public class GetEmployeeResponderStub implements GetEmployeeIncludingProtectedPersonResponderInterface {

    @Autowired
    private HsaServiceStub hsaServiceStub;

    @Override
    public GetEmployeeIncludingProtectedPersonResponseType getEmployeeIncludingProtectedPerson(String logicalAddress, GetEmployeeIncludingProtectedPersonType getEmployeeIncludingProtectedPersonType) {
        GetEmployeeIncludingProtectedPersonResponseType response = new GetEmployeeIncludingProtectedPersonResponseType();
        String personHsaId = getEmployeeIncludingProtectedPersonType.getPersonHsaId();
        HsaPerson hsaPerson = hsaServiceStub.getHsaPerson(personHsaId);
        if (hsaPerson == null) {
            response.setResultText("Null HsaPerson returned by HsaServiceStub.");
            response.setResultCode(ResultCodeEnum.ERROR);
            return response;
        }

        PersonInformationType person = new PersonInformationType();
        person.setTitle(hsaPerson.getTitel());
        person.setPersonHsaId(hsaPerson.getHsaId());
        person.setGivenName(hsaPerson.getEfterNamn());

        for (String legYrkesGrp : hsaPerson.getLegitimeradeYrkesgrupper()) {
            PaTitleType paTitle = new PaTitleType();
            paTitle.setPaTitleName(legYrkesGrp);
            person.getPaTitle().add(paTitle);
        }

        for (HsaSpecialicering spec : hsaPerson.getSpecialiseringar()) {
            person.getSpecialityCode().add(spec.getKod());
            person.getSpecialityName().add(spec.getNamn());
        }

        response.getPersonInformation().add(person);
        response.setResultCode(ResultCodeEnum.OK);
        return response;
    }
}
