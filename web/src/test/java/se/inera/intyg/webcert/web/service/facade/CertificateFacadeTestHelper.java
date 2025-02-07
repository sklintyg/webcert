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
package se.inera.intyg.webcert.web.service.facade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.config.CertificateDataConfigCheckboxDateRangeList;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRange;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRangeList;

public class CertificateFacadeTestHelper {

    public static Certificate createCertificate(String certificateType, CertificateStatus status) {
        return createCertificateWithChildRelation(certificateType, status, (CertificateRelation) null);
    }

    public static Certificate createCertificate(String certificateType, CertificateStatus status, boolean addressFromPU) {
        return createCertificateWithChildRelation(certificateType, status, addressFromPU, (CertificateRelation) null);
    }

    public static Certificate createCertificateWithChildRelation(String certificateType, CertificateStatus status,
        CertificateRelation... relation) {
        return createCertificateWithChildRelation(certificateType, status, true, relation);
    }

    public static Certificate createCertificateTypeWithVersion(String certificateType, CertificateStatus status, boolean addressFromPU,
        String typeVersion) {
        return createCertificateCertainWithVersion(certificateType, status, addressFromPU, typeVersion);
    }

    private static Certificate createCertificateCertainWithVersion(String certificateType, CertificateStatus status, boolean addressFromPU,
        String typeVersion) {
        final var metadataBuilder = CertificateMetadata.builder()
            .id("certificateId")
            .type(certificateType)
            .typeVersion(typeVersion)
            .status(status)
            .signed(LocalDateTime.now())
            .modified(LocalDateTime.now().plusDays(5))
            .patient(
                Patient.builder()
                    .personId(
                        PersonId.builder()
                            .id("191212121212")
                            .build()
                    )
                    .addressFromPU(addressFromPU)
                    .build()
            )
            .unit(
                Unit.builder()
                    .unitId("unitId")
                    .unitName("unitName")
                    .address("address")
                    .zipCode("zipCode")
                    .city("city")
                    .email("email")
                    .phoneNumber("phoneNumber")
                    .build()
            )
            .careProvider(
                Unit.builder()
                    .unitId("careProviderId")
                    .unitName("careProviderName")
                    .address("address")
                    .zipCode("zipCode")
                    .city("city")
                    .email("email")
                    .phoneNumber("phoneNumber")
                    .build()
            ).careUnit(
                Unit.builder()
                    .unitId("unitId")
                    .unitName("unitName")
                    .address("address")
                    .zipCode("zipCode")
                    .city("city")
                    .email("email")
                    .phoneNumber("phoneNumber")
                    .build()
            ).issuedBy(Staff.builder()
                .fullName("fullName")
                .personId("personId")
                .prescriptionCode("prescriptionCode")
                .build()
            );

        return CertificateBuilder.create()
            .metadata(metadataBuilder.build())
            .build();
    }

    public static Certificate createCertificateWithChildRelation(String certificateType, CertificateStatus status,
        boolean addressFromPU, CertificateRelation... relation) {
        final var metadataBuilder = CertificateMetadata.builder()
            .id("certificateId")
            .type(certificateType)
            .typeVersion("1.2")
            .status(status)
            .patient(
                Patient.builder()
                    .personId(
                        PersonId.builder()
                            .id("191212121212")
                            .build()
                    )
                    .addressFromPU(addressFromPU)
                    .build()
            )
            .unit(
                Unit.builder()
                    .unitId("unitId")
                    .unitName("unitName")
                    .address("address")
                    .zipCode("zipCode")
                    .city("city")
                    .email("email")
                    .phoneNumber("phoneNumber")
                    .build()
            )
            .careProvider(
                Unit.builder()
                    .unitId("careProviderId")
                    .unitName("careProviderName")
                    .address("address")
                    .zipCode("zipCode")
                    .city("city")
                    .email("email")
                    .phoneNumber("phoneNumber")
                    .build()
            ).careUnit(
                Unit.builder().
                    unitId("unitId")
                    .unitName("unitName")
                    .address("address")
                    .zipCode("zipCode")
                    .city("city")
                    .email("email")
                    .phoneNumber("phoneNumber")
                    .build()
            ).issuedBy(Staff.builder()
                .fullName("fullName")
                .personId("personId")
                .prescriptionCode("prescriptionCode")
                .build()
            );
        ;

        if (relation != null && relation.length > 0 && relation[0] != null) {
            metadataBuilder.relations(
                CertificateRelations.builder()
                    .children(
                        relation
                    )
                    .build()
            );
        }

        return CertificateBuilder.create()
            .metadata(metadataBuilder.build())
            .build();
    }

    public static Certificate createCertificateWithParentRelation(String certificateType, CertificateStatus status,
        CertificateRelation parent) {
        final var metadataBuilder = CertificateMetadata.builder()
            .id("certificateId")
            .type(certificateType)
            .typeVersion("1.2")
            .status(status)
            .patient(
                Patient.builder()
                    .personId(
                        PersonId.builder()
                            .id("191212121212")
                            .build()
                    )
                    .build()
            )
            .unit(
                Unit.builder()
                    .unitId("unitId")
                    .unitName("unitName")
                    .address("address")
                    .zipCode("zipCode")
                    .city("city")
                    .email("email")
                    .phoneNumber("phoneNumber")
                    .build()
            )
            .careProvider(
                Unit.builder()
                    .unitId("careProviderId")
                    .unitName("careProviderName")
                    .address("address")
                    .zipCode("zipCode")
                    .city("city")
                    .email("email")
                    .phoneNumber("phoneNumber")
                    .build()
            );

        if (parent != null) {
            metadataBuilder.relations(
                CertificateRelations.builder()
                    .parent(parent)
                    .build()
            );
        }

        return CertificateBuilder.create()
            .metadata(metadataBuilder.build())
            .build();
    }

    public static Certificate createCertificateWithSickleavePeriod(int numberOfDays) {
        final var metadataBuilder = CertificateMetadata.builder()
            .id("certificateId")
            .type("lisjp")
            .typeVersion("1.2")
            .status(CertificateStatus.SIGNED)
            .patient(
                Patient.builder()
                    .personId(
                        PersonId.builder()
                            .id("191212121212")
                            .build()
                    )
                    .build()
            )
            .unit(
                Unit.builder()
                    .unitId("unitId")
                    .unitName("unitName")
                    .address("address")
                    .zipCode("zipCode")
                    .city("city")
                    .email("email")
                    .phoneNumber("phoneNumber")
                    .build()
            )
            .careProvider(
                Unit.builder()
                    .unitId("careProviderId")
                    .unitName("careProviderName")
                    .address("address")
                    .zipCode("zipCode")
                    .city("city")
                    .email("email")
                    .phoneNumber("phoneNumber")
                    .build()
            );

        final var sickLeavePeriod = CertificateDataElement.builder()
            .config(
                CertificateDataConfigCheckboxDateRangeList.builder().build()
            )
            .value(
                CertificateDataValueDateRangeList.builder()
                    .list(Arrays.asList(
                            CertificateDataValueDateRange.builder()
                                .from(LocalDate.now())
                                .to(LocalDate.now().plusDays(numberOfDays))
                                .build()
                        )
                    )
                    .build()
            )
            .build();

        return CertificateBuilder.create()
            .metadata(metadataBuilder.build())
            .addElement(sickLeavePeriod)
            .build();
    }
}
