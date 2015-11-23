package se.inera.intyg.webcert.web.monitoring;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import se.inera.intyg.webcert.web.service.monitoring.HealthCheckService;
import se.inera.intyg.webcert.web.service.monitoring.dto.HealthStatus;
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.ConfigurationType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

import javax.annotation.PostConstruct;
import javax.jws.WebParam;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implements PingForConfiguration and returns various statuses about the health of the application.
 *
 * @author nikpet
 */
public class PingForConfigurationResponderImpl implements PingForConfigurationResponderInterface {
    private static final Logger LOG = LoggerFactory.getLogger(PingForConfigurationResponderImpl.class);

    @Value("${project.version}")
    private String projectVersion;

    @Value("${buildNumber}")
    private String buildNumberString;

    @Value("${buildTime}")
    private String buildTimeString;

    @Autowired
    private HealthCheckService healthCheck;

    @Override
    public PingForConfigurationResponseType pingForConfiguration(
            @WebParam(partName = "LogicalAddress", name = "LogicalAddress", targetNamespace = "urn:riv:itintegration:registry:1", header = true) String logicalAddress,
            @WebParam(partName = "parameters", name = "PingForConfiguration", targetNamespace = "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1") PingForConfigurationType parameters) {
        PingForConfigurationResponseType response = new PingForConfigurationResponseType();
        response.setPingDateTime(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        LOG.info("Version String: " + projectVersion);
        response.setVersion(projectVersion);

        HealthStatus db = healthCheck.checkDB();
        HealthStatus hsa = healthCheck.checkHSA();
        HealthStatus jms = healthCheck.checkJMS();
        HealthStatus queueSize = healthCheck.checkSignatureQueue();
        HealthStatus pingIntygstjanst = healthCheck.checkIntygstjanst();
        HealthStatus uptime = healthCheck.checkUptime();
        HealthStatus nbrOfUsers = healthCheck.checkNbrOfUsers();

        addConfiguration(response, "buildNumber", buildNumberString);
        addConfiguration(response, "buildTime", buildTimeString);
        addConfiguration(response, "systemUptime", DurationFormatUtils.formatDurationWords(uptime.getMeasurement(), true, true));
        addConfiguration(response, "dbStatus", db.isOk() ? "ok" : "error");
        addConfiguration(response, "hsaStatus", hsa.isOk() ? "ok" : "error");
        addConfiguration(response, "jmsStatus", jms.isOk() ? "ok" : "error");
        addConfiguration(response, "intygstjanst", pingIntygstjanst.isOk() ? "ok" : "no connection");
        addConfiguration(response, "signatureQueueSize", "" + queueSize.getMeasurement());
        addConfiguration(response, "nbrOfUsers", "" + nbrOfUsers.getMeasurement());

        return response;
    }

    private void addConfiguration(PingForConfigurationResponseType response, String name, String value) {
        ConfigurationType conf = new ConfigurationType();
        conf.setName(name);
        conf.setValue(value);
        response.getConfiguration().add(conf);
    }

    @PostConstruct
    public void init() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
}
