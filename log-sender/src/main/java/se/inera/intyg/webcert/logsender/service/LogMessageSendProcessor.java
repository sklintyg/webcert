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

package se.inera.intyg.webcert.logsender.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.logmessages.PdlLogMessage;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.logsender.client.LogSenderClient;
import se.inera.intyg.webcert.logsender.converter.LogTypeFactory;
import se.inera.intyg.webcert.logsender.exception.BatchValidationException;
import se.inera.intyg.webcert.logsender.exception.LoggtjanstExecutionException;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogResponseType;
import se.riv.ehr.log.store.v1.ResultType;
import se.riv.ehr.log.v1.LogType;

/**
 * Created by eriklupander on 2015-05-21.
 */
public class LogMessageSendProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(LogMessageSendProcessor.class);

    @Autowired
    private LogSenderClient logSenderClient;

    @Autowired
    private LogTypeFactory logTypeFactory;

    private ObjectMapper objectMapper = new CustomObjectMapper();

    /**
     * Note use of Camel "boxing" of the body.
     *
     * The body must be a String with a JSON encoded array of {@link PdlLogMessage}(s)
     *
     * @param groupedLogEntries
     *      A String containing a JSON encoded array of {@link PdlLogMessage}(s)
     * @throws IOException
     * @throws BatchValidationException
     * @throws TemporaryException
     */
    public void process(String groupedLogEntries) throws IOException, BatchValidationException, TemporaryException {

        try {

            List<String> groupedList = objectMapper.readValue(groupedLogEntries, List.class);

            List<LogType> logMessages = groupedList.stream()
                    .map(this::jsonToPdlLogMessage)
                    .map(alm -> logTypeFactory.convert(alm))
                    .collect(Collectors.toList());

            StoreLogResponseType response = logSenderClient.sendLogMessage(logMessages);

            final ResultType result = response.getResultType();
            final String resultText = result.getResultText();

            switch (result.getResultCode()) {
                case OK:
                    break;
                case ERROR:
                case VALIDATION_ERROR:
                    LOG.error("Loggtjänsten rejected PDL message batch with {}, batch will be moved to DLQ. Result text: '{}'", result.getResultCode().value(), resultText);
                    throw new BatchValidationException("Loggtjänsten rejected PDL message batch with error: " + resultText + ". Batch will be moved directly to DLQ.");
                case INFO:
                    LOG.warn("Warning of type INFO occured when sending PDL log message batch: '{}'. Will not requeue.", resultText);
                    break;
                default:
                    throw new TemporaryException(resultText);
            }

        } catch (IllegalArgumentException e) {
            LOG.error(e.getMessage() + ". Moving batch to DLQ.");
            throw new BatchValidationException("Unparsable Log message: " + e.getMessage());
        } catch (LoggtjanstExecutionException e) {
            LOG.warn("Call to send log message caused a LoggtjanstExecutionException: {}. Will retry", e.getMessage());
            throw new TemporaryException(e.getMessage());
        } catch (WebServiceException e) {
            LOG.warn("Call to send log message caused an error: {}. Will retry", e.getMessage());
            throw new TemporaryException(e.getMessage());
        }
    }

    private PdlLogMessage jsonToPdlLogMessage(String body) {
        try {
            return objectMapper.readValue(body, PdlLogMessage.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not parse PdlLogMessage from log message JSON: " + e.getMessage());
        }
    }
}
