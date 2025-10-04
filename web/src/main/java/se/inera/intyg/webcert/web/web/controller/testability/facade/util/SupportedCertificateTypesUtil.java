/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.testability.facade.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.af00213.support.Af00213EntryPoint;
import se.inera.intyg.common.ag114.support.Ag114EntryPoint;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.webcert.web.csintegration.testability.CSTestabilityIntegrationService;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CertificateType;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

@Component
public class SupportedCertificateTypesUtil {

    private final CertificateServiceProfile certificateServiceProfile;
    private final CSTestabilityIntegrationService csTestabilityIntegrationService;


    public SupportedCertificateTypesUtil(CertificateServiceProfile certificateServiceProfile,
        CSTestabilityIntegrationService csTestabilityIntegrationService) {
        this.certificateServiceProfile = certificateServiceProfile;
        this.csTestabilityIntegrationService = csTestabilityIntegrationService;
    }

    public List<CertificateType> get() {
        final var certificateTypes = new ArrayList<CertificateType>();
        certificateTypes.add(
            new CertificateType(
                Af00213EntryPoint.ISSUER_TYPE_ID,
                Af00213EntryPoint.MODULE_ID,
                Af00213EntryPoint.MODULE_NAME,
                Collections.singletonList("1.0"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL, CreateCertificateFillType.MAXIMAL)
            )
        );
        certificateTypes.add(
            new CertificateType(
                LisjpEntryPoint.ISSUER_TYPE_ID,
                LisjpEntryPoint.MODULE_ID,
                LisjpEntryPoint.MODULE_NAME,
                Arrays.asList("1.0", "1.1", "1.2", "1.3"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL, CreateCertificateFillType.MAXIMAL)
            )
        );
        certificateTypes.add(
            new CertificateType(
                Ag7804EntryPoint.ISSUER_TYPE_ID,
                Ag7804EntryPoint.MODULE_ID,
                Ag7804EntryPoint.MODULE_NAME,
                Arrays.asList("1.0", "1.1", "1.2"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL, CreateCertificateFillType.MAXIMAL)
            )
        );
        certificateTypes.add(
            new CertificateType(
                DbModuleEntryPoint.ISSUER_TYPE_ID,
                DbModuleEntryPoint.MODULE_ID,
                DbModuleEntryPoint.MODULE_NAME,
                Collections.singletonList("1.0"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL, CreateCertificateFillType.MAXIMAL)
            )
        );
        certificateTypes.add(
            new CertificateType(
                DoiModuleEntryPoint.ISSUER_TYPE_ID,
                DoiModuleEntryPoint.MODULE_ID,
                DoiModuleEntryPoint.MODULE_NAME,
                Collections.singletonList("1.0"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL, CreateCertificateFillType.MAXIMAL)
            )
        );
        certificateTypes.add(
            new CertificateType(
                LuaenaEntryPoint.ISSUER_TYPE_ID,
                LuaenaEntryPoint.MODULE_ID,
                LuaenaEntryPoint.MODULE_NAME,
                Collections.singletonList("1.2"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL, CreateCertificateFillType.MAXIMAL)
            )
        );

        certificateTypes.add(
            new CertificateType(
                LuaefsEntryPoint.ISSUER_TYPE_ID,
                LuaefsEntryPoint.MODULE_ID,
                LuaefsEntryPoint.MODULE_NAME,
                Collections.singletonList("1.1"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL, CreateCertificateFillType.MAXIMAL)
            )
        );

        certificateTypes.add(
            new CertificateType(
                TsBasEntryPoint.KV_UTLATANDETYP_INTYG_CODE,
                TsBasEntryPoint.MODULE_ID,
                TsBasEntryPoint.MODULE_NAME,
                Arrays.asList("6.8", "7.0"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL, CreateCertificateFillType.MAXIMAL)
            )
        );

        certificateTypes.add(
            new CertificateType(
                LuseEntryPoint.ISSUER_TYPE_ID,
                LuseEntryPoint.MODULE_ID,
                LuseEntryPoint.MODULE_NAME,
                Collections.singletonList("1.3"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL, CreateCertificateFillType.MAXIMAL)
            )
        );

        certificateTypes.add(
            new CertificateType(
                Fk7263EntryPoint.ISSUER_TYPE_ID,
                Fk7263EntryPoint.MODULE_ID,
                Fk7263EntryPoint.MODULE_NAME,
                Collections.singletonList("1.0"),
                Arrays.asList(CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL, CreateCertificateFillType.MAXIMAL)
            )
        );

        certificateTypes.add(
            new CertificateType(
                TsDiabetesEntryPoint.KV_UTLATANDETYP_INTYG_CODE,
                TsDiabetesEntryPoint.MODULE_ID,
                TsDiabetesEntryPoint.MODULE_NAME,
                Arrays.asList("2.6", "2.8", "3.0", "4.0", "4.1", "4.2"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL, CreateCertificateFillType.MAXIMAL)
            )
        );

        certificateTypes.add(
            new CertificateType(
                Ag114EntryPoint.ISSUER_TYPE_ID,
                Ag114EntryPoint.MODULE_ID,
                Ag114EntryPoint.MODULE_NAME,
                Collections.singletonList("1.0"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL, CreateCertificateFillType.MAXIMAL)
            )
        );

        if (certificateServiceProfile.active()) {
            certificateTypes.addAll(
                csTestabilityIntegrationService.getSupportedTypes()
            );
        }

        return certificateTypes.stream()
            .collect(Collectors.toMap(
                cert -> cert.getType().replaceAll("\\s", "").toLowerCase(),
                cert -> cert,
                this::mergeCertificateType
            ))
            .values()
            .stream()
            .sorted(Comparator.comparing(CertificateType::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();

    }

  private CertificateType mergeCertificateType(CertificateType certificateTypeWC, CertificateType certificateTypeCS) {
    final var mergedVersions = Stream.concat(
        certificateTypeWC.getVersions().stream(),
        certificateTypeCS.getVersions().stream())
        .filter(Objects::nonNull)
        .distinct();

    return new CertificateType(
        certificateTypeWC.getType(),
        certificateTypeWC.getInternalType(),
        certificateTypeWC.getName(),
        mergedVersions.toList(),
        certificateTypeWC.getStatuses(),
        certificateTypeWC.getFillType()
    );
  }
}
