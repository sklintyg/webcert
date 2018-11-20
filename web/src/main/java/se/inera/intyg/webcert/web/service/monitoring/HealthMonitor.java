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
import java.util.Collections;
import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.prometheus.client.Gauge;
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

/**
 * Exposes health metrics as Prometheus values. To simplify any 3rd party scraping applications, all metrics produced
 * by this component uses the following conventions:
 *
 * All metrics are prefixed with "health_"
 * All metrics are suffixed with their type, either "_normal" that indicates a boolean value 0 or 1 OR
 * "_value" that indiciates a numeric metric of some kind.
 *
 * Note that NORMAL values uses 0 to indicate OK state and 1 to indicate a problem.
 *
 * @author eriklupander
 */
@EnableScheduling
@Component
public class HealthMonitor {

    private static final String PREFIX = "health_";
    private static final String NORMAL = "_normal";
    private static final String VALUE = "_value";

    private static final Gauge LOGGED_IN_USERS = Gauge.build()
            .name(PREFIX + "logged_in_users" + VALUE)
            .help("Current number of logged in users")
            .register();

    private static final Gauge DB_ACCESSIBLE = Gauge.build()
            .name(PREFIX + "db_accessible" + NORMAL)
            .help("0 == OK 1 == NOT OK")
            .register();

    private static final Gauge JMS_ACCESSIBLE = Gauge.build()
            .name(PREFIX + "jms_accessible" + NORMAL)
            .help("0 == OK 1 == NOT OK")
            .register();

    private static final Gauge IT_ACCESSIBLE = Gauge.build()
            .name(PREFIX + "intygstjanst_accessible" + NORMAL)
            .help("0 == OK 1 == NOT OK")
            .register();

    private static final Gauge PLP_ACCESSIBLE = Gauge.build()
            .name(PREFIX + "privatlakarportal_accessible" + NORMAL)
            .help("0 == OK 1 == NOT OK")
            .register();

    private static final Gauge SIGNATURE_QUEUE_DEPTH = Gauge.build()
            .name(PREFIX + "signature_queue_depth" + VALUE)
            .help("Number of waiting messages")
            .register();

    // Runs a lua script to count number of keys matching our session keys.
    private final RedisScript<Long> redisScript = new DefaultRedisScript<>(
            "return #redis.call('keys','spring:session:webcert:index:*')", Long.class);

    private static final long DELAY = 30000L;
    private static final String CURR_TIME_SQL = "SELECT CURRENT_TIME()";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    @Qualifier("jmsCertificateSenderTemplate")
    private JmsTemplate jmsCertificateSenderTemplate;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    @Qualifier("rediscache")
    private RedisTemplate<Object, Object> redisTemplate;

    @Value("${intygstjanst.logicaladdress}")
    private String itLogicalAddress;

    @Value("${privatepractitioner.logicaladdress}")
    private String ppLogicalAddress;

    @Autowired
    @Qualifier("pingIntygstjanstForConfigurationClient")
    private PingForConfigurationResponderInterface intygstjanstPingForConfiguration;

    @Autowired
    @Qualifier("pingPrivatlakarportalForConfigurationClient")
    private PingForConfigurationResponderInterface privatlakarportalPingForConfiguration;

    @Scheduled(fixedDelay = DELAY)
    public void healthCheck() {
        LOGGED_IN_USERS.set(countSessions());
        DB_ACCESSIBLE.set(checkTimeFromDb() ? 0 : 1);
        JMS_ACCESSIBLE.set(checkJmsConnection() ? 0 : 1);
        PLP_ACCESSIBLE.set(pingPrivatlakarportal() ? 0 : 1);
        IT_ACCESSIBLE.set(pingIntygstjanst() ? 0 : 1);
        SIGNATURE_QUEUE_DEPTH.set(checkSignatureQueue());
    }

    private int countSessions() {
        Long numberOfUsers = redisTemplate.execute(redisScript, Collections.emptyList());
        return numberOfUsers.intValue();
    }

    private boolean checkJmsConnection() {
        try {
            Connection connection = connectionFactory.createConnection();
            connection.close();
        } catch (JMSException e) {
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
            return false;
        }
        return timestamp != null;
    }

    private int checkSignatureQueue() {
        try {
            return jmsCertificateSenderTemplate.browse((session, browser) -> {
                Enumeration<?> enumeration = browser.getEnumeration();
                int qd = 0;
                while (enumeration.hasMoreElements()) {
                    enumeration.nextElement();
                    qd++;
                }
                return qd;
            });
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean pingPrivatlakarportal() {
        try {
            PingForConfigurationType parameters = new PingForConfigurationType();
            PingForConfigurationResponseType pingResponse = privatlakarportalPingForConfiguration.pingForConfiguration(ppLogicalAddress,
                    parameters);
            return pingResponse != null;
        } catch (Exception e) {
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
            return false;
        }
    }

}
