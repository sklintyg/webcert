/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.logmessages.ActivityPurpose;
import se.inera.intyg.infra.logmessages.Enhet;
import se.inera.intyg.infra.logmessages.Patient;
import se.inera.intyg.infra.logmessages.PdlLogMessage;
import se.inera.intyg.infra.logmessages.PdlResource;
import se.inera.intyg.infra.logmessages.ResourceType;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;

import java.time.LocalDateTime;

/**
 * Provides population of a {@link PdlLogMessage} instance from {@link LogRequest} and {@link LogUser}
 * instances.
 *
 * Created by eriklupander on 2017-04-24.
 */
@Service
public class LogMessagePopulatorImpl implements LogMessagePopulator {

    @Value("${pdlLogging.systemId}")
    private String systemId;

    @Value("${pdlLogging.systemName}")
    private String systemName;

    @Override
    public PdlLogMessage populateLogMessage(PdlLogMessage logMessage, LogRequest logRequest, LogUser logUser) {

        populateWithCurrentUserAndCareUnit(logMessage, logUser);

        PdlResource pdlResource = new PdlResource();
        pdlResource.setPatient(createPatient(logRequest));
        pdlResource.setResourceOwner(createEnhet(logRequest));
        pdlResource.setResourceType(ResourceType.RESOURCE_TYPE_INTYG.getResourceTypeName());

        logMessage.getPdlResourceList().add(pdlResource);

        logMessage.setSystemId(systemId);
        logMessage.setSystemName(systemName);
        logMessage.setTimestamp(LocalDateTime.now());
        logMessage.setPurpose(ActivityPurpose.CARE_TREATMENT);

        populateActivityArgsWithAdditionalInformationIfApplicable(logMessage, logRequest);

        // Clear values due to regulations
        unsetValues(logMessage);

        return logMessage;
    }

    private Enhet createEnhet(LogRequest logRequest) {
        String careUnitId = logRequest.getIntygCareUnitId();
        String careUnitName = logRequest.getIntygCareUnitName();
        String careGiverId = logRequest.getIntygCareGiverId();
        String careGiverName = logRequest.getIntygCareGiverName();

        return createEnhet(careUnitId, careUnitName, careGiverId, careGiverName);
    }

    private Enhet createEnhet(LogUser logUser) {
        return createEnhet(logUser.getEnhetsId(), logUser.getEnhetsNamn(), logUser.getVardgivareId(), logUser.getVardgivareNamn());
    }

    private Enhet createEnhet(String enhetsId, String enhetsNamn, String vardgivareId, String vardgivareNamn) {
        return new Enhet(enhetsId, enhetsNamn, vardgivareId, vardgivareNamn);
    }

    private Patient createPatient(LogRequest logRequest) {
        return createPatient(
                logRequest.getPatientId().getPersonnummer().
                        replace("-", "").
                        replace("+", ""),
                logRequest.getPatientName());
    }

    private Patient createPatient(String patientId, String patientName) {
        return new Patient(patientId, patientName);
    }

    private void populateActivityArgsWithAdditionalInformationIfApplicable(PdlLogMessage logMsg, LogRequest logRequest) {
        if (!Strings.isNullOrEmpty(logRequest.getAdditionalInfo())) {
            if (!Strings.isNullOrEmpty(logMsg.getActivityArgs()) && !logMsg.getActivityArgs().equals(logRequest.getAdditionalInfo())) {
                logMsg.setActivityArgs(logMsg.getActivityArgs() + ". " + logRequest.getAdditionalInfo());
            } else {
                logMsg.setActivityArgs(logRequest.getAdditionalInfo());
            }
        }
    }

    private void populateWithCurrentUserAndCareUnit(PdlLogMessage logMsg, LogUser user) {
        logMsg.setUserId(user.getUserId());
        logMsg.setUserName(user.getUserName());
        logMsg.setUserAssignment(user.getUserAssignment());
        logMsg.setUserTitle(user.getUserTitle());
        logMsg.setUserCareUnit(createEnhet(user));
    }

    private void unsetValues(final PdlLogMessage logMessage) {
        // INTYG-8349: Inget användarnamn vid PDL-logging
        logMessage.setUserName("");

        // INTYG-4647: Inget patientnamn vid PDL-logging
        logMessage.getPdlResourceList().forEach(pdlResource ->
                pdlResource.setPatient(createPatient(pdlResource.getPatient().getPatientId(), ""))
        );
    }


}
