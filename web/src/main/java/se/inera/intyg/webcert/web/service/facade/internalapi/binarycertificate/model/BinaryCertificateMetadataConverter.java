/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model;

import java.util.List;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.CVType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Specialistkompetens;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@Component
public class BinaryCertificateMetadataConverter {

  public BinaryCertificateMetadataDTO toBinaryCertificate(Intyg intyg, Certificate certificate) {
    final var metadata = certificate.getMetadata();
    return BinaryCertificateMetadataDTO.builder()
        .certificateId(metadata.getId())
        .type(
            BinaryCertificateTypeDTO.builder()
                .code(metadata.getType())
                .codeSystem(intyg.getTyp().getCodeSystem())
                .displayName(metadata.getTypeName())
                .build())
        .version(metadata.getTypeVersion())
        .sentAt(intyg.getSkickatTidpunkt())
        .signedAt(metadata.getSigned())
        .revokedAt(metadata.getRevokedAt())
        .patient(metadata.getPatient())
        .relations(metadata.getRelations())
        .issuedBy(toIssuedBy(intyg.getSkapadAv(), metadata))
        .build();
  }

  private BinaryStaffDTO toIssuedBy(HosPersonal skapadAv, CertificateMetadata metadata) {
    return BinaryStaffDTO.builder()
        .personId(metadata.getIssuedBy().getPersonId())
        .fullName(metadata.getIssuedBy().getFullName())
        .titles(toTypes(skapadAv.getBefattning()))
        .specialities(
            skapadAv.getSpecialistkompetens().stream()
                .map(Specialistkompetens::getDisplayName)
                .toList())
        .licences(toTypes(skapadAv.getLegitimeratYrke()))
        .unit(toUnit(metadata.getUnit(), metadata.getCareProvider()))
        .build();
  }

  private BinaryUnitDTO toUnit(Unit unit, Unit careProvider) {
    return BinaryUnitDTO.builder()
        .unitId(unit.getUnitId())
        .unitName(unit.getUnitName())
        .address(unit.getAddress())
        .zipCode(unit.getZipCode())
        .city(unit.getCity())
        .phoneNumber(unit.getPhoneNumber())
        .workplaceCode(unit.getWorkplaceCode())
        .email(unit.getEmail())
        .careProvider(
            BinaryCareProviderDTO.builder()
                .unitId(careProvider.getUnitId())
                .unitName(careProvider.getUnitName())
                .build())
        .build();
  }

  private List<BinaryCertificateTypeDTO> toTypes(List<? extends CVType> types) {
    return types.stream()
        .map(
            type ->
                BinaryCertificateTypeDTO.builder()
                    .code(type.getCode())
                    .codeSystem(type.getCodeSystem())
                    .displayName(type.getDisplayName())
                    .build())
        .toList();
  }
}
