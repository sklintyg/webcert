/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelation;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateRelations;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.facade.impl.GetCertificateServiceImpl;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations.FrontendRelations;

@Component
public class CertificateConverterImpl implements CertificateConverter {

    private static final Logger LOG = LoggerFactory.getLogger(GetCertificateServiceImpl.class);

    private final IntygModuleRegistry moduleRegistry;

    private final CertificateRelationService certificateRelationService;

    @Autowired
    public CertificateConverterImpl(IntygModuleRegistry moduleRegistry, CertificateRelationService certificateRelationService) {
        this.moduleRegistry = moduleRegistry;
        this.certificateRelationService = certificateRelationService;
    }

    @Override
    public Certificate convert(Utkast certificate) {
        LOG.debug("Converting Utkast to Certificate");
        return convertToCertificate(certificate);
    }

    private Certificate convertToCertificate(Utkast certificate) {
        final var certificateToReturn = getCertificateToReturn(certificate);

        certificateToReturn.getMetadata().setCreated(certificate.getSkapad());
        certificateToReturn.getMetadata().setVersion(certificate.getVersion());
        certificateToReturn.getMetadata().setForwarded(certificate.getVidarebefordrad());
        certificateToReturn.getMetadata().setTestCertificate(certificate.isTestIntyg());

        certificateToReturn.getMetadata().setCareProvider(
            getCareProvider(certificate)
        );

        certificateToReturn.getMetadata().setStatus(
            getStatus(isRevoked(certificate), certificate.getStatus())
        );

        certificateToReturn.getMetadata().setPatient(
            getPatient(certificate)
        );

        certificateToReturn.getMetadata().setIssuedBy(
            getIssuedBy(certificate)
        );

        certificateToReturn.getMetadata().setRelations(
            getRelations(certificateToReturn.getMetadata().getId())
        );

        return certificateToReturn;
    }

    private Staff getIssuedBy(Utkast certificate) {
        final var staff = new Staff();

        staff.setPersonId(certificate.getSkapadAv().getHsaId());
        staff.setFullName(certificate.getSkapadAv().getNamn());

        return staff;
    }

    private Unit getCareProvider(Utkast certificate) {
        return Unit.builder()
            .unitId(certificate.getVardgivarId())
            .unitName(certificate.getVardgivarNamn())
            .build();
    }

    private CertificateRelations getRelations(String certificateId) {
        final var certificateRelations = new CertificateRelations();

        LOG.debug("Retrieving relations for certificate");
        final var relations = certificateRelationService.getRelations(certificateId);

        final var parent = getRelation(relations.getParent());
        certificateRelations.setParent(parent);

        final var childRelations = getChildRelations(relations.getLatestChildRelations());
        certificateRelations.setChildren(childRelations);

        return certificateRelations;
    }

    private CertificateRelation[] getChildRelations(FrontendRelations latestChildRelations) {
        final List<CertificateRelation> childRelations = new ArrayList<>();

        addRelationToListIfExists(childRelations, latestChildRelations.getReplacedByIntyg(), CertificateRelationType.REPLACED);
        addRelationToListIfExists(childRelations, latestChildRelations.getReplacedByUtkast(), CertificateRelationType.REPLACED);
        addRelationToListIfExists(childRelations, latestChildRelations.getComplementedByIntyg(), CertificateRelationType.COMPLEMENTED);
        addRelationToListIfExists(childRelations, latestChildRelations.getComplementedByUtkast(), CertificateRelationType.COMPLEMENTED);
        addRelationToListIfExists(childRelations, latestChildRelations.getUtkastCopy(), CertificateRelationType.COPIED);

        return childRelations.toArray(new CertificateRelation[0]);
    }

    private void addRelationToListIfExists(List<CertificateRelation> childRelations, WebcertCertificateRelation relation,
        CertificateRelationType relationType) {
        final var childRelation = getRelation(relation, relationType);

        if (childRelation != null) {
            childRelations.add(childRelation);
        }
    }

    private CertificateRelation getRelation(WebcertCertificateRelation relation) {
        if (relation == null) {
            return null;
        }
        return getRelation(relation, getType(relation.getRelationKod()));
    }

    private CertificateRelation getRelation(WebcertCertificateRelation relation, CertificateRelationType type) {
        if (relation == null) {
            return null;
        }
        final var certificateRelation = new CertificateRelation();
        certificateRelation.setCertificateId(relation.getIntygsId());
        certificateRelation.setCreated(relation.getSkapad());
        certificateRelation.setStatus(
            getStatus(relation.isMakulerat(), relation.getStatus())
        );
        certificateRelation.setType(type);
        return certificateRelation;
    }

    private CertificateRelationType getType(RelationKod relationCode) {
        switch (relationCode) {
            case ERSATT:
                return CertificateRelationType.REPLACED;
            case KOPIA:
                return CertificateRelationType.COPIED;
            case KOMPLT:
                return CertificateRelationType.COMPLEMENTED;
            case FRLANG:
                return CertificateRelationType.EXTENDED;
            default:
                throw new IllegalArgumentException("Cannot map the relation code: " + relationCode);
        }
    }

    private CertificateStatus getStatus(boolean isRevoked, UtkastStatus status) {
        if (isRevoked) {
            if (status == UtkastStatus.DRAFT_LOCKED) {
                return CertificateStatus.LOCKED_REVOKED;
            } else {
                return CertificateStatus.REVOKED;
            }
        }

        switch (status) {
            case SIGNED:
                return CertificateStatus.SIGNED;
            case DRAFT_LOCKED:
                return CertificateStatus.LOCKED;
            case DRAFT_COMPLETE:
            case DRAFT_INCOMPLETE:
                return CertificateStatus.UNSIGNED;
            default:
                throw new IllegalArgumentException("Cannot map the status: " + status);
        }
    }

    private Patient getPatient(Utkast certificate) {
        final var patient = new Patient();

        patient.setPersonId(new PersonId());
        patient.getPersonId().setId(certificate.getPatientPersonnummer().getPersonnummer());
        patient.getPersonId().setType("PERSON_NUMMER");
        patient.setFirstName(certificate.getPatientFornamn());
        patient.setMiddleName(certificate.getPatientMellannamn());
        patient.setLastName(certificate.getPatientEfternamn());
        if (Objects.nonNull(certificate.getPatientMellannamn()) && certificate.getPatientMellannamn().trim().length() > 0) {
            patient.setFullName(
                certificate.getPatientFornamn() + ' ' + certificate.getPatientMellannamn() + ' ' + certificate.getPatientEfternamn()
            );
        } else {
            patient.setFullName(
                certificate.getPatientFornamn() + ' ' + certificate.getPatientEfternamn()
            );
        }

        return patient;
    }

    private boolean isRevoked(Utkast certificate) {
        return certificate.getAterkalladDatum() != null;
    }

    private Certificate getCertificateToReturn(Utkast certificate) {
        try {
            LOG.debug("Retrieving ModuleAPI for type '{}' version '{}'", certificate.getIntygsTyp(), certificate.getIntygTypeVersion());
            final var moduleApi = moduleRegistry.getModuleApi(certificate.getIntygsTyp(), certificate.getIntygTypeVersion());
            LOG.debug("Retrieving Certificate from Json");
            return moduleApi.getCertificateFromJson(certificate.getModel());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
