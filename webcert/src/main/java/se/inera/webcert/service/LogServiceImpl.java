package se.inera.webcert.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.log.messages.IntygReadMessage;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.web.service.WebCertUserService;
import se.riv.ehr.log.store.storelog.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelog.v1.StoreLogResponderInterface;
import se.riv.ehr.log.v1.ActivityType;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.SystemType;
import se.riv.ehr.log.v1.UserType;

import javax.jms.*;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author andreaskaltenbach
 */
@Service
public class LogServiceImpl implements LogService {

    @Autowired(required = false)
    JmsTemplate jmsTemplate;


    @Autowired
    private WebCertUserService webCertUserService;

    @Override
    public void logReadOfIntyg(String utlatandeId) {
        IntygReadMessage logthis = new IntygReadMessage();
        WebCertUser user =  webCertUserService.getWebCertUser();
        logthis.setUserId(user.getHsaId());
        //logthis.setEnhetId(user.get);
        //logthis.setVardgivareId();

        //logthis.set(user.getNamn());
        //logthis.set(utlatandeId);
        logthis.setTimestamp(LocalDateTime.now());


        jmsTemplate.send(new MC(logthis));
    }

    private static CustomObjectMapper jsonMapper = new CustomObjectMapper();

    private static String asJson(Object object) {
        StringWriter sw = new StringWriter();
        try {
            jsonMapper.writeValue(sw, object) ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sw.toString();
    }

    private static final class MC implements MessageCreator {
        private final IntygReadMessage logthis;

        public MC(IntygReadMessage log) {
                //this.type = type;
            this.logthis = log;
        }

        public Message createMessage(Session session) throws JMSException {
            ObjectMessage message = session.createObjectMessage(logthis);
            //message.setJMSCorrelationID(type + ':' + certificate.getId());
            return message;
        }
    }
}