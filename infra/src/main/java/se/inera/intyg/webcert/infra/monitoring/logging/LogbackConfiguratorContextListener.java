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
package se.inera.intyg.webcert.infra.monitoring.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class LogbackConfiguratorContextListener implements ServletContextListener {

  private static final Logger LOG =
      LoggerFactory.getLogger(LogbackConfiguratorContextListener.class);

  private static final String CLASSPATH = "classpath:";
  private static final String DEFAULTURI = CLASSPATH + "logback-spring.xml";

  /** initialize logback with external configuration file. */
  @Override
  public void contextInitialized(final ServletContextEvent servletContextEvent) {
    final Resource resource = getConfigurationResource(getConfigurationUri(servletContextEvent));

    if (!resource.exists()) {
      LOG.error(
          "Can't read logback configuration from "
              + resource.getDescription()
              + " - Keep default configuration");
      return;
    }

    LOG.info(
        "Found logback configuration "
            + resource.getDescription()
            + " - Overriding default configuration");
    final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    try {
      configure(loggerContext, resource);
    } catch (Exception ex) {
      try {
        new ContextInitializer(loggerContext).autoConfig();
      } catch (JoranException e) {
        LOG.error("Can't fallback to default (auto) configuration", e);
      }
      LOG.error(
          "Can't configure logback from "
              + resource.getDescription()
              + " - Keep default configuration",
          ex);
    }
  }

  private Resource getConfigurationResource(final String uri) {
    return uri.startsWith(CLASSPATH)
        ? new ClassPathResource(uri.substring(CLASSPATH.length()))
        : new FileSystemResource(uri);
  }

  //
  private void configure(final LoggerContext ctx, final Resource config)
      throws IOException, JoranException {
    final JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(ctx);
    ctx.reset();
    final InputStream in = config.getInputStream();
    try {
      jc.doConfigure(in);
    } finally {
      in.close();
    }
    StatusPrinter.printIfErrorsOccured(ctx);
    ctx.start();
  }

  private String getConfigurationUri(final ServletContextEvent ctx) {
    final String name = ctx.getServletContext().getInitParameter("logbackConfigParameter");
    return (name == null || name.isEmpty()) ? DEFAULTURI : System.getProperty(name, DEFAULTURI);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // Do nothing
  }
}
