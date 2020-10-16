package se.inera.intyg.webcert.web.service.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventTypeDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateRelationDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateRelationTypeDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateRelationsDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateStatusDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.event.model.CertificateEvent;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

@Service
public class CertificateServiceImpl implements CertificateService {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateServiceImpl.class);

    private final UtkastService utkastService;

    private final UnderskriftService underskriftService;

    private final IntygModuleRegistry moduleRegistry;

    private final IntygService intygService;

    private final CopyUtkastServiceHelper copyUtkastServiceHelper;

    private final CopyUtkastService copyUtkastService;

    private final CertificateRelationService certificateRelationService;

    private final CertificateEventService certificateEventService;

    private final ResourceLinkHelper resourceLinkHelper;

    @Autowired
    public CertificateServiceImpl(UtkastService utkastService, UnderskriftService underskriftService, IntygModuleRegistry moduleRegistry,
        IntygService intygService, CopyUtkastServiceHelper copyUtkastServiceHelper,
        CopyUtkastService copyUtkastService, CertificateRelationService certificateRelationService,
        CertificateEventService certificateEventService, ResourceLinkHelper resourceLinkHelper) {
        this.utkastService = utkastService;
        this.underskriftService = underskriftService;
        this.moduleRegistry = moduleRegistry;
        this.intygService = intygService;
        this.copyUtkastServiceHelper = copyUtkastServiceHelper;
        this.copyUtkastService = copyUtkastService;
        this.certificateRelationService = certificateRelationService;
        this.certificateEventService = certificateEventService;
        this.resourceLinkHelper = resourceLinkHelper;
    }

    @Override
    public CertificateDTO getCertificate(String certificateId) {
        final Utkast certificate = utkastService.getDraft(certificateId);
        try {
            final var moduleApi = moduleRegistry.getModuleApi(certificate.getIntygsTyp(), certificate.getIntygTypeVersion());
            final var certificateDTO = moduleApi.getCertificateDTOFromJson(certificate.getModel());
            certificateDTO.getMetadata().setCreated(certificate.getSkapad());
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

            final var certificateRelations = new CertificateRelationsDTO();
            certificateDTO.getMetadata().setRelations(certificateRelations);

            final var relations = certificateRelationService.getRelations(certificateDTO.getMetadata().getCertificateId());
            final var parentRelation = relations.getParent();
            if (parentRelation != null) {
                final CertificateRelationDTO parentCertificate = new CertificateRelationDTO();
                parentCertificate.setCertificateId(parentRelation.getIntygsId());
                parentCertificate.setCreated(parentRelation.getSkapad());
                if (parentRelation.isMakulerat()) {
                    parentCertificate.setStatus(CertificateStatusDTO.INVALIDATED);
                } else {
                    switch (parentRelation.getStatus()) {
                        case SIGNED:
                            parentCertificate.setStatus(CertificateStatusDTO.SIGNED);
                            break;
                        default:
                            parentCertificate.setStatus(CertificateStatusDTO.UNSIGNED);
                    }
                }
                parentCertificate.setType(CertificateRelationTypeDTO.REPLACED);
                certificateRelations.setParent(parentCertificate);
            }
            final var replacedByIntyg = relations.getLatestChildRelations().getReplacedByIntyg();
            final var replacedByUtkast = relations.getLatestChildRelations().getReplacedByUtkast();
            final var replacedBy = replacedByIntyg != null ? replacedByIntyg : replacedByUtkast;
            if (replacedBy != null) {
                final CertificateRelationDTO childCertificate = new CertificateRelationDTO();
                childCertificate.setCertificateId(replacedBy.getIntygsId());
                childCertificate.setCreated(replacedBy.getSkapad());
                if (replacedBy.isMakulerat()) {
                    childCertificate.setStatus(CertificateStatusDTO.INVALIDATED);
                } else {
                    switch (replacedBy.getStatus()) {
                        case SIGNED:
                            childCertificate.setStatus(CertificateStatusDTO.SIGNED);
                            break;
                        default:
                            childCertificate.setStatus(CertificateStatusDTO.UNSIGNED);
                    }
                }
                childCertificate.setType(CertificateRelationTypeDTO.REPLACED);
                certificateRelations.setChildren(new CertificateRelationDTO[]{childCertificate});
            }

            resourceLinkHelper.decorateCertificateWithValidActionLinks(certificateDTO);

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
    public CertificateDTO revokeCertificate(String certificateId, String reason, String message) {
        final var certificateType = intygService.getIntygTypeInfo(certificateId).getIntygType();
        intygService.revokeIntyg(certificateId, certificateType, reason, message);
        return getCertificate(certificateId);
    }

    @Override
    public String replaceCertificate(String certificateId, String certificateType, String patientId) {
        final var copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(Personnummer.createPersonnummer(patientId).get());

        CreateReplacementCopyRequest serviceRequest = copyUtkastServiceHelper
            .createReplacementCopyRequest(certificateId, certificateType, copyIntygRequest);
        CreateReplacementCopyResponse serviceResponse = copyUtkastService.createReplacementCopy(serviceRequest);

        return serviceResponse.getNewDraftIntygId();
    }

    @Override
    public CertificateEventDTO[] getCertificateEvents(String certificateId) {
        final var certificateEventList = certificateEventService.getCertificateEvents(certificateId);

        final var relations = certificateRelationService.getRelations(certificateId);

        final List<CertificateEventDTO> certificateEventDTOList = new ArrayList<>();
        for (CertificateEvent certificateEvent : certificateEventList) {
            if (certificateEvent.getEventCode() == EventCode.RELINTYGMAKULE) {
                continue;
            }

            final var certificateEventDTO = new CertificateEventDTO();
            certificateEventDTO.setCertificateId(certificateEvent.getCertificateId());
            certificateEventDTO.setTimestamp(certificateEvent.getTimestamp());
            CertificateEventTypeDTO mappedEventCode;
            switch (certificateEvent.getEventCode()) {
                case SKAPAT:
                    mappedEventCode = CertificateEventTypeDTO.CREATED;
                    break;
                case SIGNAT:
                    mappedEventCode = CertificateEventTypeDTO.SIGNED;
                    break;
                case SKICKAT:
                    mappedEventCode = CertificateEventTypeDTO.SENT;
                    break;
                case MAKULERAT:
                    mappedEventCode = CertificateEventTypeDTO.REVOKED;
                    break;
                case ERSATTER:
                    mappedEventCode = CertificateEventTypeDTO.REPLACES;
                    final var parentRelation = relations.getParent();
                    if (parentRelation != null) {
                        certificateEventDTO.setRelatedCertificateId(parentRelation.getIntygsId());
                        if (parentRelation.isMakulerat()) {
                            certificateEventDTO.setRelatedCertificateStatus(CertificateStatusDTO.INVALIDATED);
                        } else {
                            switch (parentRelation.getStatus()) {
                                case SIGNED:
                                    certificateEventDTO.setRelatedCertificateStatus(CertificateStatusDTO.SIGNED);
                                    break;
                                default:
                                    certificateEventDTO.setRelatedCertificateStatus(CertificateStatusDTO.UNSIGNED);
                            }
                        }
                    }
                    break;
                default:
                    mappedEventCode = CertificateEventTypeDTO.CREATED;
            }
            certificateEventDTO.setType(mappedEventCode);
            certificateEventDTOList.add(certificateEventDTO);

            if (certificateEventDTO.getType() == CertificateEventTypeDTO.SIGNED) {
                final var availableForPatient = new CertificateEventDTO();
                availableForPatient.setType(CertificateEventTypeDTO.AVAILABLE_FOR_PATIENT);
                availableForPatient.setTimestamp(certificateEventDTO.getTimestamp());
                availableForPatient.setCertificateId(certificateEventDTO.getCertificateId());
                certificateEventDTOList.add(availableForPatient);
            }
        }

        final var replacedByIntyg = relations.getLatestChildRelations().getReplacedByIntyg();
        final var replacedByUtkast = relations.getLatestChildRelations().getReplacedByUtkast();
        final var replacedBy = replacedByIntyg != null ? replacedByIntyg : replacedByUtkast;
        if (replacedBy != null) {
            final var replaced = new CertificateEventDTO();
            replaced.setCertificateId(certificateId);
            replaced.setType(CertificateEventTypeDTO.REPLACED);
            replaced.setTimestamp(replacedBy.getSkapad());
            replaced.setRelatedCertificateId(replacedBy.getIntygsId());
            if (replacedBy.isMakulerat()) {
                replaced.setRelatedCertificateStatus(CertificateStatusDTO.INVALIDATED);
            } else {
                switch (replacedBy.getStatus()) {
                    case SIGNED:
                        replaced.setRelatedCertificateStatus(CertificateStatusDTO.SIGNED);
                        break;
                    default:
                        replaced.setRelatedCertificateStatus(CertificateStatusDTO.UNSIGNED);
                }
            }

            certificateEventDTOList.add(replaced);
        }

        certificateEventDTOList.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));

        return certificateEventDTOList.toArray(new CertificateEventDTO[certificateEventDTOList.size()]);
    }

    private ValidationErrorDTO convertValidationError(DraftValidationMessage validationMessage) {
        final var validationError = new ValidationErrorDTO();
        validationError.setCategory(validationMessage.getCategory());
        validationError.setField(validationMessage.getField());
        validationError.setType(validationMessage.getType().name());
        validationError.setId(validationMessage.getQuestionId());
        // TODO: We need a way to give correct message to the user. Or should frontend adjust based on component?
        if (validationError.getField().contains("har")) {
            validationError.setText("Välj ett alternativ.");
        } else {
            validationError.setText("Ange ett svar.");
        }
        return validationError;
    }
}
