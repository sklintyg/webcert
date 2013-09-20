package se.inera.webcert.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

import javax.jws.WebParam;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author johannesc
 */
public class PingForConfigurationResponderImpl implements PingForConfigurationResponderInterface {

    @Autowired
    @Value("${project.version}")
    private String projectVersion;

    @Override
    public PingForConfigurationResponseType pingForConfiguration(@WebParam(partName = "LogicalAddress", name = "LogicalAddress", targetNamespace = "urn:riv:itintegration:registry:1", header = true) String logicalAddress, @WebParam(partName = "parameters", name = "PingForConfiguration", targetNamespace = "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1") PingForConfigurationType parameters) {
        PingForConfigurationResponseType response = new PingForConfigurationResponseType();
        response.setPingDateTime(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        response.setVersion(projectVersion);
        return response;
    }
}
