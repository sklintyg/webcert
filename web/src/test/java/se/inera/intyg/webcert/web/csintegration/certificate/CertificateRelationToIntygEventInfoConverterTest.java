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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent.Source;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEventType;

@ExtendWith(MockitoExtension.class)
class CertificateRelationToIntygEventInfoConverterTest {

    private static final String STAFF_NAME = "Dr. John Doe";
    private static final String STAFF_ID = "1234567890";
    private static final String CERTIFICATE_ID = "CERTIFICATE_ID";

    @InjectMocks
    private CertificateRelationToIntygEventInfoConverter certificateRelationToIntygEventInfoConverter;

    @Nested
    class WithRelatedCertificate {

        private Certificate createRelatedCertificate() {
            final var certificate = new Certificate();
            final var metadata = CertificateMetadata.builder()
                .issuedBy(Staff.builder()
                    .fullName(STAFF_NAME)
                    .personId(STAFF_ID)
                    .build())
                .build();
            certificate.setMetadata(metadata);
            return certificate;
        }

        @Test
        void shouldConvertExtendedRelation() {
            final var certificateRelation = CertificateRelation.builder()
                .type(CertificateRelationType.EXTENDED)
                .created(LocalDateTime.now())
                .certificateId(CERTIFICATE_ID)
                .build();
            final var relatedCertificate = createRelatedCertificate();
            final var expected = new IntygInfoEvent(Source.WEBCERT, certificateRelation.getCreated(), IntygInfoEventType.IS007);
            expected.addData("intygsId", CERTIFICATE_ID);
            expected.addData("name", STAFF_NAME);
            expected.addData("hsaId", STAFF_ID);

            final var result = certificateRelationToIntygEventInfoConverter.convert(certificateRelation, relatedCertificate);

            assertEquals(
                expected,
                result
            );
        }

        @Test
        void shouldConvertComplementedEvent() {
            final var certificateRelation = CertificateRelation.builder()
                .type(CertificateRelationType.COMPLEMENTED)
                .created(LocalDateTime.now())
                .certificateId(CERTIFICATE_ID)
                .build();
            final var relatedCertificate = createRelatedCertificate();
            final var expected = new IntygInfoEvent(Source.WEBCERT, certificateRelation.getCreated(), IntygInfoEventType.IS014);
            expected.addData("intygsId", CERTIFICATE_ID);
            expected.addData("name", STAFF_NAME);
            expected.addData("hsaId", STAFF_ID);

            final var result = certificateRelationToIntygEventInfoConverter.convert(certificateRelation, relatedCertificate);

            assertEquals(
                expected,
                result
            );
        }

        @Test
        void shouldConvertReplacedEvent() {
            final var certificateRelation = CertificateRelation.builder()
                .type(CertificateRelationType.REPLACED)
                .created(LocalDateTime.now())
                .certificateId(CERTIFICATE_ID)
                .build();
            final var relatedCertificate = createRelatedCertificate();
            final var expected = new IntygInfoEvent(Source.WEBCERT, certificateRelation.getCreated(), IntygInfoEventType.IS008);
            expected.addData("intygsId", CERTIFICATE_ID);
            expected.addData("name", STAFF_NAME);
            expected.addData("hsaId", STAFF_ID);

            final var result = certificateRelationToIntygEventInfoConverter.convert(certificateRelation, relatedCertificate);

            assertEquals(
                expected,
                result
            );
        }

        @Test
        void shouldConvertCopiedEvent() {
            final var certificateRelation = CertificateRelation.builder()
                .type(CertificateRelationType.COPIED)
                .created(LocalDateTime.now())
                .certificateId(CERTIFICATE_ID)
                .build();
            final var relatedCertificate = createRelatedCertificate();
            final var expected = new IntygInfoEvent(Source.WEBCERT, certificateRelation.getCreated(), IntygInfoEventType.IS026);
            expected.addData("intygsId", CERTIFICATE_ID);
            expected.addData("name", STAFF_NAME);
            expected.addData("hsaId", STAFF_ID);

            final var result = certificateRelationToIntygEventInfoConverter.convert(certificateRelation, relatedCertificate);

            assertEquals(
                expected,
                result
            );
        }
    }

    @Nested
    class NoRelatedCertificate {

        @Test
        void shouldConvertExtendedRelation() {
            final var certificateRelation = CertificateRelation.builder()
                .type(CertificateRelationType.EXTENDED)
                .created(LocalDateTime.now())
                .build();
            final var expected = new IntygInfoEvent(Source.WEBCERT, certificateRelation.getCreated(), IntygInfoEventType.IS007);

            final var result = certificateRelationToIntygEventInfoConverter.convert(certificateRelation, null);

            assertEquals(
                expected,
                result
            );
        }

        @Test
        void shouldConvertComplementedEvent() {
            final var certificateRelation = CertificateRelation.builder()
                .type(CertificateRelationType.COMPLEMENTED)
                .created(LocalDateTime.now())
                .build();
            final var expected = new IntygInfoEvent(Source.WEBCERT, certificateRelation.getCreated(), IntygInfoEventType.IS014);

            final var result = certificateRelationToIntygEventInfoConverter.convert(certificateRelation, null);

            assertEquals(
                expected,
                result
            );
        }

        @Test
        void shouldConvertReplacedEvent() {
            final var certificateRelation = CertificateRelation.builder()
                .type(CertificateRelationType.REPLACED)
                .created(LocalDateTime.now())
                .build();
            final var expected = new IntygInfoEvent(Source.WEBCERT, certificateRelation.getCreated(), IntygInfoEventType.IS008);

            final var result = certificateRelationToIntygEventInfoConverter.convert(certificateRelation, null);

            assertEquals(
                expected,
                result
            );
        }

        @Test
        void shouldConvertCopiedEvent() {
            final var certificateRelation = CertificateRelation.builder()
                .type(CertificateRelationType.COPIED)
                .created(LocalDateTime.now())
                .build();
            final var expected = new IntygInfoEvent(Source.WEBCERT, certificateRelation.getCreated(), IntygInfoEventType.IS026);

            final var result = certificateRelationToIntygEventInfoConverter.convert(certificateRelation, null);

            assertEquals(
                expected,
                result
            );
        }
    }
}