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

package se.inera.intyg.webcert.web.service.facade;

import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.metadata.Unit;

public class CertificateFacadeTestHelper {

    public static Certificate createCertificate(String certificateType, CertificateStatus status) {
        return createCertificate(certificateType, status, null);
    }

    public static Certificate createCertificate(String certificateType, CertificateStatus status, CertificateRelation relation) {
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

        if (relation != null) {
            metadataBuilder.relations(
                CertificateRelations.builder()
                    .children(
                        new CertificateRelation[]{
                            relation
                        }
                    )
                    .build()
            );
        }

        return CertificateBuilder.create()
            .metadata(metadataBuilder.build())
            .build();
    }
}
