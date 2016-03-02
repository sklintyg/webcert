package se.inera.intyg.webcert.logsender.helper;

import se.inera.intyg.common.logmessages.AbstractLogMessage;
import se.inera.intyg.common.logmessages.ActivityPurpose;
import se.inera.intyg.common.logmessages.ActivityType;
import se.inera.intyg.common.logmessages.Enhet;
import se.inera.intyg.common.logmessages.Patient;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

import java.util.ArrayList;

/**
 * Utility for creating test data for unit- and integration tests.
 *
 * Created by eriklupander on 2016-03-01.
 */
public class TestDataHelper {

    public static ArrayList<AbstractLogMessage> buildAbstractLogMessageList(ActivityType activityType) {
        AbstractLogMessage abstractLogMessage = new AbstractLogMessage(activityType, ActivityPurpose.CARE_TREATMENT, "Intyg");
        abstractLogMessage.setSystemId("webcert");
        abstractLogMessage.setSystemName("webcert");
        abstractLogMessage.setUserCareUnit(buildEnhet());
        abstractLogMessage.setPatient(buildPatient());
        abstractLogMessage.setResourceOwner(buildEnhet());
        ArrayList<AbstractLogMessage> arrayList = new ArrayList<>();
        arrayList.add(abstractLogMessage);
        return arrayList;
    }

    private static Patient buildPatient() {
        Personnummer pnr = new Personnummer("19121212-1212");
        Patient patient = new Patient(pnr, "Tolvan Tolvansson");
        return patient;
    }

    private static Enhet buildEnhet() {
        Enhet enhet = new Enhet("enhet-1", "Enhet nr 1", "vardgivare-1" ,"Vårdgivare 1");
        return enhet;
    }
}
