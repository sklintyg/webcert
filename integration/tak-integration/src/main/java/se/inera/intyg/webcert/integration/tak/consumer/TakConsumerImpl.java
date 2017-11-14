/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.tak.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.webcert.integration.tak.model.ConnectionPoint;
import se.inera.intyg.webcert.integration.tak.model.ServiceContract;
import se.inera.intyg.webcert.integration.tak.model.TakLogicalAddress;

import java.util.Optional;

public class TakConsumerImpl implements TakConsumer {

    private static final String LOGICAL_ADDRESS_URL = "%s/logicalAddresss?logicalAddress=%s&connectionPointId=%s&serviceContractId=%s";
    private static final String CONNECTION_POINT_URL = "%s/connectionPoints?platform=%s&environment=%s";
    private static final String SERVICE_CONTRACT_ID_URL = "%s/serviceContracts?namespace=%s";
    private static final String PLATFORM = "NTJP";

    @Autowired
    private RestTemplate customRestTemplate;

    @Value("${tak.environment}")
    private String environment;

    @Value("${tak.base.url}")
    private String baseUrl;

    @Override
    public TakLogicalAddress[] doLookup(String ntjpId, String careUnitId, String contract) {
        return customRestTemplate.getForEntity(String.format(LOGICAL_ADDRESS_URL,
                baseUrl, careUnitId, ntjpId, contract), TakLogicalAddress[].class).getBody();
    }

    @Override
    public String getConnectionPointId() throws TakServiceException{
        String url = String.format(CONNECTION_POINT_URL, baseUrl, PLATFORM, environment);
        ConnectionPoint[] tmp = customRestTemplate.getForEntity(url, ConnectionPoint[].class).getBody();
        if (tmp.length > 0) {
            return tmp[0].getId();
        }
        throw new TakServiceException("Failed to get ConnectionPointId");
    }

    @Override
    public String getServiceContractId(String contract) throws TakServiceException{
        String url = String.format(SERVICE_CONTRACT_ID_URL, baseUrl, contract);
        ServiceContract[] tmp = customRestTemplate.getForEntity(url, ServiceContract[].class).getBody();
        if (tmp.length > 0) {
            return tmp[0].getId();
        }
        throw new TakServiceException("Failed to get ServiceContractId");
    }
}
