/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Exposes health metrics via Micrometer. All metrics produced by this component use the following
 * conventions:
 *
 * <p>All metrics are prefixed with "health_" All metrics are suffixed with their type, either
 * "_normal" that indicates a boolean value 0 or 1 OR "_value" that indicates a numeric metric of
 * some kind.
 *
 * <p>Note that NORMAL values use 0 to indicate OK state and 1 to indicate a problem.
 *
 * <p>Gauges are registered with pull-based supplier lambdas so that the health checks run on each
 * Prometheus scrape.
 *
 * @author eriklupander
 */
@Component
public class HealthMonitor {

  private static final String PREFIX = "health_";
  private static final String NORMAL = "_normal";
  private static final String VALUE = "_value";

  private static final long START_TIME = System.currentTimeMillis();
  private static final long MILLIS_PER_SECOND = 1000L;

  private static final String PING_SQL = "SELECT 1";

  @PersistenceContext private EntityManager entityManager;

  @Autowired
  @Qualifier("jmsCertificateSenderTemplate") private JmsTemplate jmsCertificateSenderTemplate;

  @Autowired private ConnectionFactory connectionFactory;

  @Autowired
  @Qualifier("rediscache") private RedisTemplate<Object, Object> redisTemplate;

  @Autowired private MeterRegistry meterRegistry;

  @Value("${intygstjanst.metrics.url}")
  private String itMetricsUrl;

  @FunctionalInterface
  interface Tester {

    void run() throws Exception;
  }

  @PostConstruct
  public void init() {
    Gauge.builder(
            PREFIX + "uptime" + VALUE,
            this,
            m -> (double) ((System.currentTimeMillis() - START_TIME) / MILLIS_PER_SECOND))
        .description("Current uptime in seconds")
        .register(meterRegistry);

    Gauge.builder(PREFIX + "db_accessible" + NORMAL, this, m -> m.checkDbConnection() ? 0.0 : 1.0)
        .description("0 == OK 1 == NOT OK")
        .register(meterRegistry);

    Gauge.builder(PREFIX + "jms_accessible" + NORMAL, this, m -> m.checkJmsConnection() ? 0.0 : 1.0)
        .description("0 == OK 1 == NOT OK")
        .register(meterRegistry);

    Gauge.builder(
            PREFIX + "intygstjanst_accessible" + NORMAL,
            this,
            m -> m.pingIntygstjanst() ? 0.0 : 1.0)
        .description("0 == OK 1 == NOT OK")
        .register(meterRegistry);

    Gauge.builder(
            PREFIX + "signature_queue_depth" + VALUE, this, m -> (double) m.checkSignatureQueue())
        .description("Number of waiting messages")
        .register(meterRegistry);
  }

  private boolean checkJmsConnection() {
    return invoke(
        () -> {
          Connection connection = connectionFactory.createConnection();
          connection.close();
        });
  }

  private boolean checkDbConnection() {
    return invoke(() -> entityManager.createNativeQuery(PING_SQL).getSingleResult());
  }

  private int checkSignatureQueue() {
    try {
      return jmsCertificateSenderTemplate.browse(
          (session, browser) -> {
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

  private boolean invoke(Tester tester) {
    try {
      tester.run();
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private boolean pingIntygstjanst() {
    return invoke(
        () -> {
          HttpURLConnection httpConnection =
              (HttpURLConnection) new URL(itMetricsUrl).openConnection();
          int respCode = httpConnection.getResponseCode();
          httpConnection.disconnect();
          if (respCode != HttpServletResponse.SC_OK) {
            throw new RuntimeException();
          }
        });
  }
}
