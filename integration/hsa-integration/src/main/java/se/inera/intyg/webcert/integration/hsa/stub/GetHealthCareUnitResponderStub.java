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
    HsaServiceStub hsaServiceStub;

    @Override
    public GetHealthCareUnitResponseType getHealthCareUnit(String logicalAddress, GetHealthCareUnitType parameters) {
        GetHealthCareUnitResponseType responseType = new GetHealthCareUnitResponseType();

        Mottagning mottagning = hsaServiceStub.getMottagning(parameters.getHealthCareUnitMemberHsaId());
        if (mottagning == null) {
            return null;
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
