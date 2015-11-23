package se.inera.intyg.webcert.web.service.log;

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
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

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
    public void logCreateIntyg(LogRequest logRequest) {
        logCreateIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logCreateIntyg(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, new IntygCreateMessage(logRequest.getIntygId()), user));
    }

    @Override
    public void logUpdateIntyg(LogRequest logRequest) {
        logUpdateIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logUpdateIntyg(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, new IntygUpdateMessage(logRequest.getIntygId()), user));
    }

    @Override
    public void logReadIntyg(LogRequest logRequest) {
        logReadIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logReadIntyg(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, new IntygReadMessage(logRequest.getIntygId()), user));
    }

    @Override
    public void logDeleteIntyg(LogRequest logRequest) {
        logDeleteIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logDeleteIntyg(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, new IntygDeleteMessage(logRequest.getIntygId()), user));
    }

    @Override
    public void logSignIntyg(LogRequest logRequest) {
        logSignIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logSignIntyg(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, new IntygSignMessage(logRequest.getIntygId()), user));
    }

    @Override
    public void logRevokeIntyg(LogRequest logRequest) {
        logRevokeIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logRevokeIntyg(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, new IntygRevokeMessage(logRequest.getIntygId()), user));
    }

    @Override
    public void logPrintIntygAsPDF(LogRequest logRequest) {
        logPrintIntygAsPDF(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logPrintIntygAsPDF(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, new IntygPrintMessage(logRequest.getIntygId(), PRINTED_AS_PDF), user));
    }

    @Override
    public void logPrintIntygAsDraft(LogRequest logRequest) {
        logPrintIntygAsDraft(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logPrintIntygAsDraft(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, new IntygPrintMessage(logRequest.getIntygId(), PRINTED_AS_DRAFT), user));
    }

    @Override
    public void logSendIntygToRecipient(LogRequest logRequest) {
        logSendIntygToRecipient(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logSendIntygToRecipient(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, new IntygSendMessage(logRequest.getIntygId(), logRequest.getAdditionalInfo()), user));
    }

    @Override
    public LogUser getLogUser(WebCertUser webCertUser) {

        LogUser logUser = new LogUser();

        logUser.setUserId(webCertUser.getHsaId());
        logUser.setUserName(webCertUser.getNamn());

        SelectableVardenhet valdVardenhet = webCertUser.getValdVardenhet();
        logUser.setEnhetsId(valdVardenhet.getId());
        logUser.setEnhetsNamn(valdVardenhet.getNamn());

        SelectableVardenhet valdVardgivare = webCertUser.getValdVardgivare();
        logUser.setVardgivareId(valdVardgivare.getId());
        logUser.setVardgivareNamn(valdVardgivare.getNamn());

        return logUser;
    }


    private AbstractLogMessage populateLogMessage(LogRequest logRequest, AbstractLogMessage logMsg, LogUser user) {

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

    private void populateWithCurrentUserAndCareUnit(AbstractLogMessage logMsg, LogUser user) {
        logMsg.setUserId(user.getUserId());
        logMsg.setUserName(user.getUserName());

        Enhet vardenhet = new Enhet(user.getEnhetsId(), user.getEnhetsNamn(), user.getVardgivareId(), user.getVardgivareNamn());
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

        private MC(AbstractLogMessage log) {
            this.logMsg = log;
        }

        @Override
        public Message createMessage(Session session) throws JMSException {
            return session.createObjectMessage(logMsg);
        }
    }

}
