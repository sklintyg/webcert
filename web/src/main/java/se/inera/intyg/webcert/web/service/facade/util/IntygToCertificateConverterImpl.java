/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.util;

import static se.inera.intyg.webcert.web.service.facade.util.CertificateStatusConverter.getStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.facade.TypeAheadProvider;
import se.inera.intyg.infra.integration.hsatk.services.HsatkOrganizationService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Component
public class IntygToCertificateConverterImpl implements IntygToCertificateConverter {

    private static final Logger LOG = LoggerFactory.getLogger(IntygToCertificateConverterImpl.class);
    private static final long DEFAULT_CERTIFICATE_VERSION = 99;

    private final IntygModuleRegistry moduleRegistry;

    private final IntygTextsService intygTextsService;

    private final PatientConverter patientConverter;

    private final CertificateRelationsConverter certificateRelationsConverter;

    private final HsatkOrganizationService hsatkOrganizationService;

    private final TypeAheadProvider typeAheadProvider;

    private final CertificateRecipientConverter certificateRecipientConverter;

    @Autowired
    public IntygToCertificateConverterImpl(IntygModuleRegistry moduleRegistry,
        IntygTextsService intygTextsService,
        PatientConverter patientConverter,
        CertificateRelationsConverter certificateRelationsConverter,
        HsatkOrganizationService hsatkOrganizationService,
        TypeAheadProvider typeAheadProvider,
        CertificateRecipientConverter certificateRecipientConverter, WebCertUserService webCertUserService) {
        this.moduleRegistry = moduleRegistry;
        this.intygTextsService = intygTextsService;
        this.patientConverter = patientConverter;
        this.certificateRelationsConverter = certificateRelationsConverter;
        this.hsatkOrganizationService = hsatkOrganizationService;
        this.typeAheadProvider = typeAheadProvider;
        this.certificateRecipientConverter = certificateRecipientConverter;
    }

    @Override
    public Certificate convert(IntygContentHolder intygContentHolder) {
        LOG.debug("Converting IntygContentHolder to Certificate");
        return convertToCertificate(intygContentHolder);
    }

    private Certificate convertToCertificate(IntygContentHolder certificate) {
        final var sentStatus = certificate.getStatuses()
            .stream()
            .filter(status -> status.getType() == CertificateState.SENT)
            .findFirst();

        final var certificateToReturn = getCertificateToReturn(
            certificate.getUtlatande().getTyp(),
            certificate.getUtlatande().getTextVersion(),
            certificate.getContents()
        );

        certificateToReturn.getMetadata().setCreated(
            certificate.getUtlatande().getGrundData().getSigneringsdatum()
        );
        certificateToReturn.getMetadata().setVersion(DEFAULT_CERTIFICATE_VERSION);
        certificateToReturn.getMetadata().setForwarded(false);
        certificateToReturn.getMetadata().setTestCertificate(certificate.isTestIntyg());

        certificateToReturn.getMetadata().setRecipient(
            certificateRecipientConverter.get(
                certificate.getUtlatande().getTyp(),
                certificate.getUtlatande().getId(),
                sentStatus.map(Status::getTimestamp).orElse(null))
        );

        certificateToReturn.getMetadata().setSent(sentStatus.isPresent());
        certificateToReturn.getMetadata().setSentTo(
            certificateToReturn.getMetadata().getRecipient() != null
                ? certificateToReturn.getMetadata().getRecipient().getName() : null
        );

        certificateToReturn.getMetadata().setCareProvider(
            getCareProvider(certificate.getUtlatande().getGrundData().getSkapadAv())
        );

        certificateToReturn.getMetadata().setCareUnit(
            getCareUnit(certificate.getUtlatande().getGrundData().getSkapadAv().getVardenhet())
        );

        certificateToReturn.getMetadata().setStatus(
            getStatus(certificate.getStatuses())
        );

        certificateToReturn.getMetadata().setPatient(
            patientConverter.convert(
                certificateToReturn.getMetadata().getPatient(),
                certificate.getUtlatande().getGrundData().getPatient().getPersonId(),
                certificate.getUtlatande().getTyp(),
                certificate.getUtlatande().getTextVersion()
            )
        );

        certificateToReturn.getMetadata().setRelations(
            certificateRelationsConverter.convert(certificateToReturn.getMetadata().getId())
        );

        certificateToReturn.getMetadata().setLatestMajorVersion(
            intygTextsService.isLatestMajorVersion(certificateToReturn.getMetadata().getType(),
                certificateToReturn.getMetadata().getTypeVersion())
        );
        certificateToReturn.getMetadata().setAvailableForCitizen(
            !(certificate.getUtlatande().getTyp().equals(DbModuleEntryPoint.MODULE_ID)
                || certificate.getUtlatande().getTyp().equals(DoiModuleEntryPoint.MODULE_ID))
        );

        return certificateToReturn;
    }

    private Unit getCareProvider(HoSPersonal skapadAv) {
        return Unit.builder()
            .unitId(skapadAv.getVardenhet().getVardgivare().getVardgivarid())
            .unitName(skapadAv.getVardenhet().getVardgivare().getVardgivarnamn())
            .build();
    }

    private Unit getCareUnit(Vardenhet unit) {
        try {
            final var careUnitId = hsatkOrganizationService.getHealthCareUnit(unit.getEnhetsid()).getHealthCareUnitHsaId();
            final var careUnit = careUnitId != null ? hsatkOrganizationService.getUnit(careUnitId, null) : null;

            return Unit.builder()
                .unitId(careUnitId != null ? careUnitId : unit.getEnhetsid())
                .unitName(careUnit != null ? careUnit.getUnitName() : unit.getEnhetsnamn())
                .build();

        } catch (Exception e) {
            LOG.warn("Could not get unit from hsa", e);

            return Unit.builder()
                .unitId(unit.getEnhetsid())
                .unitName(unit.getEnhetsnamn())
                .build();
        }
    }

    private Certificate getCertificateToReturn(String certificateType, String certificateTypeVersion, String jsonModel) {
        try {
            LOG.debug("Retrieving ModuleAPI for type '{}' version '{}'", certificateType, certificateTypeVersion);
            final var moduleApi = moduleRegistry.getModuleApi(certificateType, certificateTypeVersion);
            LOG.debug("Retrieving Certificate from Json");
            return moduleApi.getCertificateFromJson(jsonModel, typeAheadProvider);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
