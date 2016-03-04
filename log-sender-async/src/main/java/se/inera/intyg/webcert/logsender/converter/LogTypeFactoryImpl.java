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

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.logmessages.Enhet;
import se.inera.intyg.common.logmessages.Patient;
import se.inera.intyg.common.logmessages.PdlLogMessage;
import se.inera.intyg.common.logmessages.PdlResource;
import se.riv.ehr.log.v1.ActivityType;
import se.riv.ehr.log.v1.CareProviderType;
import se.riv.ehr.log.v1.CareUnitType;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.PatientType;
import se.riv.ehr.log.v1.ResourceType;
import se.riv.ehr.log.v1.ResourcesType;
import se.riv.ehr.log.v1.SystemType;
import se.riv.ehr.log.v1.UserType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Encapsulates PdlLogMessage (internal format) -> LogType (ehr format) conversion.
 *
 * Created by eriklupander on 2016-02-29.
 */
@Service
public class LogTypeFactoryImpl implements LogTypeFactory {

    @Override
    public LogType convert(PdlLogMessage source) {
        LogType logType = new LogType();
        logType.setLogId(source.getLogId());

        buildSystemType(source, logType);
        buildActivityType(source, logType);
        buildUserType(source, logType);

        logType.setResources(new ResourcesType());

        List<ResourceType> resources = source.getPdlResourceList()
                .stream()
                .map(this::buildResource)
                .collect(Collectors.toList());
        logType.getResources().getResource().addAll(resources);

        return logType;
    }

    private void buildUserType(PdlLogMessage source, LogType logType) {
        UserType user = new UserType();
        user.setUserId(source.getUserId());
        user.setName(source.getUserName());
        user.setCareProvider(careProvider(source.getUserCareUnit()));
        user.setCareUnit(careUnit(source.getUserCareUnit()));
        logType.setUser(user);
    }

    private void buildSystemType(PdlLogMessage source, LogType logType) {
        SystemType system = new SystemType();
        system.setSystemId(source.getSystemId());
        system.setSystemName(source.getSystemName());
        logType.setSystem(system);
    }

    private void buildActivityType(PdlLogMessage source, LogType logType) {
        ActivityType activity = new ActivityType();
        activity.setActivityType(source.getActivityType().getType());
        activity.setStartDate(source.getTimestamp());
        activity.setPurpose(source.getPurpose().getType());
        activity.setActivityLevel(source.getActivityLevel());

        if (StringUtils.isNotEmpty(source.getActivityArgs())) {
            activity.setActivityArgs(source.getActivityArgs());
        }

        logType.setActivity(activity);
    }

    private PatientType patient(Patient source) {
        PatientType patient = new PatientType();
        patient.setPatientId(source.getPatientId().getPersonnummerWithoutDash());
        patient.setPatientName(source.getPatientNamn());
        return patient;
    }

    private CareUnitType careUnit(Enhet source) {
        CareUnitType careUnit = new CareUnitType();
        careUnit.setCareUnitId(source.getEnhetsId());
        careUnit.setCareUnitName(source.getEnhetsNamn());
        return careUnit;
    }

    private CareProviderType careProvider(Enhet source) {
        CareProviderType careProvider = new CareProviderType();
        careProvider.setCareProviderId(source.getVardgivareId());
        careProvider.setCareProviderName(source.getVardgivareNamn());
        return careProvider;
    }

    private ResourceType buildResource(PdlResource source) {
        ResourceType resource = new ResourceType();
        resource.setResourceType(source.getResourceType());
        resource.setCareProvider(careProvider(source.getResourceOwner()));
        resource.setCareUnit(careUnit(source.getResourceOwner()));

        resource.setPatient(patient(source.getPatient()));
        return resource;
    }
}
