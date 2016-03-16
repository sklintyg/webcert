package se.inera.intyg.webcert.logsender.converter;

import org.junit.Test;
import se.inera.intyg.common.logmessages.ActivityType;
import se.inera.intyg.common.logmessages.PdlLogMessage;
import se.inera.intyg.webcert.logsender.helper.TestDataHelper;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.ResourceType;

import static org.junit.Assert.assertEquals;

/**
 * Tests that {@link PdlLogMessage} is properly converted into a {@link LogType}.
 *
 * Created by eriklupander on 2016-03-08.
 */
public class LogTypeFactoryImplTest {

    private LogTypeFactoryImpl testee = new LogTypeFactoryImpl();

    @Test
    public void testConvertOk() {
        PdlLogMessage pdlLogMessage = TestDataHelper.buildBasePdlLogMessage(ActivityType.READ);
        LogType logType = testee.convert(pdlLogMessage);
        assertEquals(logType.getActivity().getActivityType(), pdlLogMessage.getActivityType().getType());
        assertEquals(logType.getLogId(), pdlLogMessage.getLogId());
        assertEquals(logType.getSystem().getSystemId(), pdlLogMessage.getSystemId());
        assertEquals(logType.getSystem().getSystemName(), pdlLogMessage.getSystemName());
        assertEquals(logType.getUser().getPersonId(), pdlLogMessage.getUserId());
        assertEquals(logType.getUser().getName(), pdlLogMessage.getUserName());

        assertEquals(logType.getUser().getCareUnit().getCareUnitId(), pdlLogMessage.getUserCareUnit().getEnhetsId());
        assertEquals(logType.getUser().getCareUnit().getCareUnitName(), pdlLogMessage.getUserCareUnit().getEnhetsNamn());

        assertEquals(logType.getUser().getCareProvider().getCareProviderId(), pdlLogMessage.getUserCareUnit().getVardgivareId());
        assertEquals(logType.getUser().getCareProvider().getCareProviderName(), pdlLogMessage.getUserCareUnit().getVardgivareNamn());

        assertEquals(1, logType.getResources().getResource().size());
        ResourceType resourceType = logType.getResources().getResource().get(0);

        assertEquals(resourceType.getPatient().getPatientId(), pdlLogMessage.getPdlResourceList().get(0).getPatient().getPatientId().getPersonnummerWithoutDash());
        assertEquals(resourceType.getPatient().getPatientName(), pdlLogMessage.getPdlResourceList().get(0).getPatient().getPatientNamn());
        assertEquals(resourceType.getResourceType(), pdlLogMessage.getPdlResourceList().get(0).getResourceType());

        assertEquals(resourceType.getCareUnit().getCareUnitId(), pdlLogMessage.getPdlResourceList().get(0).getResourceOwner().getEnhetsId());
        assertEquals(resourceType.getCareUnit().getCareUnitName(), pdlLogMessage.getPdlResourceList().get(0).getResourceOwner().getEnhetsNamn());

        assertEquals(resourceType.getCareProvider().getCareProviderId(), pdlLogMessage.getPdlResourceList().get(0).getResourceOwner().getVardgivareId());
        assertEquals(resourceType.getCareProvider().getCareProviderName(), pdlLogMessage.getPdlResourceList().get(0).getResourceOwner().getVardgivareNamn());
    }
}
