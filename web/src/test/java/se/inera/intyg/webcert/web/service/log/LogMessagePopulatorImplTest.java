/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
        assertEquals(ACTIVITY_ARGS + ". " + ADDITIONAL_INFO, logMessage.getActivityArgs());
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
        logRequest.setPatientId(Personnummer.createPersonnummer("19121212-1212").get());
        logRequest.setAdditionalInfo(additionalInfo);
        return logRequest;
    }

    private LogUser buildLogUser() {
        return new LogUser.Builder("userId", "ve-1", "vg-1").build();
    }
}
