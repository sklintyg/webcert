package se.inera.intyg.webcert.web.service.monitoring;

import java.sql.Time;
import java.util.Enumeration;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.web.service.monitoring.dto.HealthStatus;
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

/**
 * Service for getting the health status of the application.
 *
 * @author npet
 *
 */
@Service("healthCheckService")
public class HealthCheckServiceImpl implements HealthCheckService {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckServiceImpl.class);

    private static final long START_TIME = System.currentTimeMillis();

    private static final String CURR_TIME_SQL = "SELECT CURRENT_TIME()";

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;

    //@Autowired
    //private HSAWebServiceCalls hsaService;

//    @Autowired
//    private OrganizationUnitService organizationUnitService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    @Qualifier("jmsCertificateSenderTemplate")
    private JmsTemplate jmsCertificateSenderTemplate;

    @Autowired
    @Qualifier("jmsFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    @Qualifier("pingIntygstjanstForConfigurationClient")
    private PingForConfigurationResponderInterface intygstjanstPingForConfiguration;

    @Autowired
    @Qualifier("pingForConfigurationResponderInterfaceAuthorizationmanagement")
    private PingForConfigurationResponderInterface pingForConfigurationResponderInterfaceAuthorizationmanagement;

    @Autowired
    @Qualifier("pingForConfigurationResponderInterfaceEmployee")
    private PingForConfigurationResponderInterface pingForConfigurationResponderInterfaceEmployee;

    @Autowired
    @Qualifier("pingForConfigurationResponderInterfaceOrganization")
    private PingForConfigurationResponderInterface pingForConfigurationResponderInterfaceOrganization;

    @Value("${infrastructure.directory.authorizationmanagement.logicalAddress}")
    private String authorizationmanagementLogicalAddress;

    @Value("${infrastructure.directory.employee.logicalAddress}")
    private String employeeLogicalAddress;

    @Value("${infrastructure.directory.organization.logicalAddress}")
    private String organizationLogicalAddress;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Override
    public HealthStatus checkHsaAuthorizationmanagement() {
        boolean ok;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            PingForConfigurationResponseType pingResponse =
                    pingForConfigurationResponderInterfaceAuthorizationmanagement.pingForConfiguration(authorizationmanagementLogicalAddress, buildPingRequest(authorizationmanagementLogicalAddress));

            ok = pingResponse !=  null && pingResponse.getPingDateTime() !=  null;
        } catch (Exception e) {
            ok = false;
        }
        stopWatch.stop();
        HealthStatus status = createStatusWithTiming(ok, stopWatch);
        logStatus("getHsaAuthorizationManagementStatus", status);
        return status;
    }

    @Override
    public HealthStatus checkHsaEmployee() {
        boolean ok;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {

            PingForConfigurationResponseType pingResponse = pingForConfigurationResponderInterfaceEmployee.pingForConfiguration(employeeLogicalAddress, buildPingRequest(employeeLogicalAddress));

            ok = pingResponse !=  null && pingResponse.getPingDateTime() !=  null;
        } catch (Exception e) {
            ok = false;
        }
        stopWatch.stop();
        HealthStatus status = createStatusWithTiming(ok, stopWatch);
        logStatus("getHsaStatus", status);
        return status;
    }

    @Override
    public HealthStatus checkHsaOrganization() {
        boolean ok;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            PingForConfigurationResponseType pingResponse = pingForConfigurationResponderInterfaceOrganization.pingForConfiguration(organizationLogicalAddress, buildPingRequest(organizationLogicalAddress));

            ok = pingResponse !=  null && pingResponse.getPingDateTime() !=  null;
        } catch (Exception e) {
            ok = false;
        }
        stopWatch.stop();
        HealthStatus status = createStatusWithTiming(ok, stopWatch);
        logStatus("getHsaStatus", status);
        return status;
    }

    private PingForConfigurationType buildPingRequest(String logicalAddress) {
        PingForConfigurationType param = new PingForConfigurationType();
        param.setLogicalAddress(logicalAddress);
        param.setServiceContractNamespace("urn:riv:itintegration:monitoring:PingForConfiguration:1:rivtabp21");
        return param;
    }


    @Override
    @Transactional
    public HealthStatus checkDB() {
        boolean ok;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ok = checkTimeFromDb();
        stopWatch.stop();
        HealthStatus status = createStatusWithTiming(ok, stopWatch);
        logStatus("getDbStatus", status);
        return status;
    }

    @Override
    public HealthStatus checkJMS() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        boolean ok = checkJmsConnection();
        stopWatch.stop();
        HealthStatus status = createStatusWithTiming(ok, stopWatch);
        logStatus("getJMSStatus", status);
        return status;
    }

    @Override
    public HealthStatus checkSignatureQueue() {
        int queueDepth = jmsCertificateSenderTemplate.browse((session, browser) -> {
            Enumeration<?> enumeration = browser.getEnumeration();
            int qd = 0;
            while (enumeration.hasMoreElements()) {
                enumeration.nextElement();
                qd++;
            }
            return qd;
        });
        LOG.info("Operation checkSignatureQueue completed with queue size {}", queueDepth);
        return new HealthStatus(queueDepth, true);
    }


    @Override
    public HealthStatus checkIntygstjanst() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        boolean ok = pingIntygstjanst();
        stopWatch.stop();
        HealthStatus status = createStatusWithTiming(ok, stopWatch);
        logStatus("pingIntygstjanst", status);
        return status;
    }

    @Override
    public HealthStatus checkNbrOfUsers() {
        boolean ok;
        long size = -1;
        try {
            List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
            size = allPrincipals.size();
            ok = true;
        } catch (Exception e) {
            ok = false;
        }

        String result = ok ? "OK" : "FAIL";
        LOG.info("Operation checkNbrOfUsers completed with result {}, nbr of users is {}", result, size);

        return new HealthStatus(size, ok);
    }

    @Override
    public HealthStatus checkUptime() {
        long uptime = System.currentTimeMillis() - START_TIME;
        LOG.info("Current system uptime is {}", DurationFormatUtils.formatDurationWords(uptime, true, true));
        return new HealthStatus(uptime, true);
    }

    @Override
    public String checkUptimeAsString() {
        HealthStatus uptime = checkUptime();
        return DurationFormatUtils.formatDurationWords(uptime.getMeasurement(), true, true);
    }

    private boolean pingIntygstjanst() {
        try {
            PingForConfigurationType parameters = new PingForConfigurationType();
            PingForConfigurationResponseType pingResponse = intygstjanstPingForConfiguration.pingForConfiguration(logicalAddress, parameters);
            return (pingResponse != null);
        } catch (Exception e) {
            LOG.error("pingIntygstjanst failed with exception: " + e.getMessage());
            return false;
        }
    }

    private boolean checkJmsConnection() {
        try {
            Connection connection = connectionFactory.createConnection();
            connection.close();
        } catch (JMSException e) {
            LOG.error("checkJmsConnection failed with exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean checkTimeFromDb() {
        Time timestamp;
        try {
            Query query = entityManager.createNativeQuery(CURR_TIME_SQL);
            timestamp = (Time) query.getSingleResult();
        } catch (Exception e) {
            LOG.error("checkTimeFromDb failed with exception: " + e.getMessage());
            return false;
        }
        return timestamp != null;

    }

    private void logStatus(String operation, HealthStatus status) {
        String result = status.isOk() ? "OK" : "FAIL";
        LOG.info("Operation {} completed with result {} in {} ms", operation, result, status.getMeasurement());
    }

    private HealthStatus createStatusWithTiming(boolean ok, StopWatch stopWatch) {
        return new HealthStatus(stopWatch.getTime(), ok);
    }
}
