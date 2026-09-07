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
package se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate;

import static se.inera.intyg.common.support.Constants.BEFATTNING_KOD_OID;
import static se.inera.intyg.common.support.Constants.KV_INTYGSTYP_CODE_SYSTEM;
import static se.inera.intyg.common.support.Constants.KV_UTLATANDETYP_INTYG_CODE_SYSTEM;
import static se.inera.intyg.common.support.model.CertificateState.CANCELLED;
import static se.inera.intyg.common.support.model.CertificateState.RECEIVED;
import static se.inera.intyg.common.support.model.CertificateState.SENT;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.PaTitle;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.validate.SamordningsnummerValidator;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.dto.PersonIdType;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.BinaryCertificateCareProviderDTO;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.BinaryCertificateCodeDTO;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.BinaryCertificateMetadataDTO;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.BinaryCertificatePatientDTO;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.BinaryCertificateRelationDTO;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.BinaryCertificateStaffDTO;
import se.inera.intyg.webcert.web.service.facade.internalapi.binarycertificate.model.BinaryCertificateUnitDTO;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

@Component
public class BinaryCertificateMetadataConverter {

  public static final String FK_7263 = "FK7263";

  public BinaryCertificateMetadataDTO toBinaryCertificate(
      IntygContentHolder content,
      ModuleEntryPoint entryPoint,
      IntygContentHolder parentCertificate) {

    final var utlatande = content.getUtlatande();
    final var statuses = content.getStatuses();
    final var skapadAv = utlatande.getGrundData().getSkapadAv();
    final var patientId = utlatande.getGrundData().getPatient().getPersonId().getPersonnummer();

    return BinaryCertificateMetadataDTO.builder()
        .certificateId(utlatande.getId())
        .type(toType(entryPoint))
        .version(utlatande.getTextVersion())
        .patient(toPatient(patientId))
        .issuedBy(toIssuedBy(skapadAv))
        .signedAt(toStatusValue(statuses, RECEIVED, Status::getTimestamp))
        .sentAt(toStatusValue(statuses, SENT, Status::getTimestamp))
        .recipientId(toStatusValue(statuses, SENT, Status::getTarget))
        .revokedAt(toStatusValue(statuses, CANCELLED, Status::getTimestamp))
        .parentRelation(toRelation(content, parentCertificate))
        .build();
  }

  private BinaryCertificateCodeDTO toType(ModuleEntryPoint entryPoint) {
    return BinaryCertificateCodeDTO.builder()
        .code(entryPoint.getExternalId())
        .codeSystem(toTypeCodeSystem(entryPoint.getExternalId()))
        .displayName(entryPoint.getModuleName())
        .build();
  }

  private String toTypeCodeSystem(String externalId) {
    return switch (externalId) {
      case TsDiabetesEntryPoint.KV_UTLATANDETYP_INTYG_CODE,
          TsBasEntryPoint.KV_UTLATANDETYP_INTYG_CODE,
          FK_7263 ->
          KV_UTLATANDETYP_INTYG_CODE_SYSTEM;
      default -> KV_INTYGSTYP_CODE_SYSTEM;
    };
  }

  private BinaryCertificatePatientDTO toPatient(String patientId) {
    return BinaryCertificatePatientDTO.builder()
        .patientId(patientId)
        .type(toPatientIdType(patientId))
        .build();
  }

  private PersonIdType toPatientIdType(String patientId) {
    final var personnummer = Personnummer.createPersonnummer(patientId).orElseThrow();
    return SamordningsnummerValidator.isSamordningsNummer(Optional.of(personnummer))
        ? PersonIdType.COORDINATION_NUMBER
        : PersonIdType.PERSONAL_IDENTITY_NUMBER;
  }

  private BinaryCertificateStaffDTO toIssuedBy(HoSPersonal skapadAv) {
    return BinaryCertificateStaffDTO.builder()
        .personId(skapadAv.getPersonId())
        .fullName(skapadAv.getFullstandigtNamn())
        .titles(toTitles(skapadAv.getBefattningsKoder()))
        .specialities(skapadAv.getSpecialiteter().stream().toList())
        .licences(Collections.emptyList())
        .unit(toUnit(skapadAv.getVardenhet()))
        .build();
  }

  private List<BinaryCertificateCodeDTO> toTitles(List<PaTitle> types) {
    return types.stream()
        .map(
            type ->
                BinaryCertificateCodeDTO.builder()
                    .code(type.getKod())
                    .codeSystem(BEFATTNING_KOD_OID)
                    .displayName(type.getKlartext())
                    .build())
        .toList();
  }

  private BinaryCertificateUnitDTO toUnit(Vardenhet unit) {
    return BinaryCertificateUnitDTO.builder()
        .unitId(unit.getEnhetsid())
        .unitName(unit.getEnhetsnamn())
        .address(unit.getPostadress())
        .zipCode(unit.getPostnummer())
        .city(unit.getPostort())
        .phoneNumber(unit.getTelefonnummer())
        .workplaceCode(unit.getArbetsplatsKod())
        .email(unit.getEpost())
        .careProvider(
            BinaryCertificateCareProviderDTO.builder()
                .unitId(unit.getVardgivare().getVardgivarid())
                .unitName(unit.getVardgivare().getVardgivarnamn())
                .build())
        .build();
  }

  private <T> T toStatusValue(
      List<Status> statuses, CertificateState state, Function<Status, T> mapper) {
    return toStatus(statuses, state).map(mapper).orElse(null);
  }

  private Optional<Status> toStatus(List<Status> statuses, CertificateState state) {
    return statuses.stream().filter(status -> status.getType() == state).findFirst();
  }

  private BinaryCertificateRelationDTO toRelation(
      IntygContentHolder content, IntygContentHolder parentCertificate) {
    if (parentCertificate == null) {
      return null;
    }

    final var parentIssuingUnit =
        parentCertificate.getUtlatande().getGrundData().getSkapadAv().getVardenhet().getEnhetsid();
    return Optional.ofNullable(content.getRelations())
        .map(Relations::getParent)
        .map(
            parent ->
                BinaryCertificateRelationDTO.builder()
                    .certificateId(parent.getIntygsId())
                    .issuingUnitId(parentIssuingUnit)
                    .type(toRelationType(parent.getRelationKod()))
                    .build())
        .orElse(null);
  }

  private CertificateRelationType toRelationType(RelationKod code) {
    return switch (code) {
      case ERSATT -> CertificateRelationType.REPLACED;
      case FRLANG -> CertificateRelationType.EXTENDED;
      case KOPIA -> CertificateRelationType.COPIED;
      case KOMPLT -> CertificateRelationType.COMPLEMENTED;
    };
  }
}
