package se.inera.intyg.webcert.integration.hsa.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import se.riv.infrastructure.directory.organization.gethealthcareunit.v1.rivtabp21.GetHealthCareUnitResponderInterface;
import se.riv.infrastructure.directory.organization.gethealthcareunitmembers.v1.rivtabp21.GetHealthCareUnitMembersResponderInterface;
import se.riv.infrastructure.directory.organization.gethealthcareunitmembersresponder.v1.GetHealthCareUnitMembersResponseType;
import se.riv.infrastructure.directory.organization.gethealthcareunitmembersresponder.v1.GetHealthCareUnitMembersType;
import se.riv.infrastructure.directory.organization.gethealthcareunitresponder.v1.GetHealthCareUnitResponseType;
import se.riv.infrastructure.directory.organization.gethealthcareunitresponder.v1.GetHealthCareUnitType;
import se.riv.infrastructure.directory.organization.getunit.v1.rivtabp21.GetUnitResponderInterface;
import se.riv.infrastructure.directory.organization.getunitresponder.v1.GetUnitResponseType;
import se.riv.infrastructure.directory.organization.getunitresponder.v1.GetUnitType;

/**
 * Created by eriklupander on 2015-12-03.
 */
@Service
public class OrganizationUnitServiceBean implements OrganizationUnitService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationUnitServiceBean.class);

    @Autowired
    private GetUnitResponderInterface getUnitResponderInterface;

    @Autowired
    private GetHealthCareUnitResponderInterface getHealthCareUnitResponderInterface;

    @Autowired
    private GetHealthCareUnitMembersResponderInterface getHealthCareUnitMembersResponderInterface;

    @Value("${infrastructure.directory.organization.logicalAddress}")
    private String logicalAddress;

    @Override
    public GetUnitResponseType getUnit(String unitHsaId) {
        GetUnitType parameters = new GetUnitType();
        parameters.setUnitHsaId(unitHsaId);
        GetUnitResponseType unitResponse = getUnitResponderInterface.getUnit(logicalAddress, parameters);
        return unitResponse;
    }

    @Override
    public GetHealthCareUnitResponseType getHealthCareUnit(String hsaId) {
        GetHealthCareUnitType parameters = new GetHealthCareUnitType();
        parameters.setHealthCareUnitMemberHsaId(hsaId);
        GetHealthCareUnitResponseType response = getHealthCareUnitResponderInterface.getHealthCareUnit(logicalAddress, parameters);
        return response;
    }

    @Override
    public GetHealthCareUnitMembersResponseType getHealthCareUnitMembers(String unitHsaId) {
        GetHealthCareUnitMembersType parameters = new GetHealthCareUnitMembersType();
        parameters.setHealthCareUnitHsaId(unitHsaId);
        GetHealthCareUnitMembersResponseType response = getHealthCareUnitMembersResponderInterface.getHealthCareUnitMembers(logicalAddress, parameters);
        return response;
    }

}
