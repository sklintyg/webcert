package se.inera.intyg.webcert.web.service.facade;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateStatusDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

@Service
public class CertificateServiceImpl implements CertificateService {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateServiceImpl.class);

    private final UtkastService utkastService;

    private final UnderskriftService underskriftService;

    private final IntygModuleRegistry moduleRegistry;

    private final IntygService intygService;

    private final ResourceLinkHelper resourceLinkHelper;

    @Autowired
    public CertificateServiceImpl(UtkastService utkastService, UnderskriftService underskriftService, IntygModuleRegistry moduleRegistry,
        IntygService intygService, ResourceLinkHelper resourceLinkHelper) {
        this.utkastService = utkastService;
        this.underskriftService = underskriftService;
        this.moduleRegistry = moduleRegistry;
        this.intygService = intygService;
        this.resourceLinkHelper = resourceLinkHelper;
    }

    @Override
    public CertificateDTO getCertificate(String certificateId) {
        final Utkast certificate = utkastService.getDraft(certificateId);
        try {
            final var moduleApi = moduleRegistry.getModuleApi(certificate.getIntygsTyp(), certificate.getIntygTypeVersion());
            final var certificateDTO = moduleApi.getCertificateDTOFromJson(certificate.getModel());
            certificateDTO.getMetadata().setVersion(certificate.getVersion());
            if (certificate.getAterkalladDatum() != null) {
                certificateDTO.getMetadata().setCertificateStatus(CertificateStatusDTO.INVALIDATED);
            }
            if (certificateDTO.getMetadata().getPatient().getFullName() == null) {
                certificateDTO.getMetadata().getPatient().setFirstName(certificate.getPatientFornamn());
                certificateDTO.getMetadata().getPatient().setLastName(certificate.getPatientEfternamn());
                certificateDTO.getMetadata().getPatient().setFullName(
                    certificate.getPatientFornamn() + ' ' + certificate.getPatientEfternamn()
                );
            }
            return certificateDTO;
        } catch (Exception ex) {
            LOG.error("Cannot convert certificate!", ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public long saveCertificate(CertificateDTO certificate) {
        final var certificateId = certificate.getMetadata().getCertificateId();
        final var certificateType = certificate.getMetadata().getCertificateType();
        final var certificateTypeVersion = certificate.getMetadata().getCertificateTypeVersion();
        final var version = certificate.getMetadata().getVersion();
        final var createPdlLogEvent = true;

        final Utkast currentCertificate = utkastService.getDraft(certificateId);

        try {
            final ModuleApi moduleApi = moduleRegistry.getModuleApi(certificateType, certificateTypeVersion);
            final var jsonFromCertificateDTO = moduleApi.getJsonFromCertificateDTO(certificate, currentCertificate.getModel());
            final var saveDraftResponse = utkastService.saveDraft(certificateId, version, jsonFromCertificateDTO, createPdlLogEvent);
            return saveDraftResponse.getVersion();
        } catch (Exception ex) {
            LOG.error("Cannot convert certificate!", ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ValidationErrorDTO[] validate(CertificateDTO certificate) {
        final var certificateId = certificate.getMetadata().getCertificateId();
        final var certificateType = certificate.getMetadata().getCertificateType();
        final var certificateTypeVersion = certificate.getMetadata().getCertificateTypeVersion();

        final var currentCertificate = utkastService.getDraft(certificateId);

        try {
            final ModuleApi moduleApi = moduleRegistry.getModuleApi(certificateType, certificateTypeVersion);
            final var jsonFromCertificateDTO = moduleApi.getJsonFromCertificateDTO(certificate, currentCertificate.getModel());
            final DraftValidation draftValidation = utkastService.validateDraft(certificateId, certificateType, jsonFromCertificateDTO);
            return draftValidation.getMessages().stream()
                .map(this::convertValidationError)
                .toArray(ValidationErrorDTO[]::new);
        } catch (Exception ex) {
            LOG.error("Cannot convert certificate!", ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public CertificateDTO signCertificate(CertificateDTO certificate) {
        final var certificateId = certificate.getMetadata().getCertificateId();
        final var certificateType = certificate.getMetadata().getCertificateType();
        final var version = certificate.getMetadata().getVersion();
        final var signMethod = SignMethod.FAKE;
        final var ticketId = UUID.randomUUID().toString();

        // We just start and finalize a fake signing request.
        underskriftService.startSigningProcess(certificateId, certificateType, version, signMethod, ticketId);
        underskriftService.fakeSignature(certificateId, certificateType, version, ticketId);

        return getCertificate(certificateId);
    }

    @Override
    public void deleteCertificate(String certificateId, long version) {
        utkastService.deleteUnsignedDraft(certificateId, version);
    }

    @Override
    public void revokeCertificate(String certificateId, String reason, String message) {
        final var certificateType = intygService.getIntygTypeInfo(certificateId).getIntygType();
        intygService.revokeIntyg(certificateId, certificateType, reason, message);
    }

    private ValidationErrorDTO convertValidationError(DraftValidationMessage validationMessage) {
        final var validationError = new ValidationErrorDTO();
        validationError.setCategory(validationMessage.getCategory());
        validationError.setField(validationMessage.getField());
        validationError.setText("Välj ett alternativ.");
        validationError.setType(validationMessage.getType().name());
        validationError.setId(validationMessage.getQuestionId());
        return validationError;
    }
}
