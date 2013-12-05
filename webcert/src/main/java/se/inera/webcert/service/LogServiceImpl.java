package se.inera.webcert.service;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.log.messages.IntygReadMessage;
import se.inera.webcert.hsa.model.WebCertUser;
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
    public void logReadOfIntyg(GetCertificateForCareResponseType intyg) {
        if (jmsTemplate != null) {
            IntygReadMessage logthis = new IntygReadMessage();
            WebCertUser user = webCertUserService.getWebCertUser();
            logthis.setUserId(user.getHsaId());
            logthis.setEnhetId(intyg.getCertificate().getSkapadAv().getEnhet().getEnhetsId().getExtension());

            logthis.setVardgivareId(intyg.getCertificate().getSkapadAv().getEnhet().getVardgivare().getVardgivareId()
                    .getExtension());
            logthis.setTimestamp(LocalDateTime.now());

            logthis.setSystemId(systemId);

            jmsTemplate.send(new MC(logthis));
        }
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
