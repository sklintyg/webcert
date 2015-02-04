package se.inera.webcert.service.utkast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.pu.model.Person;
import se.inera.webcert.pu.model.PersonSvar;
import se.inera.webcert.pu.services.PUService;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.notification.NotificationMessageFactory;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.service.utkast.dto.CreateNewDraftCopyRequest;
import se.inera.webcert.service.utkast.dto.CreateNewDraftCopyResponse;

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
    private NotificationService notificationService;

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.utkast.CopyUtkastService#createCopy(se.inera.webcert.service.utkast.dto.
     * CreateNewDraftCopyRequest)
     */
    @Override
    public CreateNewDraftCopyResponse createCopy(CreateNewDraftCopyRequest copyRequest) {

        String originalIntygId = copyRequest.getOriginalIntygId();

        LOG.debug("Creating copy of intyg '{}'", originalIntygId);

        try {

            Person patientDetails = null;

            if (!copyRequest.isDjupintegrerad()) {
                patientDetails = refreshPatientDetails(copyRequest);
            }

            Utkast copyUtkast = null;

            if (utkastRepository.exists(originalIntygId)) {
                copyUtkast = utkastBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails);
            } else {
                copyUtkast = utkastBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails);
            }

            Utkast savedUtkast = utkastRepository.save(copyUtkast);

            sendNotification(savedUtkast);

            return new CreateNewDraftCopyResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygsId());

        } catch (ModuleException me) {
            LOG.error("Module exception occured when trying to make a copy of " + originalIntygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        } catch (ModuleNotFoundException e) {
            LOG.error("Module exception occured when trying to make a copy of " + originalIntygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    private Person refreshPatientDetails(CreateNewDraftCopyRequest copyRequest) {

        String patientPersonnummer = copyRequest.getPatientPersonnummer();

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

    private void sendNotification(Utkast utkast) {

        NotificationRequestType notificationRequestType = NotificationMessageFactory.createNotificationFromCreatedDraft(utkast);
        String logMsg = "Notification sent: utkast with id '{}' was created.";

        notificationService.notify(notificationRequestType);

        LOG.debug(logMsg, utkast.getIntygsId());
    }

}
