/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.infra.logmessages.PdlLogMessage;
import se.inera.intyg.webcert.common.service.log.template.*;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * Implementation of service for logging user actions according to PDL requirements.
 *
 * @author nikpet
 */
@Service
public class LogServiceImpl implements LogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogServiceImpl.class);

    private static final String PRINTED_AS_PDF = "Intyg utskrivet";
    private static final String PRINTED_AS_DRAFT = "Utkastet utskrivet";
    private static final String PRINTED_WHEN_REVOKED = "Makulerat intyg utskrivet";
    private static final String SHOW_PREDICTION = "Prediktion från SRS av risk för lång sjukskrivning";
    private static final String SET_OWN_OPINION = "Läkarens egen bedömning";

    @Autowired(required = false)
    @Qualifier("jmsPDLLogTemplate")
    private JmsTemplate jmsTemplate;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private LogMessagePopulator logMessagePopulator;

    @Autowired
    private LogRequestFactory logRequestFactory;

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
        send(logMessagePopulator.populateLogMessage(
            IntygCreateMessage.build(logRequest.getIntygId()), logRequest, user),
            logRequest.isTestIntyg());
    }

    @Override
    public void logUpdateIntyg(LogRequest logRequest) {
        logUpdateIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logUpdateIntyg(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(
            IntygUpdateMessage.build(logRequest.getIntygId()), logRequest, user),
            logRequest.isTestIntyg());
    }

    @Override
    public void logReadIntyg(LogRequest logRequest) {
        logReadIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logReadIntyg(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(
            IntygReadMessage.build(logRequest.getIntygId()), logRequest, user),
            logRequest.isTestIntyg());
    }

    @Override
    public void logDeleteIntyg(LogRequest logRequest) {
        logDeleteIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logDeleteIntyg(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(
            IntygDeleteMessage.build(logRequest.getIntygId()), logRequest, user),
            logRequest.isTestIntyg());
    }

    @Override
    public void logSignIntyg(LogRequest logRequest) {
        logSignIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logSignIntyg(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(
            IntygSignMessage.build(logRequest.getIntygId()), logRequest, user),
            logRequest.isTestIntyg());
    }

    @Override
    public void logRevokeIntyg(LogRequest logRequest) {
        logRevokeIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logRevokeIntyg(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(
            IntygRevokeMessage.build(logRequest.getIntygId()), logRequest, user),
            logRequest.isTestIntyg());
    }

    @Override
    public void logPrintIntygAsPDF(LogRequest logRequest) {
        logPrintIntygAsPDF(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logPrintIntygAsPDF(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(
            IntygPrintMessage.build(logRequest.getIntygId(), PRINTED_AS_PDF), logRequest, user),
            logRequest.isTestIntyg());
    }

    @Override
    public void logPrintIntygAsDraft(LogRequest logRequest) {
        logPrintIntygAsDraft(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logPrintIntygAsDraft(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(
            IntygPrintMessage.build(logRequest.getIntygId(), PRINTED_AS_DRAFT), logRequest, user),
            logRequest.isTestIntyg());
    }

    @Override
    public void logPrintRevokedIntygAsPDF(LogRequest logRequest) {
        logPrintRevokedIntygAsPDF(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logPrintRevokedIntygAsPDF(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(
            IntygPrintMessage.build(logRequest.getIntygId(), PRINTED_WHEN_REVOKED), logRequest, user),
            logRequest.isTestIntyg());
    }

    @Override
    public void logSendIntygToRecipient(LogRequest logRequest) {
        logSendIntygToRecipient(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logSendIntygToRecipient(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(
            IntygSendMessage.build(logRequest.getIntygId(), logRequest.getAdditionalInfo()), logRequest, user),
            logRequest.isTestIntyg());
    }

    @Override
    public void logShowPrediction(String patientId, String intygId) {
        LogRequest logReq = logRequestFactory.createLogRequestFromUser(webCertUserService.getUser(), patientId);
        logReq.setIntygId(intygId);
        logShowPrediction(logReq, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logShowPrediction(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(
            IntygPredictionMessage.build(logRequest.getIntygId(), SHOW_PREDICTION, ActivityType.READ), logRequest, user),
            logRequest.isTestIntyg());
    }

    @Override
    public void logSetOwnOpinion(String patientId, String intygId) {
        LogRequest logRequest = logRequestFactory.createLogRequestFromUser(webCertUserService.getUser(), patientId);
        logRequest.setIntygId(intygId);
        logSetOwnOpinion(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logSetOwnOpinion(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(
            IntygPredictionMessage.build(logRequest.getIntygId(), SET_OWN_OPINION, ActivityType.CREATE), logRequest, user),
            logRequest.isTestIntyg());
    }

    @Override
    public void logListIntyg(WebCertUser user, String patient) {
        LogRequest logRequest = logRequestFactory.createLogRequestFromUser(user, patient);
        send(logMessagePopulator.populateLogMessage(
            IntygListsMessage.build(), logRequest, getLogUser(user)), logRequest.isTestIntyg());
    }

    @Override
    public LogUser getLogUser(WebCertUser webCertUser) {
        SelectableVardenhet valdVardenhet = webCertUser.getValdVardenhet();
        SelectableVardenhet valdVardgivare = webCertUser.getValdVardgivare();

        return new LogUser.Builder(webCertUser.getHsaId(), valdVardenhet.getId(), valdVardgivare.getId())
            .userName(webCertUser.getNamn())
            .userAssignment(webCertUser.getSelectedMedarbetarUppdragNamn())
            .userTitle(webCertUser.getTitel())
            .enhetsNamn(valdVardenhet.getNamn())
            .vardgivareNamn(valdVardgivare.getNamn())
            .build();
    }

    @Override
    public void logCreateMessage(WebCertUser user, Arende message) {

        LogRequest logRequest = logRequestFactory.createLogRequestFromUser(user, message.getPatientPersonId());
        send(logMessagePopulator.populateLogMessage(
            IntygCreateMessage.build(message.getIntygsId()), logRequest, getLogUser(user)), logRequest.isTestIntyg());
    }

    private void send(PdlLogMessage logMsg, boolean isTestIntyg) {
        if (isTestIntyg) {
            LOGGER.info("Can not log {} of Intyg '{}' since it is a test intyg or related to patient with testIndicator",
                logMsg.getActivityType(), logMsg.getActivityLevel());
            return;
        }

        if (jmsTemplate == null) {
            LOGGER.warn("Can not log {} of Intyg '{}' since PDL logging is disabled!", logMsg.getActivityType(), logMsg.getActivityLevel());
            return;
        }

        LOGGER.debug("Logging {} ({}) of Intyg {}", logMsg.getActivityType(), logMsg.getActivityArgs(), logMsg.getActivityLevel());

        try {
            jmsTemplate.send(new MC(logMsg));
        } catch (JmsException e) {
            LOGGER.error("Could not log {} of Intyg '{}'", logMsg.getActivityType(), logMsg.getActivityLevel(), e);
            throw e;
        }
    }

    @VisibleForTesting
    void setLogMessagePopulator(LogMessagePopulator logMessagePopulator) {
        this.logMessagePopulator = logMessagePopulator;
    }

    private static final class MC implements MessageCreator {

        private final PdlLogMessage logMsg;

        private final ObjectMapper objectMapper = new CustomObjectMapper();

        private MC(PdlLogMessage log) {
            this.logMsg = log;
        }

        @Override
        public Message createMessage(Session session) throws JMSException {
            try {
                return session.createTextMessage(objectMapper.writeValueAsString(logMsg));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Could not serialize log message of type '" + logMsg.getClass().getName()
                    + "' into JSON, message: " + e.getMessage());
            }
        }
    }
}
