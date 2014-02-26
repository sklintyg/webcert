package se.inera.webcert.service.log;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.PatientType;
import se.inera.log.messages.AbstractLogMessage;
import se.inera.log.messages.Enhet;
import se.inera.log.messages.IntygCreateMessage;
import se.inera.log.messages.IntygPrintMessage;
import se.inera.log.messages.IntygReadMessage;
import se.inera.log.messages.Patient;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * @author andreaskaltenbach
 */
@Service
public class LogServiceImpl implements LogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogServiceImpl.class);

    @Autowired(required = false)
    JmsTemplate jmsTemplate;

    @Value("${pdlLogging.systemId}")
    String systemId;

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
        
        IntygReadMessage logMsg = new IntygReadMessage(intygId);
        populateLogMessage(patientId, logMsg);
        send(logMsg);
    }

    @Override
    public void logPrintOfIntyg(String intygId, String patientId) {
        IntygPrintMessage logMsg = new IntygPrintMessage(intygId);
        populateLogMessage(patientId, logMsg);
        send(logMsg);
    }

    @Override
    public void logCreateOfIntyg(LogRequest logRequest) {
        IntygCreateMessage logMsg = new IntygCreateMessage(logRequest.getIntygId());
        populateLogMessage(logRequest, logMsg);
        send(logMsg);
    }
    
    private void populateLogMessage(LogRequest logRequest, AbstractLogMessage logMsg) {
        
        populateWithCurrentUser(logMsg);

        populateWithVardgivareAndVardenhet(logMsg);
        
        Patient patient = new Patient(logRequest.getPatientId(), logRequest.getPatientName());
        logMsg.setPatient(patient);

        logMsg.setSystemId(systemId);
    }

    private void populateLogMessage(String patientId, AbstractLogMessage logMsg) {
        
        populateWithCurrentUser(logMsg);

        populateWithVardgivareAndVardenhet(logMsg);
        
        Patient patient = new Patient(patientId);
        logMsg.setPatient(patient);

        logMsg.setSystemId(systemId);
    }
    
    private void populateWithCurrentUser(AbstractLogMessage logMsg) {
        WebCertUser user = webCertUserService.getWebCertUser();
        logMsg.setUserId(user.getHsaId());
        logMsg.setUserName(user.getNamn());

        user.getVardgivare();
    }

    private void populateWithVardgivareAndVardenhet(AbstractLogMessage logMsg) {

        WebCertUser user = webCertUserService.getWebCertUser();

        List<Vardgivare> allVardgivare = user.getVardgivare();

        // TODO: Here we use the first of the Vardgivare that the user have.
        Vardgivare vardgivare = (allVardgivare.isEmpty()) ? null : allVardgivare.get(0);

        if (vardgivare == null) {
            LOGGER.error("Can not populate log message, vardgivare is null for user: {}", user.getAsJson());
            return;
        }

        String vardgivareId = vardgivare.getId();
        String vardgivareNamn = vardgivare.getNamn();

        List<Vardenhet> allVardenheterForVardgivare = vardgivare.getVardenheter();

        Vardenhet enhet = (allVardenheterForVardgivare.isEmpty()) ? null : allVardenheterForVardgivare.get(0);

        if (enhet == null) {
            LOGGER.error("Can not populate log message, vardenhet is null for user: {}", user.getAsJson());
            return;
        }

        String enhetsId = enhet.getId();
        String enhetsNamn = enhet.getNamn();

        logMsg.setEnhet(new Enhet(enhetsId, enhetsNamn, vardgivareId, vardgivareNamn));
    }

    private void send(AbstractLogMessage logMsg) {
        
        if (jmsTemplate == null) {
            LOGGER.warn("Can not log {} of Intyg '{}' since PDL logging is disabled!", logMsg.getActivityType(), logMsg.getActivityLevel());
            return;
        }
        
        LOGGER.debug("Logging {} of Intyg {}", logMsg.getActivityType(), logMsg.getActivityLevel());
        
        jmsTemplate.send(new MC(logMsg));
    }
    
    private Patient fetchPatientFromIntyg(PatientType source) {
        if (source == null) {
            return null;
        }
        return new Patient(source.getPersonId().getExtension(), patientName(source));
    }

    private String patientName(PatientType source) {
        List<String> names = new ArrayList<>();
        names.addAll(source.getFornamn());
        names.addAll(source.getMellannamn());
        names.add(source.getEfternamn());
        return StringUtils.join(names, " ");
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
