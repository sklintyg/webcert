package se.inera.logsender;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.stereotype.Component;
import se.inera.logsender.exception.LoggtjanstExecutionException;
import se.riv.ehr.log.store.storelog.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelog.v1.StoreLogResponderInterface;
import se.riv.ehr.log.store.storelog.v1.StoreLogResponseType;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.ResourceType;
import se.riv.ehr.log.v1.ResourcesType;
import se.riv.ehr.log.v1.UserType;

/**
 * @author andreaskaltenbach
 */
@Component
public class LogSender {

    private static final Logger LOG = LoggerFactory.getLogger(LogSender.class);

    @Value("${logsender.deliveryIntervalInSeconds}")
    private int deliveryIntervalInSeconds;

    @Value("${logsender.bulkSize}")
    private int bulkSize;

    @Autowired
    private StoreLogResponderInterface loggTjanstResponder;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Queue queue;

    @PostConstruct
    public void checkConfiguration() {
        if (deliveryIntervalInSeconds == 0) {
            throw new IllegalStateException("'deliveryIntervalInSeconds' has to be greater than zero");
        }

        if (bulkSize == 0) {
            throw new IllegalStateException("'bulkSize' has to be greater than zero");
        }
    }

    private int queueDepth(Session session) throws JMSException {
        QueueBrowser queueBrowser = session.createBrowser(queue);
        Enumeration queueMessageEnum = queueBrowser.getEnumeration();
        int count = 0;
        while (queueMessageEnum.hasMoreElements()) {
            queueMessageEnum.nextElement();
            count++;
        }
        return count;
    }

    public void sendLogEntries() {

        jmsTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object doInJms(Session session) throws JMSException {

                int queueDepth = queueDepth(session);
                int chunk = Math.min(queueDepth, bulkSize);


                if (chunk > 0) {
                    LOG.info("Transferring " + chunk + " log entries to loggtjänst.");

                    // consume messages
                    MessageConsumer consumer = session.createConsumer(queue);

                    List<Message> messages = new ArrayList<>();
                    while (chunk > 0) {
                        messages.add(consumer.receive());
                        chunk--;
                    }

                    try {
                        sendLogEntriesToLoggtjanst(convert(messages));
                        session.commit();
                        sendLogEntries();

                    } catch (LoggtjanstExecutionException e) {
                        LOG.warn("Failed to send log entries to loggtjänst, JMS session will be rolled back.");
                        session.rollback();
                    }

                } else {
                    LOG.info("Zero messages in logging queue. Nothing will be sent to loggtjänst");
                    session.commit();
                }

                return null;
            }
        }, true);
    }

    private List<LogType> convert(List<Message> messages) {
        List<LogType> logTypes = new ArrayList<>();
        for (Message message : messages) {
            logTypes.add(convert(message));
        }
        return logTypes;
    }

    private LogType convert(Message message) {
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

    private void sendLogEntriesToLoggtjanst(List<LogType> logEntries) {

        StoreLogRequestType request = new StoreLogRequestType();
        request.getLog().addAll(logEntries);
        StoreLogResponseType response = loggTjanstResponder.storeLog(null, request);
        switch (response.getResultType().getResultCode()) {
        case OK:
        case INFO:
            break;
        default:
            throw new LoggtjanstExecutionException();
        }
    }

}
