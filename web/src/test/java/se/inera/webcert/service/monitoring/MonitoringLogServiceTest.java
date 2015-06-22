package se.inera.webcert.service.monitoring;


import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;


/**
 * Unit test to assure that the monitoring log produces relevant messages.
 * 
 * @author npet
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MonitoringLogServiceTest {

   
    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    private MonitoringLogService monitoringLogService = new MonitoringLogServiceImpl();

    @Before
    public void setup() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
    }

    @After
    public void teardown() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(mockAppender);
    }

    @Test
    public void testThatMonitoringLogProducesLogMessage() {
        
        monitoringLogService.logUserLogin("ABC123", "test-scheme");

        verify(mockAppender).doAppend(captorLoggingEvent.capture());

        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        assertThat(loggingEvent.getLevel(), equalTo(Level.INFO));

        // assert that messages contains log event message and args
        assertThat(loggingEvent.getFormattedMessage(),
                containsString("USER_LOGIN"));
        assertThat(loggingEvent.getFormattedMessage(),
                containsString("ABC123"));
        assertThat(loggingEvent.getFormattedMessage(),
                containsString("test-scheme"));
    }

}
