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

package se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;

class AvailableFunctionUtilsTest {

    @Nested
    class IsReplacedOrComplemented {

        @Test
        void shouldReturnFalseIfRelationsIsNull() {
            final var response = AvailableFunctionUtils.isReplacedOrComplemented(null);

            assertFalse(response);
        }

        @Test
        void shouldReturnFalseIfRelationsHasNoChildren() {
            final var response = AvailableFunctionUtils.isReplacedOrComplemented(
                CertificateRelations.builder().build()
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnTrueIfRelationsHasComplementedChildThatIsSigned() {
            CertificateRelation[] children = {
                CertificateRelation.builder()
                    .type(CertificateRelationType.COMPLEMENTED)
                    .status(CertificateStatus.SIGNED)
                    .build()
            };
            final var response = AvailableFunctionUtils.isReplacedOrComplemented(
                CertificateRelations.builder().children(children).build()
            );

            assertTrue(response);
        }

        @Test
        void shouldReturnFalseIfRelationsHasComplementedChildThatIsUnsigned() {
            CertificateRelation[] children = {
                CertificateRelation.builder()
                    .type(CertificateRelationType.COMPLEMENTED)
                    .status(CertificateStatus.UNSIGNED)
                    .build()
            };
            final var response = AvailableFunctionUtils.isReplacedOrComplemented(
                CertificateRelations.builder().children(children).build()
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnFalseIfRelationsHasComplementedChildThatIsLocked() {
            CertificateRelation[] children = {
                CertificateRelation.builder()
                    .type(CertificateRelationType.COMPLEMENTED)
                    .status(CertificateStatus.LOCKED)
                    .build()
            };
            final var response = AvailableFunctionUtils.isReplacedOrComplemented(
                CertificateRelations.builder().children(children).build()
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnFalseIfRelationsHasComplementedChildThatIsLockedRevoked() {
            CertificateRelation[] children = {
                CertificateRelation.builder()
                    .type(CertificateRelationType.COMPLEMENTED)
                    .status(CertificateStatus.LOCKED_REVOKED)
                    .build()
            };
            final var response = AvailableFunctionUtils.isReplacedOrComplemented(
                CertificateRelations.builder().children(children).build()
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnFalseIfRelationsHasComplementedChildThatIsRevoked() {
            CertificateRelation[] children = {
                CertificateRelation.builder()
                    .type(CertificateRelationType.COMPLEMENTED)
                    .status(CertificateStatus.REVOKED)
                    .build()
            };
            final var response = AvailableFunctionUtils.isReplacedOrComplemented(
                CertificateRelations.builder().children(children).build()
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnTrueIfRelationsHasReplacedChildThatIsSigned() {
            CertificateRelation[] children = {
                CertificateRelation.builder()
                    .type(CertificateRelationType.REPLACED)
                    .status(CertificateStatus.SIGNED)
                    .build()
            };
            final var response = AvailableFunctionUtils.isReplacedOrComplemented(
                CertificateRelations.builder().children(children).build()
            );

            assertTrue(response);
        }

        @Test
        void shouldReturnFalseIfRelationsHasReplacedChildThatIsUnsigned() {
            CertificateRelation[] children = {
                CertificateRelation.builder()
                    .type(CertificateRelationType.REPLACED)
                    .status(CertificateStatus.UNSIGNED)
                    .build()
            };
            final var response = AvailableFunctionUtils.isReplacedOrComplemented(
                CertificateRelations.builder().children(children).build()
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnFalseIfRelationsHasReplacedChildThatIsLocked() {
            CertificateRelation[] children = {
                CertificateRelation.builder()
                    .type(CertificateRelationType.REPLACED)
                    .status(CertificateStatus.LOCKED)
                    .build()
            };
            final var response = AvailableFunctionUtils.isReplacedOrComplemented(
                CertificateRelations.builder().children(children).build()
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnFalseIfRelationsHasReplacedChildThatIsLockedRevoked() {
            CertificateRelation[] children = {
                CertificateRelation.builder()
                    .type(CertificateRelationType.REPLACED)
                    .status(CertificateStatus.LOCKED_REVOKED)
                    .build()
            };
            final var response = AvailableFunctionUtils.isReplacedOrComplemented(
                CertificateRelations.builder().children(children).build()
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnFalseIfRelationsHasReplacedChildThatIsRevoked() {
            CertificateRelation[] children = {
                CertificateRelation.builder()
                    .type(CertificateRelationType.REPLACED)
                    .status(CertificateStatus.REVOKED)
                    .build()
            };
            final var response = AvailableFunctionUtils.isReplacedOrComplemented(
                CertificateRelations.builder().children(children).build()
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnFalseIfRelationsHasCopiedChild() {
            CertificateRelation[] children = {CertificateRelation.builder().type(CertificateRelationType.COPIED).build()};
            final var response = AvailableFunctionUtils.isReplacedOrComplemented(
                CertificateRelations.builder().children(children).build()
            );

            assertFalse(response);
        }
    }

    @Nested
    class IsCertificateOfType {

        @Test
        void shouldReturnFalseIfNotType() {
            final var certificate = new Certificate();
            certificate.setMetadata(CertificateMetadata.builder()
                .type("TYPE")
                .build()
            );

            final var response = AvailableFunctionUtils.isCertificateOfType(certificate, Ag7804EntryPoint.MODULE_ID);

            assertFalse(response);
        }

        @Test
        void shouldReturnTrueIfType() {
            final var certificate = new Certificate();
            certificate.setMetadata(CertificateMetadata.builder()
                .type(Ag7804EntryPoint.MODULE_ID)
                .build()
            );

            final var response = AvailableFunctionUtils.isCertificateOfType(certificate, Ag7804EntryPoint.MODULE_ID);

            assertTrue(response);
        }
    }

    @Nested
    class QuestionUtils {

        private Certificate certificate;
        private CertificateDataElement element;

        @BeforeEach
        void setup() {
            element = CertificateDataElement.builder()
                .value(CertificateDataValueBoolean.builder().build())
                .build();
            final var data = new HashMap<String, CertificateDataElement>();
            data.put("ID", element);
            certificate = new Certificate();
            certificate.setData(data);
        }

        @Nested
        class HasQuestion {

            @Test
            void shouldReturnTrueIfCertificateIncludesQuestion() {
                final var response = AvailableFunctionUtils.hasQuestion(certificate, "ID");

                assertTrue(response);
            }

            @Test
            void shouldReturnFalseIfCertificateNotIncludesQuestion() {
                final var response = AvailableFunctionUtils.hasQuestion(certificate, "NOT_ID");

                assertFalse(response);
            }

        }

        @Test
        void shouldReturnValueForQuestionMatchingId() {
            final var response = AvailableFunctionUtils.getQuestionValue(certificate, "ID");

            assertEquals(element.getValue(), response);
        }
    }

    @Nested
    class IsBooleanValueTrue {

        @Test
        void shouldReturnFalseIfNull() {
            final var response = AvailableFunctionUtils.isBooleanValueTrue(null);

            assertFalse(response);
        }

        @Test
        void shouldReturnFalseIfSelectedIsNull() {
            final var response = AvailableFunctionUtils.isBooleanValueTrue(
                CertificateDataValueBoolean.builder().build()
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnFalseIfSelectedIsFalse() {
            final var response = AvailableFunctionUtils.isBooleanValueTrue(
                CertificateDataValueBoolean.builder().selected(false).build()
            );

            assertFalse(response);
        }

        @Test
        void shouldReturnTrueIfSelectedIsTrue() {
            final var response = AvailableFunctionUtils.isBooleanValueTrue(
                CertificateDataValueBoolean.builder().selected(true).build()
            );

            assertTrue(response);
        }
    }

    @Nested
    class IsBooleanValueNullOrFalse {

        @Test
        void shouldReturnTrueIfNull() {
            final var response = AvailableFunctionUtils.isBooleanValueNullOrFalse(null);

            assertTrue(response);
        }

        @Test
        void shouldReturnTrueIfSelectedIsNull() {
            final var response = AvailableFunctionUtils.isBooleanValueNullOrFalse(
                CertificateDataValueBoolean.builder().build()
            );

            assertTrue(response);
        }

        @Test
        void shouldReturnTrueIfSelectedIsFalse() {
            final var response = AvailableFunctionUtils.isBooleanValueNullOrFalse(
                CertificateDataValueBoolean.builder().selected(false).build()
            );

            assertTrue(response);
        }

        @Test
        void shouldReturnFalseIfSelectedIsTrue() {
            final var response = AvailableFunctionUtils.isBooleanValueNullOrFalse(
                CertificateDataValueBoolean.builder().selected(true).build()
            );

            assertFalse(response);
        }
    }
}
