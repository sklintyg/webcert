package se.inera.logsender;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.stereotype.Component;
import se.inera.log.messages.AbstractLogMessage;
import se.inera.log.messages.Enhet;
import se.inera.log.messages.Patient;
import se.inera.logsender.exception.LoggtjanstExecutionException;
import se.riv.ehr.log.store.storelog.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelog.v1.StoreLogResponderInterface;
import se.riv.ehr.log.store.storelog.v1.StoreLogResponseType;
import se.riv.ehr.log.v1.ActivityType;
import se.riv.ehr.log.v1.CareProviderType;
import se.riv.ehr.log.v1.CareUnitType;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.PatientType;
import se.riv.ehr.log.v1.ResourceType;
import se.riv.ehr.log.v1.ResourcesType;
import se.riv.ehr.log.v1.SystemType;
import se.riv.ehr.log.v1.UserType;

/**
 * @author andreaskaltenbach
 */
@Component
public class LogSender {

    private static final Logger LOG = LoggerFactory.getLogger(LogSender.class);

    @Value( "${loggtjanst.logicalAddress}" )
    private String logicalAddress;

    @Value( "${logsender.bulkSize}" )
    private int bulkSize;

    @Autowired
    private StoreLogResponderInterface loggTjanstResponder;

    @Autowired
    @Qualifier( "jmsTemplate" )
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier( "nonTransactedJmsTemplate" )
    private JmsTemplate nonTransactedJmsTemplate;

    @Autowired
    private Queue queue;

    @PostConstruct
    public void checkConfiguration() {
        if (bulkSize == 0) {
            throw new IllegalStateException("'bulkSize' has to be greater than zero");
        }
    }

    private int bulkSize() {
        return nonTransactedJmsTemplate.execute(new SessionCallback<Integer>() {
            @Override
            public Integer doInJms(Session session) throws JMSException {
                QueueBrowser queueBrowser = session.createBrowser(queue);
                Enumeration queueMessageEnum = queueBrowser.getEnumeration();
                int count = 0;
                while (queueMessageEnum.hasMoreElements() && count < bulkSize) {
                    queueMessageEnum.nextElement();
                    count++;
                }
                return count;
            }
        }, true);
    }

    public void sendLogEntries() {

        final int chunk = bulkSize();

        if (chunk == 0) {
            LOG.info("Zero messages in logging queue. Nothing will be sent to loggtjänst");
        } else {
            Boolean reExecute = jmsTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public Boolean doInJms(Session session) throws JMSException {

                    int count = chunk;

                    LOG.info("Transferring " + count + " log entries to loggtjänst.");

                    MessageConsumer consumer = session.createConsumer(queue);

                    List<Message> messages = new ArrayList<>();
                    while (count > 0) {
                        messages.add(consumer.receive());
                        count--;
                    }

                    try {
                        sendLogEntriesToLoggtjanst(convert(messages));
                        session.commit();
                        return true;
                    } catch (LoggtjanstExecutionException e) {
                        LOG.warn("Failed to send log entries to loggtjänst, JMS session will be rolled back.", e);
                        session.rollback();
                        return false;
                    }
                }
            }, true);

            // there may be messages left on the queue after the first chunk, so reperform the action
            if (reExecute) {
                sendLogEntries();
            }
        }

    }

    private List<LogType> convert(List<Message> messages) {
        List<LogType> logTypes = new ArrayList<>();
        for (Message message : messages) {
            logTypes.add(convert(message));
        }
        return logTypes;
    }

    private LogType convert(Message message) {
        try {
            Object element = ((ObjectMessage) message).getObject();

            if (element instanceof AbstractLogMessage) {
                AbstractLogMessage logMessage = (AbstractLogMessage) element;
                return convert(logMessage);

            } else {
                throw new RuntimeException("Unrecognized message type " + element.getClass().getCanonicalName());
            }

        } catch (JMSException e) {
            throw new RuntimeException("Failed to read incoming JMS message", e);
        }
    }

    private LogType convert(AbstractLogMessage source) {
        LogType logType = new LogType();

        logType.setLogId(source.getLogId());

        SystemType system = new SystemType();
        system.setSystemId(source.getSystemId());
        logType.setSystem(system);

        ActivityType activity = new ActivityType();
        activity.setActivityType(source.getActivityType().getType());
        activity.setStartDate(source.getTimestamp());
        activity.setPurpose(source.getPurpose().getType());
        activity.setActivityLevel(source.getActivityLevel());
        logType.setActivity(activity);

        UserType user = new UserType();
        user.setUserId(source.getUserId());
        user.setName(source.getUserName());
        user.setCareProvider(careProvider(source.getEnhet()));
        user.setCareUnit(careUnit(source.getEnhet()));
        logType.setUser(user);

        logType.setResources(new ResourcesType());
        ResourceType resource = new ResourceType();
        resource.setResourceType(source.getResourceType());
        resource.setCareProvider(careProvider(source.getEnhet()));
        resource.setCareUnit(careUnit(source.getEnhet()));

        resource.setPatient(patient(source.getPatient()));

        logType.getResources().getResource().add(resource);

        return logType;
    }

    private PatientType patient(Patient source) {
        PatientType patient = new PatientType();
        patient.setPatientId(source.getPatientId().replace("-", ""));
        patient.setPatientName(source.getPatientNamn());
        return patient;
    }

    private CareUnitType careUnit(Enhet source) {
        CareUnitType careUnit = new CareUnitType();
        careUnit.setCareUnitId(source.getEnhetsId());
        careUnit.setCareUnitName(source.getEnhetsNamn());
        return careUnit;
    }

    private CareProviderType careProvider(Enhet source) {
        CareProviderType careProvider = new CareProviderType();
        careProvider.setCareProviderId(source.getVardgivareId());
        careProvider.setCareProviderName(source.getVardgivareNamn());
        return careProvider;
    }

    private void sendLogEntriesToLoggtjanst(List<LogType> logEntries) {

        StoreLogRequestType request = new StoreLogRequestType();
        request.getLog().addAll(logEntries);

        try {
            StoreLogResponseType response = loggTjanstResponder.storeLog(logicalAddress, request);
            switch (response.getResultType().getResultCode()) {
                case OK:
                case INFO:
                    break;
                default:
                    throw new LoggtjanstExecutionException();
            }
        } catch (WebServiceException e) {
            throw new LoggtjanstExecutionException(e);
        }

    }
}
