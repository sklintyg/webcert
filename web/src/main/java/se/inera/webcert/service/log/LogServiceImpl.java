package se.inera.webcert.service.log;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import se.inera.log.messages.AbstractLogMessage;
import se.inera.log.messages.Enhet;
import se.inera.log.messages.IntygCreateMessage;
import se.inera.log.messages.IntygDeleteMessage;
import se.inera.log.messages.IntygPrintMessage;
import se.inera.log.messages.IntygReadMessage;
import se.inera.log.messages.IntygRevokeMessage;
import se.inera.log.messages.IntygSendMessage;
import se.inera.log.messages.IntygSignMessage;
import se.inera.log.messages.IntygUpdateMessage;
import se.inera.log.messages.Patient;
import se.inera.webcert.hsa.model.SelectableVardenhet;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * Implementation of service for logging user actions according to PDL requirements.
 *
 * @author nikpet
 */
@Service
public class LogServiceImpl implements LogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogServiceImpl.class);

    private static final String PRINTED_AS_PDF = "Intyget utskrivet som PDF";
    private static final String PRINTED_AS_DRAFT = "Intyget utskrivet som utkast";

    @Autowired(required = false)
    @Qualifier("jmsPDLLogTemplate")
    private JmsTemplate jmsTemplate;

    @Value("${pdlLogging.systemId}")
    private String systemId;

    @Value("${pdlLogging.systemName}")
    private String systemName;

    @Autowired
    private WebCertUserService webCertUserService;

    @PostConstruct
    public void checkJmsTemplate() {
        if (jmsTemplate == null) {
            LOGGER.error("PDL logging is disabled!");
        }
    }

    @Override
    public void logCreateIntyg(LogRequest logRequest, WebCertUser user) {
        send(populateLogMessage(logRequest, new IntygCreateMessage(logRequest.getIntygId()), user));
    }

    @Override
    public void logUpdateIntyg(LogRequest logRequest, WebCertUser user) {
        send(populateLogMessage(logRequest, new IntygUpdateMessage(logRequest.getIntygId()), user));
    }

    @Override
    public void logReadOfIntyg(LogRequest logRequest, WebCertUser user) {
        send(populateLogMessage(logRequest, new IntygReadMessage(logRequest.getIntygId()), user));
    }

    @Override
    public void logDeleteIntyg(LogRequest logRequest, WebCertUser user) {
        send(populateLogMessage(logRequest, new IntygDeleteMessage(logRequest.getIntygId()), user));
    }

    @Override
    public void logSignIntyg(LogRequest logRequest, WebCertUser user) {
        send(populateLogMessage(logRequest, new IntygSignMessage(logRequest.getIntygId()), user));
    }

    @Override
    public void logRevokeIntyg(LogRequest logRequest, WebCertUser user) {
        send(populateLogMessage(logRequest, new IntygRevokeMessage(logRequest.getIntygId()), user));
    }

    @Override
    public void logPrintOfIntygAsPDF(LogRequest logRequest, WebCertUser user) {
        send(populateLogMessage(logRequest, new IntygPrintMessage(logRequest.getIntygId(), PRINTED_AS_PDF), user));
    }

    @Override
    public void logPrintOfIntygAsDraft(LogRequest logRequest, WebCertUser user) {
        send(populateLogMessage(logRequest, new IntygPrintMessage(logRequest.getIntygId(), PRINTED_AS_DRAFT), user));
    }

    @Override
    public void logSendIntygToRecipient(LogRequest logRequest, WebCertUser user) {
        send(populateLogMessage(logRequest, new IntygSendMessage(logRequest.getIntygId(), logRequest.getAdditionalInfo()), user));
    }

    private AbstractLogMessage populateLogMessage(LogRequest logRequest, AbstractLogMessage logMsg, WebCertUser user) {

        populateWithCurrentUserAndCareUnit(logMsg, user);

        Patient patient = new Patient(logRequest.getPatientId(), logRequest.getPatientName());
        logMsg.setPatient(patient);

        String careUnitId = logRequest.getIntygCareUnitId();
        String careUnitName = logRequest.getIntygCareUnitName();

        String careGiverId = logRequest.getIntygCareGiverId();
        String careGiverName = logRequest.getIntygCareGiverName();

        Enhet resourceOwner = new Enhet(careUnitId, careUnitName, careGiverId, careGiverName);
        logMsg.setResourceOwner(resourceOwner);

        logMsg.setSystemId(systemId);
        logMsg.setSystemName(systemName);

        return logMsg;
    }

    private void populateWithCurrentUserAndCareUnit(AbstractLogMessage logMsg, WebCertUser user) {
        logMsg.setUserId(user.getHsaId());
        logMsg.setUserName(user.getNamn());

        SelectableVardenhet valdVardenhet = user.getValdVardenhet();
        String enhetsId = valdVardenhet.getId();
        String enhetsNamn = valdVardenhet.getNamn();

        SelectableVardenhet valdVardgivare = user.getValdVardgivare();
        String vardgivareId = valdVardgivare.getId();
        String vardgivareNamn = valdVardgivare.getNamn();

        Enhet vardenhet = new Enhet(enhetsId, enhetsNamn, vardgivareId, vardgivareNamn);
        logMsg.setUserCareUnit(vardenhet);
    }

    private void send(AbstractLogMessage logMsg) {

        if (jmsTemplate == null) {
            LOGGER.warn("Can not log {} of Intyg '{}' since PDL logging is disabled!", logMsg.getActivityType(), logMsg.getActivityLevel());
            return;
        }

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
