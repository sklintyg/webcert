package se.inera.intyg.webcert.web.service.utkast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.service.log.LogRequestFactory;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.utkast.dto.CopyUtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyResponse;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.integration.pu.model.Person;
import se.inera.intyg.webcert.integration.pu.model.PersonSvar;
import se.inera.intyg.webcert.integration.pu.services.PUService;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;

@Service
public class CopyUtkastServiceImpl implements CopyUtkastService {

    private static final Logger LOG = LoggerFactory.getLogger(CopyUtkastServiceImpl.class);

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private PUService personUppgiftsService;

    @Autowired
    private CopyUtkastBuilder utkastBuilder;

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LogService logService;

    @Autowired
    private MonitoringLogService monitoringService;

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.web.service.utkast.CopyUtkastService#createCopy(se.inera.intyg.webcert.web.service.utkast.dto.
     * CreateNewDraftCopyRequest)
     */
    @Override
    @Transactional("jpaTransactionManager")
    public CreateNewDraftCopyResponse createCopy(CreateNewDraftCopyRequest copyRequest) {

        String originalIntygId = copyRequest.getOriginalIntygId();

        LOG.debug("Creating copy of intyg '{}'", originalIntygId);

        try {

            Person patientDetails = null;

            if (!copyRequest.isDjupintegrerad()) {
                patientDetails = refreshPatientDetails(copyRequest);
            }

            CopyUtkastBuilderResponse builderResponse;

            if (utkastRepository.exists(originalIntygId)) {
                builderResponse = utkastBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails);
            } else {
                builderResponse = utkastBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails);
            }

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            Utkast savedUtkast = utkastRepository.save(builderResponse.getUtkastCopy());

            monitoringService.logIntygCopied(savedUtkast.getIntygsId(), originalIntygId);

            // notify
            notificationService.sendNotificationForDraftCreated(savedUtkast);
            LOG.debug("Notification sent: utkast with id '{}' was created as a copy.", savedUtkast.getIntygsId());

            LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(savedUtkast);
            logService.logCreateIntyg(logRequest);

            return new CreateNewDraftCopyResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygsId());

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error("Module exception occured when trying to make a copy of " + originalIntygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    private Person refreshPatientDetails(CreateNewDraftCopyRequest copyRequest) {

        Personnummer patientPersonnummer = copyRequest.getPatientPersonnummer();

        if (copyRequest.containsNyttPatientPersonnummer()) {
            patientPersonnummer = copyRequest.getNyttPatientPersonnummer();
            LOG.debug("Request contained a new personnummer to use for the copy");
        }

        LOG.debug("Refreshing person data to use for the copy");

        PersonSvar personSvar = personUppgiftsService.getPerson(patientPersonnummer);

        if (PersonSvar.Status.ERROR.equals(personSvar.getStatus())) {
            LOG.error("An error occured when using '{}' to lookup person data");
            return null;
        } else if (PersonSvar.Status.NOT_FOUND.equals(personSvar.getStatus())) {
            LOG.error("No person data was found using '{}' to lookup person data", patientPersonnummer);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "No person data found using '"
                    + patientPersonnummer + "'");
        }

        return personSvar.getPerson();
    }

    private void checkIntegreradEnhet(CopyUtkastBuilderResponse builderResponse) {

        String orginalEnhetsId = builderResponse.getOrginalEnhetsId();
        Utkast utkastCopy = builderResponse.getUtkastCopy();

        IntegreradEnhetEntry newEntry = new IntegreradEnhetEntry(utkastCopy.getEnhetsId(), utkastCopy.getEnhetsNamn(), utkastCopy.getVardgivarId(),
                utkastCopy.getVardgivarNamn());

        integreradeEnheterRegistry.addIfSameVardgivareButDifferentUnits(orginalEnhetsId, newEntry);

    }

}
