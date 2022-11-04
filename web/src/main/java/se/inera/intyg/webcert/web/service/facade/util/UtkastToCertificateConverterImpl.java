/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
import static se.inera.intyg.webcert.web.service.facade.util.CertificateStatusConverter.isRevoked;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.facade.TypeAheadProvider;
import se.inera.intyg.infra.integration.hsatk.services.HsatkOrganizationService;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Component
public class UtkastToCertificateConverterImpl implements UtkastToCertificateConverter {

    private static final Logger LOG = LoggerFactory.getLogger(UtkastToCertificateConverterImpl.class);

    private final IntygModuleRegistry moduleRegistry;

    private final IntygTextsService intygTextsService;

    private final PatientConverter patientConverter;

    private final CertificateRelationsConverter certificateRelationsConverter;

    private final WebCertUserService webCertUserService;

    private final HsatkOrganizationService hsatkOrganizationService;

    private final TypeAheadProvider typeAheadProvider;

    @Autowired
    public UtkastToCertificateConverterImpl(IntygModuleRegistry moduleRegistry,
        IntygTextsService intygTextsService,
        PatientConverter patientConverter,
        CertificateRelationsConverter certificateRelationsConverter,
        WebCertUserService webCertUserService,
        HsatkOrganizationService hsatkOrganizationService,
        TypeAheadProvider typeAheadProvider) {
        this.moduleRegistry = moduleRegistry;
        this.intygTextsService = intygTextsService;
        this.patientConverter = patientConverter;
        this.certificateRelationsConverter = certificateRelationsConverter;
        this.webCertUserService = webCertUserService;
        this.hsatkOrganizationService = hsatkOrganizationService;
        this.typeAheadProvider = typeAheadProvider;
    }

    @Override
    public Certificate convert(Utkast certificate) {
        LOG.debug("Converting Utkast to Certificate");
        return convertToCertificate(certificate);
    }

    private Certificate convertToCertificate(Utkast certificate) {
        final var certificateToReturn = getCertificateToReturn(
            certificate.getIntygsTyp(),
            certificate.getIntygTypeVersion(),
            certificate.getModel()
        );

        certificateToReturn.getMetadata().setCreated(certificate.getSkapad());
        certificateToReturn.getMetadata().setVersion(certificate.getVersion());
        certificateToReturn.getMetadata().setForwarded(certificate.getVidarebefordrad());
        certificateToReturn.getMetadata().setReadyForSign(certificate.getKlartForSigneringDatum());
        certificateToReturn.getMetadata().setTestCertificate(certificate.isTestIntyg());
        certificateToReturn.getMetadata().setSent(certificate.getSkickadTillMottagareDatum() != null);

        certificateToReturn.getMetadata().setCareProvider(
            getCareProvider(certificate)
        );

        certificateToReturn.getMetadata().setCareUnit(
            getCareUnit(certificate)
        );

        certificateToReturn.getMetadata().setStatus(
            getStatus(isRevoked(certificate), certificate.getStatus())
        );

        certificateToReturn.getMetadata().setPatient(
            patientConverter.convert(
                certificateToReturn.getMetadata().getPatient(),
                certificate.getPatientPersonnummer(),
                certificate.getIntygsTyp(),
                certificate.getIntygTypeVersion()
            )
        );

        certificateToReturn.getMetadata().setRelations(
            certificateRelationsConverter.convert(certificateToReturn.getMetadata().getId())
        );

        certificateToReturn.getMetadata().setLatestMajorVersion(
            intygTextsService.isLatestMajorVersion(certificateToReturn.getMetadata().getType(),
                certificateToReturn.getMetadata().getTypeVersion())
        );

        certificateToReturn.getMetadata().setResponsibleHospName(
            getResponsibleHospName()
        );

        return certificateToReturn;
    }

    private Unit getCareProvider(Utkast certificate) {
        return Unit.builder()
            .unitId(certificate.getVardgivarId())
            .unitName(certificate.getVardgivarNamn())
            .build();
    }

    private Unit getCareUnit(Utkast certificate) {
        final var careUnitId = hsatkOrganizationService.getHealthCareUnit(certificate.getEnhetsId()).getHealthCareUnitHsaId();
        final var careUnit = careUnitId != null ? hsatkOrganizationService.getUnit(careUnitId, null) : null;
        return Unit.builder()
            .unitId(careUnitId != null ? careUnitId : certificate.getEnhetsId())
            .unitName(careUnit != null ? careUnit.getUnitName() : certificate.getEnhetsNamn())
            .build();
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

    private String getResponsibleHospName() {
        if (!webCertUserService.hasAuthenticationContext()) {
            return null;
        }

        final var integrationParameters = webCertUserService.getUser().getParameters();
        return integrationParameters != null ? integrationParameters.getResponsibleHospName() : null;
    }
}
