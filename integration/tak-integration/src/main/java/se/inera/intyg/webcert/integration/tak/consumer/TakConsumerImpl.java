package se.inera.intyg.webcert.integration.tak.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.webcert.integration.tak.model.ConnectionPoint;
import se.inera.intyg.webcert.integration.tak.model.ServiceContract;
import se.inera.intyg.webcert.integration.tak.model.TakLogicalAddress;

public class TakConsumerImpl implements TakConsumer {

    private static final String LOGICAL_ADDRESS_URL = "%s/logicalAddresss?logicalAddress=%s&connectionPointId=%s&serviceContractId=%s";

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
    public String getConnectionPointId() {
        String url = String.format("%s/connectionPoints?platform=%s&environment=%s",
                baseUrl, "NTJP", environment);
        ConnectionPoint[] tmp = customRestTemplate.getForEntity(url, ConnectionPoint[].class).getBody();
        return tmp.length > 0 ? tmp[0].getId() : "-";
    }

    @Override
    public String getServiceContractId(String contract) {
        String url = String.format("%s/serviceContracts?namespace=%s", baseUrl, contract);
        ServiceContract[] tmp = customRestTemplate.getForEntity(url, ServiceContract[].class).getBody();
        return tmp.length > 0 ? tmp[0].getId() : "-";
    }
}
