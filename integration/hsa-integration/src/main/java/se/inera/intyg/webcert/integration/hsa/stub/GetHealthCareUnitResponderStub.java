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

package se.inera.intyg.webcert.integration.hsa.stub;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.integration.hsa.model.Mottagning;
import se.riv.infrastructure.directory.organization.gethealthcareunit.v1.rivtabp21.GetHealthCareUnitResponderInterface;
import se.riv.infrastructure.directory.organization.gethealthcareunitresponder.v1.GetHealthCareUnitResponseType;
import se.riv.infrastructure.directory.organization.gethealthcareunitresponder.v1.GetHealthCareUnitType;
import se.riv.infrastructure.directory.organization.gethealthcareunitresponder.v1.HealthCareUnitType;
import se.riv.infrastructure.directory.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-12-08.
 */
public class GetHealthCareUnitResponderStub implements GetHealthCareUnitResponderInterface {

    @Autowired
    private HsaServiceStub hsaServiceStub;

    @Override
    public GetHealthCareUnitResponseType getHealthCareUnit(String logicalAddress, GetHealthCareUnitType parameters) {
        GetHealthCareUnitResponseType responseType = new GetHealthCareUnitResponseType();

        Mottagning mottagning = hsaServiceStub.getMottagning(parameters.getHealthCareUnitMemberHsaId());
        if (mottagning == null) {
            responseType.setResultText("HsaServiceStub returned NULL Mottagning for hsaId: '" + parameters.getHealthCareUnitMemberHsaId() + "'");
            responseType.setResultCode(ResultCodeEnum.ERROR);
            return responseType;
        }
        HealthCareUnitType member = new HealthCareUnitType();

        // Mottagning
        member.setHealthCareUnitMemberHsaId(mottagning.getId());
        member.setHealthCareUnitMemberName(mottagning.getNamn());
        member.setHealthCareUnitMemberStartDate(mottagning.getStart());
        member.setHealthCareUnitMemberEndDate(mottagning.getEnd());

        // Överordnad enhet, används för att plocka fram överordnad enhets epostadress när egen saknas.
        member.setHealthCareUnitHsaId(mottagning.getParentHsaId());

        responseType.setHealthCareUnit(member);
        responseType.setResultCode(ResultCodeEnum.OK);
        return responseType;
    }
}
