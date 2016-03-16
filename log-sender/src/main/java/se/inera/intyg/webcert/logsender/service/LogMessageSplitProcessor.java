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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Body;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import se.inera.intyg.common.logmessages.PdlLogMessage;
import se.inera.intyg.common.logmessages.PdlResource;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;

import java.util.ArrayList;
import java.util.List;

/**
 * This camel processor implements the split pattern. It will create a new Message for each
 * {@link PdlResource} within the deserialized {@link PdlLogMessage}.
 *
 * The reason for this is that the PDL-service does not accept multiple PdlResources for different patients bound to the
 * same PdlLogMessage.
 *
 * Created by eriklupander on 2016-03-16.
 */
public class LogMessageSplitProcessor {

    private ObjectMapper objectMapper = new CustomObjectMapper();

    /**
     * If a PdlLogMessage contains more than one resource, it is split into (n resources) number of new PdlLogMessages
     * with one Resource each.
     *
     * @param body
     *      The inbound message, typically containing a JSON-serialized {@link PdlLogMessage} as body.
     * @return
     *      A list of {@link DefaultMessage} where each message contains a body of one {@link PdlLogMessage} having
     *      exactly one {@link PdlResource}.
     * @throws Exception
     */
    public List<Message> process(@Body Message body) throws Exception {

        List<Message> answer = new ArrayList<>();

        if (body != null) {
            PdlLogMessage pdlLogMessage = objectMapper.readValue((String) body.getBody(), PdlLogMessage.class);

            if (pdlLogMessage.getPdlResourceList().size() == 0) {
                throw new PermanentException("No resources in PDL log message, don't proceed.");
            }

            if (pdlLogMessage.getPdlResourceList().size() == 1) {
                answer.add(body);
            } else {

                for (PdlResource resource : pdlLogMessage.getPdlResourceList()) {
                    PdlLogMessage copiedPdlLogMsg = pdlLogMessage.copy(false);
                    copiedPdlLogMsg.getPdlResourceList().add(resource);

                    DefaultMessage message = new DefaultMessage();
                    message.setBody(objectMapper.writeValueAsString(copiedPdlLogMsg));
                    answer.add(message);
                }
            }
        }
        return answer;
    }
}
