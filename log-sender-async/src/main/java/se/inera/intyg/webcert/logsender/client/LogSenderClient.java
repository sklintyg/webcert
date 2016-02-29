package se.inera.intyg.webcert.logsender.client;

import se.riv.ehr.log.store.storelogresponder.v1.StoreLogResponseType;
import se.riv.ehr.log.v1.LogType;

import java.util.List;

/**
 * Created by eriklupander on 2016-02-29.
 */
public interface LogSenderClient {

    StoreLogResponseType sendLogMessage(List<LogType> logEntries);
}
