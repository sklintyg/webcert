package se.inera.webcert.service;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import se.inera.log.messages.AbstractLogMessage;
import se.inera.log.messages.Enhet;
import se.inera.log.messages.IntygPrintMessage;
import se.inera.log.messages.IntygReadMessage;
import se.inera.log.messages.Patient;
import se.inera.webcert.hsa.model.SelectableVardenhet;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * @author andreaskaltenbach
 */
@Service
public class LogServiceImpl implements LogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogServiceImpl.class);
    
    private static final String NOT_AVAILABLE = "-";

    @Autowired(required = false)
    JmsTemplate jmsTemplate;

    @Value("${pdlLogging.systemId}")
    String systemId;
    
    @Value("${pdlLogging.systemName}")
    String systemName;

    @Autowired
    private WebCertUserService webCertUserService;

    @PostConstruct
    public void checkJmsTemplate() {
        if (jmsTemplate == null) {
            LOGGER.error("PDL logging is disabled!");
        }
    }

    @Override
    public void logReadOfIntyg(String intygId, String patientId) {
        
        if (jmsTemplate == null) {
            LOGGER.warn("Can not log read of Intyg since PDL logging is disabled!");
            return;
        }
        
        IntygReadMessage logMsg = new IntygReadMessage(intygId);
        populateLogMessage(patientId, logMsg);
        send(logMsg);
    }

    @Override
    public void logPrintOfIntyg(String intygId, String patientId) {

        if (jmsTemplate == null) {
            LOGGER.warn("Can not log print of Intyg since PDL logging is disabled!");
            return;
        }

        IntygPrintMessage logMsg = new IntygPrintMessage(intygId);
        populateLogMessage(patientId, logMsg);
        send(logMsg);
    }

    private void populateLogMessage(String patientId, AbstractLogMessage logMsg) {
        
        Patient patient = new Patient(patientId);
        logMsg.setPatient(patient);

        logMsg.setSystemId(systemId);
        logMsg.setSystemName(systemName);

        WebCertUser user = webCertUserService.getWebCertUser();

        logMsg.setUserId(user.getHsaId());
        logMsg.setUserName(user.getNamn());
        
        SelectableVardenhet vardgivare = user.getValdVardgivare();

        if (vardgivare == null) {
            LOGGER.error("Can not populate log message, vardgivare is null for user: {}", user.getAsJson());
        }

        String vardgivareId = (vardgivare != null) ? vardgivare.getId() : NOT_AVAILABLE;
        String vardgivareNamn = (vardgivare != null) ? vardgivare.getNamn() : NOT_AVAILABLE;

        SelectableVardenhet enhet = user.getValdVardenhet();
        
        if (enhet == null) {
            LOGGER.error("Can not populate log message, vardenhet is null for user: {}", user.getAsJson());
        }

        String enhetsId = (enhet != null) ? enhet.getId() : NOT_AVAILABLE;
        String enhetsNamn = (enhet != null) ? enhet.getNamn() : NOT_AVAILABLE;

        logMsg.setEnhet(new Enhet(enhetsId, enhetsNamn, vardgivareId, vardgivareNamn));
    }

    private void send(AbstractLogMessage logMsg) {
        
        LOGGER.debug("Logging {} of Intyg {}", logMsg.getActivityType(), logMsg.getActivityLevel());
        
        jmsTemplate.send(new MC(logMsg));
    }
    
    private static final class MC implements MessageCreator {
        private final AbstractLogMessage logMsg;

        public MC(AbstractLogMessage log) {
            this.logMsg = log;
        }

        public Message createMessage(Session session) throws JMSException {
            return session.createObjectMessage(logMsg);
        }
    }
}
