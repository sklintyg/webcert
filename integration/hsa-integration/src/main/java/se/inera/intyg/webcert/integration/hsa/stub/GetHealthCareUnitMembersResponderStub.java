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

import se.inera.intyg.webcert.integration.hsa.model.Mottagning;
import se.inera.intyg.webcert.integration.hsa.model.Vardenhet;
import se.inera.intyg.webcert.integration.hsa.model.Vardgivare;
import se.riv.infrastructure.directory.organization.gethealthcareunitmembers.v1.rivtabp21.GetHealthCareUnitMembersResponderInterface;
import se.riv.infrastructure.directory.organization.gethealthcareunitmembersresponder.v1.GetHealthCareUnitMembersResponseType;
import se.riv.infrastructure.directory.organization.gethealthcareunitmembersresponder.v1.GetHealthCareUnitMembersType;
import se.riv.infrastructure.directory.organization.gethealthcareunitmembersresponder.v1.HealthCareUnitMemberType;
import se.riv.infrastructure.directory.organization.gethealthcareunitmembersresponder.v1.HealthCareUnitMembersType;
import se.riv.infrastructure.directory.v1.AddressType;
import se.riv.infrastructure.directory.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-12-04.
 */
public class GetHealthCareUnitMembersResponderStub implements GetHealthCareUnitMembersResponderInterface {

    @Autowired
    private HsaServiceStub hsaServiceStub;

    @Override
    public GetHealthCareUnitMembersResponseType getHealthCareUnitMembers(String logicalAddress, GetHealthCareUnitMembersType parameters) {
        GetHealthCareUnitMembersResponseType response = new GetHealthCareUnitMembersResponseType();
        if (parameters.getHealthCareUnitHsaId().endsWith("-finns-ej")) {
            response.setResultText("Returning ERROR for -finns-ej hsaId");
            response.setResultCode(ResultCodeEnum.ERROR);
            return response;
        }


        HealthCareUnitMembersType membersType = new HealthCareUnitMembersType();

        attachMembers(membersType, parameters.getHealthCareUnitHsaId());

        response.setHealthCareUnitMembers(membersType);
        response.setResultCode(ResultCodeEnum.OK);
        return response;
    }

    private void attachMembers(HealthCareUnitMembersType membersType, String unitHsaId) {

        for (Vardgivare vardgivare : hsaServiceStub.getVardgivare()) {
            for (Vardenhet enhet : vardgivare.getVardenheter()) {
                if (enhet.getId().equals(unitHsaId)) {

                    for (Mottagning mottagning : enhet.getMottagningar()) {
                        if (mottagning.getId().endsWith("-finns-ej")) {
                            continue;
                        }
                        HealthCareUnitMemberType member = new HealthCareUnitMemberType();
                        member.setHealthCareUnitMemberHsaId(mottagning.getId());
                        member.setHealthCareUnitMemberName(mottagning.getNamn());
                        member.setHealthCareUnitMemberStartDate(mottagning.getStart());
                        member.setHealthCareUnitMemberEndDate(mottagning.getEnd());
                        AddressType addressType = new AddressType();
                        addressType.getAddressLine().add(mottagning.getPostadress());

                        member.setHealthCareUnitMemberpostalAddress(addressType);
                        member.setHealthCareUnitMemberpostalCode(mottagning.getPostnummer());
                        member.getHealthCareUnitMemberPrescriptionCode().add(mottagning.getArbetsplatskod());
                        membersType.getHealthCareUnitMember().add(member);
                    }
                }
            }
        }
    }
}
