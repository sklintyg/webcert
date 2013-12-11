package se.inera.webcert.service;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.EnhetType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.PatientType;
import se.inera.log.messages.Enhet;
import se.inera.log.messages.IntygReadMessage;
import se.inera.log.messages.Patient;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * @author andreaskaltenbach
 */
@Service
public class LogServiceImpl implements LogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogServiceImpl.class);

    @Autowired( required = false )
    JmsTemplate jmsTemplate;

    @Value( "${pdlLogging.systemId}" )
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
    public void logReadOfIntyg(GetCertificateForCareResponseType intyg) {
        if (jmsTemplate != null) {
            IntygReadMessage logthis = new IntygReadMessage();
            WebCertUser user = webCertUserService.getWebCertUser();
            logthis.setUserId(user.getHsaId());
            logthis.setUserName(user.getNamn());

            logthis.setEnhet(enhet(intyg.getCertificate().getSkapadAv().getEnhet()));
            logthis.setPatient(patient(intyg.getCertificate().getPatient()));

            logthis.setTimestamp(LocalDateTime.now());

            logthis.setSystemId(systemId);

            jmsTemplate.send(new MC(logthis));
        }
    }

    private Patient patient(PatientType source) {
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

    private Enhet enhet(EnhetType source) {
        return new Enhet(source.getEnhetsId().getExtension(), source.getEnhetsnamn(),
                source.getVardgivare().getVardgivareId().getExtension(), source.getVardgivare().getVardgivarnamn());
    }

    private static final class MC implements MessageCreator {
        private final IntygReadMessage logthis;

        public MC(IntygReadMessage log) {
            this.logthis = log;
        }

        public Message createMessage(Session session) throws JMSException {
            return session.createObjectMessage(logthis);
        }
    }
}
