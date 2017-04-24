package se.inera.intyg.webcert.web.service.log;

import se.inera.intyg.infra.logmessages.PdlLogMessage;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;

/**
 * Created by eriklupander on 2017-04-24.
 */
public interface LogMessagePopulator {
    PdlLogMessage populateLogMessage(LogRequest logRequest, PdlLogMessage logMsg, LogUser user);
}
