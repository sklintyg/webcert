package se.inera.intyg.webcert.logsender.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.logmessages.AbstractLogMessage;
import se.inera.intyg.common.logmessages.Enhet;
import se.inera.intyg.common.logmessages.Patient;
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
 * Encapsulates AbstractLogMessage (internal format) -> LogType (ehr format) conversion.
 *
 * Created by eriklupander on 2016-02-29.
 */
@Service
public class LogTypeFactoryImpl implements LogTypeFactory {

    @Override
    public LogType convertFromList(List<AbstractLogMessage> sources) {

        AbstractLogMessage source = sources.get(0);
        LogType logType = new LogType();

        logType.setLogId(source.getLogId());

        buildSystemType(source, logType);
        buildActivityType(source, logType);
        buildUserType(source, logType);

        logType.setResources(new ResourcesType());
        logType.getResources().getResource().addAll(sources.stream().map(this::buildResource).collect(Collectors.toList()));

        return logType;
    }

    @Override
    public LogType convert(AbstractLogMessage source) {
        LogType logType = new LogType();
        logType.setLogId(source.getLogId());

        buildSystemType(source, logType);
        buildActivityType(source, logType);
        buildUserType(source, logType);

        logType.setResources(new ResourcesType());

        ResourceType resource = buildResource(source);

        logType.getResources().getResource().add(resource);

        return logType;
    }

    private void buildUserType(AbstractLogMessage source, LogType logType) {
        UserType user = new UserType();
        user.setUserId(source.getUserId());
        user.setName(source.getUserName());
        user.setCareProvider(careProvider(source.getUserCareUnit()));
        user.setCareUnit(careUnit(source.getUserCareUnit()));
        logType.setUser(user);
    }

    private void buildSystemType(AbstractLogMessage source, LogType logType) {
        SystemType system = new SystemType();
        system.setSystemId(source.getSystemId());
        system.setSystemName(source.getSystemName());
        logType.setSystem(system);
    }

    private void buildActivityType(AbstractLogMessage source, LogType logType) {
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

    private ResourceType buildResource(AbstractLogMessage source) {
        ResourceType resource = new ResourceType();
        resource.setResourceType(source.getResourceType());
        resource.setCareProvider(careProvider(source.getResourceOwner()));
        resource.setCareUnit(careUnit(source.getResourceOwner()));

        resource.setPatient(patient(source.getPatient()));
        return resource;
    }


}
