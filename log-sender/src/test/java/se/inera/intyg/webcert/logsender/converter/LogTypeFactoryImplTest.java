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
package se.inera.intyg.webcert.logsender.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.infra.logmessages.Enhet;
import se.inera.intyg.infra.logmessages.PdlLogMessage;
import se.inera.intyg.webcert.logsender.helper.TestDataHelper;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.ResourceType;

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
        pdlLogMessage.setActivityArgs("");
        LogType logType = testee.convert(pdlLogMessage);
        assertEquals(logType.getActivity().getActivityType(), pdlLogMessage.getActivityType().getType());
        assertNull(logType.getActivity().getActivityArgs());
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

        assertEquals(resourceType.getPatient().getPatientId(), pdlLogMessage.getPdlResourceList().get(0).getPatient().getPatientId());
        assertEquals(resourceType.getPatient().getPatientName(), pdlLogMessage.getPdlResourceList().get(0).getPatient().getPatientNamn());
        assertEquals(resourceType.getResourceType(), pdlLogMessage.getPdlResourceList().get(0).getResourceType());

        assertEquals(resourceType.getCareUnit().getCareUnitId(), pdlLogMessage.getPdlResourceList().get(0).getResourceOwner().getEnhetsId());
        assertEquals(resourceType.getCareUnit().getCareUnitName(), pdlLogMessage.getPdlResourceList().get(0).getResourceOwner().getEnhetsNamn());

        assertEquals(resourceType.getCareProvider().getCareProviderId(), pdlLogMessage.getPdlResourceList().get(0).getResourceOwner().getVardgivareId());
        assertEquals(resourceType.getCareProvider().getCareProviderName(), pdlLogMessage.getPdlResourceList().get(0).getResourceOwner().getVardgivareNamn());
    }

    @Test
    public void testConvertWithActivityArgs() {
        PdlLogMessage pdlLogMessage = TestDataHelper.buildBasePdlLogMessage(ActivityType.READ);
        pdlLogMessage.setActivityArgs("activityArgs");
        LogType logType = testee.convert(pdlLogMessage);
        assertEquals(logType.getActivity().getActivityType(), pdlLogMessage.getActivityType().getType());
        assertEquals(logType.getActivity().getActivityArgs(), pdlLogMessage.getActivityArgs());
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

        assertEquals(resourceType.getPatient().getPatientId(), pdlLogMessage.getPdlResourceList().get(0).getPatient().getPatientId());
        assertEquals(resourceType.getPatient().getPatientName(), pdlLogMessage.getPdlResourceList().get(0).getPatient().getPatientNamn());
        assertEquals(resourceType.getResourceType(), pdlLogMessage.getPdlResourceList().get(0).getResourceType());

        assertEquals(resourceType.getCareUnit().getCareUnitId(), pdlLogMessage.getPdlResourceList().get(0).getResourceOwner().getEnhetsId());
        assertEquals(resourceType.getCareUnit().getCareUnitName(), pdlLogMessage.getPdlResourceList().get(0).getResourceOwner().getEnhetsNamn());

        assertEquals(resourceType.getCareProvider().getCareProviderId(), pdlLogMessage.getPdlResourceList().get(0).getResourceOwner().getVardgivareId());
        assertEquals(resourceType.getCareProvider().getCareProviderName(), pdlLogMessage.getPdlResourceList().get(0).getResourceOwner().getVardgivareNamn());
    }

    @Test
    public void testLeadingAndTrailingWhitespacesAreTrimmed() {
        PdlLogMessage pdlLogMessage = TestDataHelper.buildBasePdlLogMessage(ActivityType.READ);
        Enhet enhet = new Enhet(" enhet-1", " enhets namn ", "vardgivare-1 ", "Vardgivare namn ");
        pdlLogMessage.setUserCareUnit(enhet);

        LogType logType = testee.convert(pdlLogMessage);
        assertEquals("enhet-1", logType.getUser().getCareUnit().getCareUnitId());
        assertEquals("enhets namn", logType.getUser().getCareUnit().getCareUnitName());

        assertEquals("vardgivare-1", logType.getUser().getCareProvider().getCareProviderId());
        assertEquals("Vardgivare namn", logType.getUser().getCareProvider().getCareProviderName());
    }
}
