package se.inera.webcert.service.monitoring;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
import se.inera.webcert.persistence.utkast.repository.OmsandningRepository;
import se.inera.webcert.service.monitoring.dto.HealthStatus;
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.sql.Time;
import java.util.List;

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

    @Autowired
    private HSAWebServiceCalls hsaService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    @Qualifier("jmsFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    private OmsandningRepository omsandningRepository;

    @Autowired
    @Qualifier("pingIntygstjanstForConfigurationClient")
    private PingForConfigurationResponderInterface intygstjanstPingForConfiguration;

    @Autowired
    private SessionRegistry sessionRegistry;

    public HealthStatus checkHSA() {
        boolean ok;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            hsaService.callPing();
            ok = true;
        } catch (Exception e) {
            ok = false;
        }
        stopWatch.stop();
        HealthStatus status = createStatusWithTiming(ok, stopWatch);
        logStatus("getHsaStatus", status);
        return status;
    }

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

    public HealthStatus checkJMS() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        boolean ok = checkJmsConnection();
        stopWatch.stop();
        HealthStatus status = createStatusWithTiming(ok, stopWatch);
        logStatus("getJMSStatus", status);
        return status;
    }

    public HealthStatus checkSignatureQueue() {
        boolean ok;
        long size = -1;
        try {
            size = omsandningRepository.count();
            ok = true;
        } catch (Exception e) {
            ok = false;
        }

        String result = ok ? "OK" : "FAIL";
        LOG.info("Operation checkSignatureQueue completed with result {}, queue size is {}", result, size);

        return new HealthStatus(size, ok);
    }

    public HealthStatus checkIntygstjanst() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        boolean ok = pingIntygstjanst();
        stopWatch.stop();
        HealthStatus status = createStatusWithTiming(ok, stopWatch);
        logStatus("pingIntygstjanst", status);
        return status;
    }

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

    public HealthStatus checkUptime() {
        long uptime = System.currentTimeMillis() - START_TIME;
        LOG.info("Current system uptime is {}", DurationFormatUtils.formatDurationWords(uptime, true, true));
        return new HealthStatus(uptime, true);
    }

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
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
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
        LOG.info("Operation {} completed with result {} in {} ms", new Object[] { operation, result, status.getMeasurement() });
    }

    private HealthStatus createStatusWithTiming(boolean ok, StopWatch stopWatch) {
        return new HealthStatus(stopWatch.getTime(), ok);
    }
}
