package se.inera.intyg.webcert.integration.hsa.stub;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;


/**
 * Created by eriklupander on 2015-12-09.
 */
@Component(value = "pingForConfigurationResponderInterfaceOrganization")
public class PingForConfigurationResponderOrganizationStub implements PingForConfigurationResponderInterface {

    @Override
    public PingForConfigurationResponseType pingForConfiguration(String logicalAddress, PingForConfigurationType parameters) {
        PingForConfigurationResponseType response = new PingForConfigurationResponseType();
        response.setPingDateTime(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        response.setVersion("1.1");

        return response;
    }
}
