package se.inera.intyg.webcert.web.service.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.model.UtkastStatus;
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
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
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
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateResponse;
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
        return convertToCertificate(certificate);
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
        final var certificate = getCertificate(certificateId);

        if (certificate.getMetadata().getCertificateStatus() == CertificateStatusDTO.LOCKED) {
            utkastService.revokeLockedDraft(certificateId, certificate.getMetadata().getCertificateType(), reason, message);
        } else {
            intygService.revokeIntyg(certificateId, certificate.getMetadata().getCertificateType(), reason, message);
        }

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
    public String copyCertificate(String certificateId, String certificateType, String patientId) {
        final var copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(Personnummer.createPersonnummer(patientId).get());

        CreateUtkastFromTemplateRequest serviceRequest = copyUtkastServiceHelper
            .createUtkastFromUtkast(certificateId, certificateType, copyIntygRequest);
        CreateUtkastFromTemplateResponse serviceResponse = copyUtkastService.createUtkastCopy(serviceRequest);

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
                case LAST:
                    mappedEventCode = CertificateEventTypeDTO.LOCKED;
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
                    decorateCertificateEventWithParentInfo(certificateEventDTO, relations.getParent());
                    break;
                case KOPIERATFRAN:
                    mappedEventCode = CertificateEventTypeDTO.COPIED_FROM;
                    decorateCertificateEventWithParentInfo(certificateEventDTO, relations.getParent());
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
                if (replacedBy.getStatus() == UtkastStatus.DRAFT_LOCKED) {
                    replaced.setRelatedCertificateStatus(CertificateStatusDTO.LOCKED_REVOKED);
                } else {
                    replaced.setRelatedCertificateStatus(CertificateStatusDTO.REVOKED);
                }
            } else {
                switch (replacedBy.getStatus()) {
                    case SIGNED:
                        replaced.setRelatedCertificateStatus(CertificateStatusDTO.SIGNED);
                        break;
                    case DRAFT_LOCKED:
                        replaced.setRelatedCertificateStatus(CertificateStatusDTO.LOCKED);
                        break;
                    default:
                        replaced.setRelatedCertificateStatus(CertificateStatusDTO.UNSIGNED);
                }
            }

            certificateEventDTOList.add(replaced);
        }

        final var copiedBy = relations.getLatestChildRelations().getUtkastCopy();
        if (copiedBy != null) {
            final var copied = new CertificateEventDTO();
            copied.setCertificateId(certificateId);
            copied.setType(CertificateEventTypeDTO.COPIED_BY);
            copied.setTimestamp(copiedBy.getSkapad());
            copied.setRelatedCertificateId(copiedBy.getIntygsId());
            if (copiedBy.isMakulerat()) {
                if (copiedBy.getStatus() == UtkastStatus.DRAFT_LOCKED) {
                    copied.setRelatedCertificateStatus(CertificateStatusDTO.LOCKED_REVOKED);
                } else {
                    copied.setRelatedCertificateStatus(CertificateStatusDTO.REVOKED);
                }
            } else {
                switch (copiedBy.getStatus()) {
                    case SIGNED:
                        copied.setRelatedCertificateStatus(CertificateStatusDTO.SIGNED);
                        break;
                    case DRAFT_LOCKED:
                        copied.setRelatedCertificateStatus(CertificateStatusDTO.LOCKED);
                        break;
                    default:
                        copied.setRelatedCertificateStatus(CertificateStatusDTO.UNSIGNED);
                }
            }

            certificateEventDTOList.add(copied);
        }

        certificateEventDTOList.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));

        return certificateEventDTOList.toArray(new CertificateEventDTO[certificateEventDTOList.size()]);
    }

    private void decorateCertificateEventWithParentInfo(CertificateEventDTO certificateEventDTO,
        WebcertCertificateRelation parentRelation) {
        if (parentRelation != null) {
            certificateEventDTO.setRelatedCertificateId(parentRelation.getIntygsId());
            if (parentRelation.isMakulerat()) {
                if (parentRelation.getStatus() == UtkastStatus.DRAFT_LOCKED) {
                    certificateEventDTO.setRelatedCertificateStatus(CertificateStatusDTO.LOCKED_REVOKED);
                } else {
                    certificateEventDTO.setRelatedCertificateStatus(CertificateStatusDTO.REVOKED);
                }
            } else {
                switch (parentRelation.getStatus()) {
                    case SIGNED:
                        certificateEventDTO.setRelatedCertificateStatus(CertificateStatusDTO.SIGNED);
                        break;
                    case DRAFT_LOCKED:
                        certificateEventDTO.setRelatedCertificateStatus(CertificateStatusDTO.LOCKED);
                        break;
                    default:
                        certificateEventDTO.setRelatedCertificateStatus(CertificateStatusDTO.UNSIGNED);
                }
            }
        }
    }

    @Override
    public CertificateDTO forwardCertificate(String certificateId, long version, boolean forwarded) {
        final var certificate = utkastService.setNotifiedOnDraft(certificateId, version, forwarded);
        return convertToCertificate(certificate);
    }

    private CertificateDTO convertToCertificate(Utkast certificate) {
        try {
            final var moduleApi = moduleRegistry.getModuleApi(certificate.getIntygsTyp(), certificate.getIntygTypeVersion());
            final var certificateDTO = moduleApi.getCertificateDTOFromJson(certificate.getModel());
            certificateDTO.getMetadata().setCreated(certificate.getSkapad());
            certificateDTO.getMetadata().setVersion(certificate.getVersion());
            certificateDTO.getMetadata().setForwarded(certificate.getVidarebefordrad());

            if (certificate.getAterkalladDatum() != null) {
                if (certificate.getStatus() == UtkastStatus.DRAFT_LOCKED) {
                    certificateDTO.getMetadata().setCertificateStatus(CertificateStatusDTO.LOCKED_REVOKED);
                } else {
                    certificateDTO.getMetadata().setCertificateStatus(CertificateStatusDTO.REVOKED);
                }
            } else {
                switch (certificate.getStatus()) {
                    case SIGNED:
                        certificateDTO.getMetadata().setCertificateStatus(CertificateStatusDTO.SIGNED);
                        break;
                    case DRAFT_LOCKED:
                        certificateDTO.getMetadata().setCertificateStatus(CertificateStatusDTO.LOCKED);
                        break;
                    default:
                        certificateDTO.getMetadata().setCertificateStatus(CertificateStatusDTO.UNSIGNED);
                }
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
                    if (parentRelation.getStatus() == UtkastStatus.DRAFT_LOCKED) {
                        parentCertificate.setStatus(CertificateStatusDTO.LOCKED_REVOKED);
                    } else {
                        parentCertificate.setStatus(CertificateStatusDTO.REVOKED);
                    }
                } else {
                    switch (parentRelation.getStatus()) {
                        case SIGNED:
                            parentCertificate.setStatus(CertificateStatusDTO.SIGNED);
                            break;
                        case DRAFT_LOCKED:
                            parentCertificate.setStatus(CertificateStatusDTO.LOCKED);
                            break;
                        default:
                            parentCertificate.setStatus(CertificateStatusDTO.UNSIGNED);
                    }
                }
                switch (parentRelation.getRelationKod()) {
                    case ERSATT:
                        parentCertificate.setType(CertificateRelationTypeDTO.REPLACED);
                        break;
                    case KOPIA:
                        parentCertificate.setType(CertificateRelationTypeDTO.COPIED);
                        break;
                }

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
                    if (replacedBy.getStatus() == UtkastStatus.DRAFT_LOCKED) {
                        childCertificate.setStatus(CertificateStatusDTO.LOCKED_REVOKED);
                    } else {
                        childCertificate.setStatus(CertificateStatusDTO.REVOKED);
                    }
                } else {
                    switch (replacedBy.getStatus()) {
                        case SIGNED:
                            childCertificate.setStatus(CertificateStatusDTO.SIGNED);
                            break;
                        case DRAFT_LOCKED:
                            childCertificate.setStatus(CertificateStatusDTO.LOCKED);
                            break;
                        default:
                            childCertificate.setStatus(CertificateStatusDTO.UNSIGNED);
                    }
                }
                childCertificate.setType(CertificateRelationTypeDTO.REPLACED);
                certificateRelations.setChildren(new CertificateRelationDTO[]{childCertificate});
            }

            final var copiedBy = relations.getLatestChildRelations().getUtkastCopy();
            if (copiedBy != null) {
                final CertificateRelationDTO childCertificate = new CertificateRelationDTO();
                childCertificate.setCertificateId(copiedBy.getIntygsId());
                childCertificate.setCreated(copiedBy.getSkapad());
                if (copiedBy.isMakulerat()) {
                    if (copiedBy.getStatus() == UtkastStatus.DRAFT_LOCKED) {
                        childCertificate.setStatus(CertificateStatusDTO.LOCKED_REVOKED);
                    } else {
                        childCertificate.setStatus(CertificateStatusDTO.REVOKED);
                    }
                } else {
                    switch (copiedBy.getStatus()) {
                        case SIGNED:
                            childCertificate.setStatus(CertificateStatusDTO.SIGNED);
                            break;
                        case DRAFT_LOCKED:
                            childCertificate.setStatus(CertificateStatusDTO.LOCKED);
                            break;
                        default:
                            childCertificate.setStatus(CertificateStatusDTO.UNSIGNED);
                    }
                }
                childCertificate.setType(CertificateRelationTypeDTO.COPIED);
                certificateRelations.setChildren(new CertificateRelationDTO[]{childCertificate});
            }

            resourceLinkHelper.decorateCertificateWithValidActionLinks(certificateDTO);

            return certificateDTO;
        } catch (Exception ex) {
            LOG.error("Cannot convert certificate!", ex);
            throw new RuntimeException(ex);
        }
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
