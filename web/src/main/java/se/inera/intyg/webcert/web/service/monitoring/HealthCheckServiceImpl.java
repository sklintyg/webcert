/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.monitoring;

import java.sql.Time;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Stopwatch;

import se.inera.intyg.webcert.web.service.monitoring.dto.HealthStatus;
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

/**
 * Service for getting the health status of the application.
 *
 * @author npet
 */
@Service("healthCheckService")
public class HealthCheckServiceImpl implements HealthCheckService {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckServiceImpl.class);

    private static final long START_TIME = System.currentTimeMillis();

    private static final String CURR_TIME_SQL = "SELECT CURRENT_TIME()";

    @Value("${intygstjanst.logicaladdress}")
    private String itLogicalAddress;

    @Value("${privatepractitioner.logicaladdress}")
    private String ppLogicalAddress;

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
    @Qualifier("pingPrivatlakarportalForConfigurationClient")
    private PingForConfigurationResponderInterface privatlakarportalPingForConfiguration;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Override
    @Transactional
    public HealthStatus checkDB() {
        boolean ok;
        Stopwatch stopWatch = Stopwatch.createStarted();
        ok = checkTimeFromDb();
        stopWatch.stop();
        HealthStatus status = createStatusWithTiming(ok, stopWatch);
        logStatus("getDbStatus", status);
        return status;
    }

    @Override
    public HealthStatus checkJMS() {
        Stopwatch stopWatch = Stopwatch.createStarted();
        boolean ok = checkJmsConnection();
        stopWatch.stop();
        HealthStatus status = createStatusWithTiming(ok, stopWatch);
        logStatus("getJMSStatus", status);
        return status;
    }

    @Override
    public HealthStatus checkSignatureQueue() {
        try {
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
        } catch (Exception e) {
            LOG.warn("Error when checking queue depth", e);
            return new HealthStatus(-1, false);
        }
    }

    @Override
    public HealthStatus checkIntygstjanst() {
        Stopwatch stopWatch = Stopwatch.createStarted();
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

    @Override
    public HealthStatus checkPrivatlakarportal() {
        Stopwatch stopWatch = Stopwatch.createStarted();
        boolean ok = pingPrivatlakarportal();
        stopWatch.stop();
        HealthStatus status = createStatusWithTiming(ok, stopWatch);
        logStatus("pingPrivatlakarportal", status);
        return status;
    }

    private boolean pingPrivatlakarportal() {
        try {
            PingForConfigurationType parameters = new PingForConfigurationType();
            PingForConfigurationResponseType pingResponse = privatlakarportalPingForConfiguration.pingForConfiguration(ppLogicalAddress,
                    parameters);
            return pingResponse != null;
        } catch (Exception e) {
            LOG.error("pingPrivatlakarportal failed with exception: " + e.getMessage());
            return false;
        }
    }

    private boolean pingIntygstjanst() {
        try {
            PingForConfigurationType parameters = new PingForConfigurationType();
            PingForConfigurationResponseType pingResponse = intygstjanstPingForConfiguration.pingForConfiguration(itLogicalAddress,
                    parameters);
            return pingResponse != null;
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

    private HealthStatus createStatusWithTiming(boolean ok, Stopwatch stopWatch) {
        return new HealthStatus(stopWatch.elapsed(TimeUnit.MILLISECONDS), ok);
    }
}
