package se.inera.logsender;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.ResourceType;
import se.riv.ehr.log.v1.ResourcesType;
import se.riv.ehr.log.v1.UserType;

/**
 * @author andreaskaltenbach
 */
public class LogMessageConsumer implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(LogMessageConsumer.class);

    @Autowired
    private BlockingLogSender logSender;

    public void consumeMessage(LogType logType) {

        LOG.debug("Consuming message {}, thread {}", logType, Thread.currentThread());
        logSender.sendLogEntry(logType);

        LOG.debug("Consuming message {} is done", logType);
    }

    private LogType toLogType(Message message) {
        LogType logType = new LogType();

        try {
            String text = ((TextMessage) message).getText();

            UserType user = new UserType();
            user.setUserId(text);
            logType.setUser(user);

            ResourceType resource = new ResourceType();
            logType.setResources(new ResourcesType());
            logType.getResources().getResource().add(resource);

        } catch (JMSException e) {
            throw new RuntimeException("Failed to read incoming JMS message", e);
        }
        return logType;
    }

    @Override
    public void onMessage(Message message) {
        consumeMessage(toLogType(message));
    }
}
