package se.inera.webcert.notifications.service;

import org.joda.time.LocalDateTime;
import se.inera.certificate.model.Status;
import se.inera.certificate.modules.registry.IntygModule;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.ApplicationOrigin;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.ModuleContainerApi;
import se.inera.certificate.modules.support.api.dto.CertificateResponse;
import se.inera.certificate.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.certificate.modules.support.api.dto.CreateNewDraftHolder;
import se.inera.certificate.modules.support.api.dto.HoSPersonal;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.PdfResponse;
import se.inera.certificate.modules.support.api.dto.ValidateDraftResponse;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.modules.support.api.notification.NotificationMessage;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;

import java.util.List;


public class MockIntygModuleRegistry implements IntygModuleRegistry {
    @Override
    public ModuleApi getModuleApi(String id) throws ModuleNotFoundException {
        return new ModuleApi() {
            @Override
            public void setModuleContainer(ModuleContainerApi moduleContainer) {
            }

            @Override
            public ModuleContainerApi getModuleContainer() {
                return null;
            }

            @Override
            public ValidateDraftResponse validateDraft(InternalModelHolder internalModel) throws ModuleException {
                return null;
            }

            @Override
            public PdfResponse pdf(InternalModelHolder internalModel, List<Status> statuses, ApplicationOrigin applicationOrigin) throws ModuleException {
                return null;
            }

            @Override
            public InternalModelResponse createNewInternal(CreateNewDraftHolder draftCertificateHolder) throws ModuleException {
                return null;
            }

            @Override
            public InternalModelResponse createNewInternalFromTemplate(CreateDraftCopyHolder draftCopyHolder, InternalModelHolder template) throws ModuleException {
                return null;
            }

            @Override
            public void registerCertificate(InternalModelHolder internalModel, String logicalAddress) throws ModuleException {

            }

            @Override
            public void sendCertificateToRecipient(InternalModelHolder internalModel, String logicalAddress, String recipientId) throws ModuleException {

            }

            @Override
            public CertificateResponse getCertificate(String certificateId, String logicalAddress) throws ModuleException {
                return null;
            }

            @Override
            public boolean isModelChanged(String persistedState, String currentState) throws ModuleException {
                return false;
            }

            @Override
            public InternalModelResponse updateBeforeSave(InternalModelHolder internalModel, HoSPersonal hosPerson) throws ModuleException {
                return null;
            }

            @Override
            public InternalModelResponse updateBeforeSigning(InternalModelHolder internalModel, HoSPersonal hosPerson, LocalDateTime signingDate) throws ModuleException {
                return null;
            }

            @Override
            public Object createNotification(NotificationMessage notificationMessage) throws ModuleException {
                CertificateStatusUpdateForCareType certificateStatusUpdateForCareType = new CertificateStatusUpdateForCareType();
                UtlatandeType utlatande = new UtlatandeType();
                UtlatandeId utlatandeId = new UtlatandeId();
                utlatandeId.setExtension("id1");
                utlatande.setUtlatandeId(utlatandeId);
                certificateStatusUpdateForCareType.setUtlatande(utlatande);
                return certificateStatusUpdateForCareType;
            }

            @Override
            public String marshall(String jsonString) {
                return null;
            }
        };
    }

    @Override
    public ModuleEntryPoint getModuleEntryPoint(String id) throws ModuleNotFoundException {
        return null;
    }

    @Override
    public IntygModule getIntygModule(String id) throws ModuleNotFoundException {
        return null;
    }

    @Override
    public List<IntygModule> listAllModules() {
        return null;
    }

    @Override
    public List<ModuleEntryPoint> getModuleEntryPoints() {
        return null;
    }

    @Override
    public boolean moduleExists(String moduleId) {
        return false;
    }
}
