package se.inera.intyg.webcert.web.service.log;

import org.junit.Test;
import se.inera.intyg.infra.logmessages.PdlLogMessage;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;

import static org.junit.Assert.assertEquals;

/**
 * Created by eriklupander on 2017-04-24.
 */
public class LogMessagePopulatorImplTest {

    private static final String ADDITIONAL_INFO = "additional info";
    private static final String ACTIVITY_ARGS = "activity args";
    private LogMessagePopulator testee = new LogMessagePopulatorImpl();

    @Test
    public void testActivityArgsAddedFromAdditionalInfoWhenActivityArgsAbsent() {
        PdlLogMessage logMessage = testee.populateLogMessage(buildLogRequest(ADDITIONAL_INFO), buildPdlLogMessage(""), buildLogUser());
        assertEquals(ADDITIONAL_INFO, logMessage.getActivityArgs());
    }

    @Test
    public void testActivityArgsAppendedFromAdditionalInfoWhenActivityArgsExists() {
        PdlLogMessage logMessage = testee.populateLogMessage(buildLogRequest(ADDITIONAL_INFO), buildPdlLogMessage(ACTIVITY_ARGS),
                buildLogUser());
        assertEquals(ACTIVITY_ARGS + "\n" + ADDITIONAL_INFO, logMessage.getActivityArgs());
    }

    @Test
    public void testActivityArgsUntouchedWhenActivityArgsExistsButNoAdditionalInfo() {
        PdlLogMessage logMessage = testee.populateLogMessage(buildLogRequest(""), buildPdlLogMessage(ACTIVITY_ARGS), buildLogUser());
        assertEquals(ACTIVITY_ARGS, logMessage.getActivityArgs());
    }

    @Test
    public void testActivityArgsUntouchedWhenActivityArgsIsEqualToAdditionalInfo() {
        PdlLogMessage logMessage = testee.populateLogMessage(buildLogRequest(ACTIVITY_ARGS), buildPdlLogMessage(ACTIVITY_ARGS),
                buildLogUser());
        assertEquals(ACTIVITY_ARGS, logMessage.getActivityArgs());
    }

    private PdlLogMessage buildPdlLogMessage(String activityArgs) {
        PdlLogMessage pdlLogMessage = new PdlLogMessage();
        pdlLogMessage.setActivityArgs(activityArgs);
        return pdlLogMessage;
    }

    private LogRequest buildLogRequest(String additionalInfo) {
        LogRequest logRequest = new LogRequest();
        logRequest.setPatientId(new Personnummer("19121212-1212"));
        logRequest.setAdditionalInfo(additionalInfo);
        return logRequest;
    }

    private LogUser buildLogUser() {
        return new LogUser.Builder("userId", "ve-1", "vg-1").build();
    }
}
