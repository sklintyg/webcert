/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.infra.logmessages.PdlLogMessage;
import se.inera.intyg.webcert.common.service.log.template.IntygCreateMessage;
import se.inera.intyg.webcert.common.service.log.template.IntygDeleteMessage;
import se.inera.intyg.webcert.common.service.log.template.IntygPredictionMessage;
import se.inera.intyg.webcert.common.service.log.template.IntygPrintMessage;
import se.inera.intyg.webcert.common.service.log.template.IntygReadMessage;
import se.inera.intyg.webcert.common.service.log.template.IntygRevokeMessage;
import se.inera.intyg.webcert.common.service.log.template.IntygSendMessage;
import se.inera.intyg.webcert.common.service.log.template.IntygSignMessage;
import se.inera.intyg.webcert.common.service.log.template.IntygUpdateMessage;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
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

    @Autowired(required = false)
    @Qualifier("jmsPDLLogTemplate")
    private JmsTemplate jmsTemplate;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private LogMessagePopulator logMessagePopulator;

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
        send(logMessagePopulator.populateLogMessage(logRequest, IntygCreateMessage.build(logRequest.getIntygId()), user));
    }

    @Override
    public void logUpdateIntyg(LogRequest logRequest) {
        logUpdateIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logUpdateIntyg(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(logRequest, IntygUpdateMessage.build(logRequest.getIntygId()), user));
    }

    @Override
    public void logReadIntyg(LogRequest logRequest) {
        logReadIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logReadIntyg(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(logRequest, IntygReadMessage.build(logRequest.getIntygId()), user));
    }

    @Override
    public void logDeleteIntyg(LogRequest logRequest) {
        logDeleteIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logDeleteIntyg(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(logRequest, IntygDeleteMessage.build(logRequest.getIntygId()), user));
    }

    @Override
    public void logSignIntyg(LogRequest logRequest) {
        logSignIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logSignIntyg(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(logRequest, IntygSignMessage.build(logRequest.getIntygId()), user));
    }

    @Override
    public void logRevokeIntyg(LogRequest logRequest) {
        logRevokeIntyg(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logRevokeIntyg(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(logRequest, IntygRevokeMessage.build(logRequest.getIntygId()), user));
    }

    @Override
    public void logPrintIntygAsPDF(LogRequest logRequest) {
        logPrintIntygAsPDF(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logPrintIntygAsPDF(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(logRequest, IntygPrintMessage.build(logRequest.getIntygId(), PRINTED_AS_PDF), user));
    }

    @Override
    public void logPrintIntygAsDraft(LogRequest logRequest) {
        logPrintIntygAsDraft(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logPrintIntygAsDraft(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(logRequest, IntygPrintMessage.build(logRequest.getIntygId(), PRINTED_AS_DRAFT), user));
    }

    @Override
    public void logPrintRevokedIntygAsPDF(LogRequest logRequest) {
        logPrintRevokedIntygAsPDF(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logPrintRevokedIntygAsPDF(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(logRequest, IntygPrintMessage.build(logRequest.getIntygId(), PRINTED_WHEN_REVOKED),
                user));
    }

    @Override
    public void logSendIntygToRecipient(LogRequest logRequest) {
        logSendIntygToRecipient(logRequest, getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logSendIntygToRecipient(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(logRequest,
                IntygSendMessage.build(logRequest.getIntygId(), logRequest.getAdditionalInfo()), user));
    }

    @Override
    public void logShowPrediction(String patientId) {
        logShowPrediction(LogRequestFactory.createLogRequestFromUser(webCertUserService.getUser(), patientId),
                getLogUser(webCertUserService.getUser()));
    }

    @Override
    public void logShowPrediction(LogRequest logRequest, LogUser user) {
        send(logMessagePopulator.populateLogMessage(logRequest, IntygPredictionMessage.build(SHOW_PREDICTION), user));
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

    private void send(PdlLogMessage logMsg) {

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

        private ObjectMapper objectMapper = new CustomObjectMapper();

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
