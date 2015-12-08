package se.inera.intyg.webcert.integration.hsa.client;

import se.riv.infrastructure.directory.organization.gethealthcareunitmembersresponder.v1.GetHealthCareUnitMembersResponseType;
import se.riv.infrastructure.directory.organization.gethealthcareunitresponder.v1.GetHealthCareUnitResponseType;
import se.riv.infrastructure.directory.organization.getunitresponder.v1.GetUnitResponseType;
import se.riv.itintegration.monitoring.pingforconfigurationresponder.v1.PingForConfigurationResponseType;

/**
 * Created by eriklupander on 2015-12-03.
 */
public interface OrganizationUnitService {
    GetUnitResponseType getUnit(String unitHsaId);

    GetHealthCareUnitResponseType getHealthCareUnit(String hsaId);

    GetHealthCareUnitMembersResponseType getHealthCareUnitMembers(String unitHsaId);

    PingForConfigurationResponseType ping();
}
