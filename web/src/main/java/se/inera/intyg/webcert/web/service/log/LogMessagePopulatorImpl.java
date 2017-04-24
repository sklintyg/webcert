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
    public PdlLogMessage populateLogMessage(LogRequest logRequest, PdlLogMessage logMsg, LogUser user) {

        populateWithCurrentUserAndCareUnit(logMsg, user);

        String careUnitId = logRequest.getIntygCareUnitId();
        String careUnitName = logRequest.getIntygCareUnitName();

        String careGiverId = logRequest.getIntygCareGiverId();
        String careGiverName = logRequest.getIntygCareGiverName();

        Patient patient = new Patient(logRequest.getPatientId().getPersonnummer().replace("-", "").replace("+", ""),
                logRequest.getPatientName());
        Enhet resourceOwner = new Enhet(careUnitId, careUnitName, careGiverId, careGiverName);

        PdlResource pdlResource = new PdlResource();
        pdlResource.setPatient(patient);
        pdlResource.setResourceOwner(resourceOwner);
        pdlResource.setResourceType(ResourceType.RESOURCE_TYPE_INTYG.getResourceTypeName());

        logMsg.getPdlResourceList().add(pdlResource);

        logMsg.setSystemId(systemId);
        logMsg.setSystemName(systemName);
        logMsg.setTimestamp(LocalDateTime.now());
        logMsg.setPurpose(ActivityPurpose.CARE_TREATMENT);

        populateActivityArgsWithAdditionalInformationIfApplicable(logRequest, logMsg);

        return logMsg;
    }

    private void populateActivityArgsWithAdditionalInformationIfApplicable(LogRequest logRequest, PdlLogMessage logMsg) {
        if (!Strings.isNullOrEmpty(logRequest.getAdditionalInfo())) {
            if (!Strings.isNullOrEmpty(logMsg.getActivityArgs()) && !logMsg.getActivityArgs().equals(logRequest.getAdditionalInfo())) {
                logMsg.setActivityArgs(logMsg.getActivityArgs() + "\n" + logRequest.getAdditionalInfo());
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

        Enhet vardenhet = new Enhet(user.getEnhetsId(), user.getEnhetsNamn(), user.getVardgivareId(), user.getVardgivareNamn());
        logMsg.setUserCareUnit(vardenhet);
    }

}
