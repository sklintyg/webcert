package se.inera.intyg.webcert.web.service.facade.util;

import static se.inera.intyg.webcert.web.service.facade.util.CertificateStatusConverter.getStatus;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;

@Component
public class IntygToCertificateConverterImpl implements IntygToCertificateConverter {

    private static final Logger LOG = LoggerFactory.getLogger(IntygToCertificateConverterImpl.class);

    private final IntygModuleRegistry moduleRegistry;

    private final IntygTextsService intygTextsService;

    private final PatientConverter patientConverter;

    private final CertificateRelationsConverter certificateRelationsConverter;

    @Autowired
    public IntygToCertificateConverterImpl(IntygModuleRegistry moduleRegistry,
        IntygTextsService intygTextsService,
        PatientConverter patientConverter,
        CertificateRelationsConverter certificateRelationsConverter) {
        this.moduleRegistry = moduleRegistry;
        this.intygTextsService = intygTextsService;
        this.patientConverter = patientConverter;
        this.certificateRelationsConverter = certificateRelationsConverter;
    }

    @Override
    public Certificate convert(IntygContentHolder intygContentHolder) {
        LOG.debug("Converting IntygContentHolder to Certificate");
        return convertToCertificate(intygContentHolder);
    }

    private Certificate convertToCertificate(IntygContentHolder certificate) {
        final var certificateToReturn = getCertificateToReturn(
            certificate.getUtlatande().getTyp(),
            certificate.getUtlatande().getTextVersion(),
            certificate.getContents()
        );

        certificateToReturn.getMetadata().setCreated(
            getSignedDate(certificate.getStatuses())
        );
        certificateToReturn.getMetadata().setVersion(99);
        certificateToReturn.getMetadata().setForwarded(false);
        certificateToReturn.getMetadata().setTestCertificate(certificate.isTestIntyg());
        certificateToReturn.getMetadata().setSent(
            certificate.getStatuses().stream().anyMatch(status -> status.getType() == CertificateState.SENT)
        );

        certificateToReturn.getMetadata().setCareProvider(
            getCareProvider(certificate.getUtlatande().getGrundData().getSkapadAv())
        );

        certificateToReturn.getMetadata().setStatus(
            getStatus(certificate.getStatuses())
        );

        certificateToReturn.getMetadata().setPatient(
            patientConverter.convert(
                certificate.getUtlatande().getGrundData().getPatient().getPersonId(),
                certificate.getUtlatande().getTyp(),
                certificate.getUtlatande().getTextVersion()
            )
        );

        certificateToReturn.getMetadata().setIssuedBy(
            getIssuedBy(certificate.getUtlatande().getGrundData().getSkapadAv())
        );

        certificateToReturn.getMetadata().setRelations(
            certificateRelationsConverter.convert(certificateToReturn.getMetadata().getId())
        );

        certificateToReturn.getMetadata().setLatestMajorVersion(
            intygTextsService.isLatestMajorVersion(certificateToReturn.getMetadata().getType(),
                certificateToReturn.getMetadata().getTypeVersion())
        );

        return certificateToReturn;
    }

    private Staff getIssuedBy(HoSPersonal skapadAv) {
        final var staff = new Staff();

        staff.setPersonId(skapadAv.getPersonId());
        staff.setFullName(skapadAv.getFullstandigtNamn());

        return staff;
    }

    private Unit getCareProvider(HoSPersonal skapadAv) {
        return Unit.builder()
            .unitId(skapadAv.getVardenhet().getVardgivare().getVardgivarid())
            .unitName(skapadAv.getVardenhet().getVardgivare().getVardgivarnamn())
            .build();
    }

    private LocalDateTime getSignedDate(List<Status> statuses) {
        return statuses.stream()
            .filter(status -> status.getType() == CertificateState.RECEIVED)
            .findFirst().orElseThrow().getTimestamp();
    }

    private Certificate getCertificateToReturn(String certificateType, String certificateTypeVersion, String jsonModel) {
        try {
            LOG.debug("Retrieving ModuleAPI for type '{}' version '{}'", certificateType, certificateTypeVersion);
            final var moduleApi = moduleRegistry.getModuleApi(certificateType, certificateTypeVersion);
            LOG.debug("Retrieving Certificate from Json");
            return moduleApi.getCertificateFromJson(jsonModel);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
