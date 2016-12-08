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
package se.inera.intyg.webcert.logsender.helper;

import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.logmessages.*;

/**
 * Utility for creating test data for unit- and integration tests.
 *
 * Created by eriklupander on 2016-03-01.
 */
public class TestDataHelper {

    private static final ObjectMapper objectMapper = new CustomObjectMapper();

    public static String buildBasePdlLogMessageAsJson(ActivityType activityType, int numberOfResources) {
        try {
            return objectMapper.writeValueAsString(buildBasePdlLogMessage(activityType, numberOfResources));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not build test data log message, JSON could not be produced: " + e.getMessage());
        }
    }

    public static String buildBasePdlLogMessageAsJson(ActivityType activityType) {
        try {
            return objectMapper.writeValueAsString(buildBasePdlLogMessage(activityType, 1));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not build test data log message, JSON could not be produced: " + e.getMessage());
        }
    }

    public static PdlLogMessage buildBasePdlLogMessage(ActivityType activityType) {
        return buildBasePdlLogMessage(activityType, 1);
    }

    public static PdlLogMessage buildBasePdlLogMessage(ActivityType activityType, int numberOfResources) {
        PdlLogMessage pdlLogMessage = new PdlLogMessage();

        pdlLogMessage.setSystemId("webcert");
        pdlLogMessage.setSystemName("webcert");
        pdlLogMessage.setUserCareUnit(buildEnhet());
        pdlLogMessage.setActivityType(activityType);
        pdlLogMessage.setTimestamp(LocalDateTime.now());
        pdlLogMessage.setPurpose(ActivityPurpose.CARE_TREATMENT);

        for (int a = 0; a < numberOfResources; a++) {
            PdlResource pdlResource = new PdlResource();
            pdlResource.setPatient(buildPatient());
            pdlResource.setResourceOwner(buildEnhet());
            pdlResource.setResourceType(ResourceType.RESOURCE_TYPE_INTYG.getResourceTypeName());
            pdlLogMessage.getPdlResourceList().add(pdlResource);
        }

        return pdlLogMessage;
    }

    private static Patient buildPatient() {
        Patient patient = new Patient("19121212-1212", "Tolvan Tolvansson");
        return patient;
    }

    private static Enhet buildEnhet() {
        Enhet enhet = new Enhet("enhet-1", "Enhet nr 1", "vardgivare-1" ,"Vårdgivare 1");
        return enhet;
    }
}
