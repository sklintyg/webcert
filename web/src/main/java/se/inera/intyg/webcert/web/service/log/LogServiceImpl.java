/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.web.service.log;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;
import javax.jms.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.common.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.common.logmessages.*;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.common.service.log.template.*;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

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
        send(populateLogMessage(logRequest, IntygCreateMessage.build(logRequest.getIntygId()), user));
    }

    @Override
    public void logUpdateIntyg(LogRequest logRequest) {
        logUpdateIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logUpdateIntyg(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, IntygUpdateMessage.build(logRequest.getIntygId()), user));
    }

    @Override
    public void logReadIntyg(LogRequest logRequest) {
        logReadIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logReadIntyg(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, IntygReadMessage.build(logRequest.getIntygId()), user));
    }

    @Override
    public void logDeleteIntyg(LogRequest logRequest) {
        logDeleteIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logDeleteIntyg(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, IntygDeleteMessage.build(logRequest.getIntygId()), user));
    }

    @Override
    public void logSignIntyg(LogRequest logRequest) {
        logSignIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logSignIntyg(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, IntygSignMessage.build(logRequest.getIntygId()), user));
    }

    @Override
    public void logRevokeIntyg(LogRequest logRequest) {
        logRevokeIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logRevokeIntyg(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, IntygRevokeMessage.build(logRequest.getIntygId()), user));
    }

    @Override
    public void logPrintIntygAsPDF(LogRequest logRequest) {
        logPrintIntygAsPDF(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logPrintIntygAsPDF(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, IntygPrintMessage.build(logRequest.getIntygId(), PRINTED_AS_PDF), user));
    }

    @Override
    public void logPrintIntygAsDraft(LogRequest logRequest) {
        logPrintIntygAsDraft(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logPrintIntygAsDraft(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, IntygPrintMessage.build(logRequest.getIntygId(), PRINTED_AS_DRAFT), user));
    }

    @Override
    public void logSendIntygToRecipient(LogRequest logRequest) {
        logSendIntygToRecipient(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logSendIntygToRecipient(LogRequest logRequest, LogUser user) {
        send(populateLogMessage(logRequest, IntygSendMessage.build(logRequest.getIntygId(), logRequest.getAdditionalInfo()), user));
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


    private PdlLogMessage populateLogMessage(LogRequest logRequest, PdlLogMessage logMsg, LogUser user) {

        populateWithCurrentUserAndCareUnit(logMsg, user);

        String careUnitId = logRequest.getIntygCareUnitId();
        String careUnitName = logRequest.getIntygCareUnitName();

        String careGiverId = logRequest.getIntygCareGiverId();
        String careGiverName = logRequest.getIntygCareGiverName();

        Patient patient = new Patient(logRequest.getPatientId(), logRequest.getPatientName());
        Enhet resourceOwner = new Enhet(careUnitId, careUnitName, careGiverId, careGiverName);

        PdlResource pdlResource = new PdlResource();
        pdlResource.setPatient(patient);
        pdlResource.setResourceOwner(resourceOwner);
        pdlResource.setResourceType(ResourceType.RESOURCE_TYPE_INTYG.getResourceTypeName());

        logMsg.getPdlResourceList().add(pdlResource);

        logMsg.setSystemId(systemId);
        logMsg.setSystemName(systemName);
        logMsg.setTimestamp(LocalDateTime.now());
        logMsg.setPurpose(ActivityPurpose.CARE_TREATMENT);

        return logMsg;
    }

    private void populateWithCurrentUserAndCareUnit(PdlLogMessage logMsg, LogUser user) {
        logMsg.setUserId(user.getUserId());
        logMsg.setUserName(user.getUserName());

        Enhet vardenhet = new Enhet(user.getEnhetsId(), user.getEnhetsNamn(), user.getVardgivareId(), user.getVardgivareNamn());
        logMsg.setUserCareUnit(vardenhet);
    }

    private void send(PdlLogMessage logMsg) {

        if (jmsTemplate == null) {
            LOGGER.warn("Can not log {} of Intyg '{}' since PDL logging is disabled!", logMsg.getActivityType(), logMsg.getActivityLevel());
            return;
        }

        LOGGER.debug("Logging {} of Intyg {}", logMsg.getActivityType(), logMsg.getActivityLevel());

        try {
            jmsTemplate.send(new MC(logMsg));
        } catch (JmsException e) {
            LOGGER.error("Could not log {} of Intyg '{}'", logMsg.getActivityType(), logMsg.getActivityLevel(), e);
            throw e;
        }
    }

    private static final class MC implements MessageCreator {
        private final PdlLogMessage logMsg;

        private ObjectMapper objectMapper = new CustomObjectMapper();

        private MC(PdlLogMessage log) {
            this.logMsg = log;
        }

        @Override
        public Message createMessage(Session session) throws JMSException {
            try {
                return session.createTextMessage(objectMapper.writeValueAsString(logMsg));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Could not serialize log message of type '" + logMsg.getClass().getName() + "' into JSON, message: " + e.getMessage());
            }
        }
    }

}
