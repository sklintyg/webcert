package se.inera.intyg.webcert.logsender.converter;

import se.inera.intyg.common.logmessages.AbstractLogMessage;
import se.riv.ehr.log.v1.LogType;

import java.util.List;

/**
 * Created by eriklupander on 2016-02-29.
 */
public interface LogTypeFactory {
    LogType convertFromList(List<AbstractLogMessage> sources);

    LogType convert(AbstractLogMessage source);
}
