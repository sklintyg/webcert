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

package se.inera.intyg.webcert.web.service.facade.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueText;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.support.modules.support.facade.TypeAheadEnum;
import se.inera.intyg.webcert.web.service.facade.util.DefaultTypeAheadProvider;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;

class SosParentCertificateValidatorTest {

    private static final String QUESTION_DODSPLATS_KOMMUN = "3.1";
    private static final String CERTIFICATE_ID = "certificate-123";

    private SosParentCertificateValidator validator;
    private DraftValidation draftValidation;

    @BeforeEach
    void setUp() {
        DefaultTypeAheadProvider typeAheadProvider = mock(DefaultTypeAheadProvider.class);

        final var validMunicipalityValues = List.of("Stockholm", "Göteborg", "Malmö", "Uppsala", "Linköping");
        doReturn(validMunicipalityValues).when(typeAheadProvider).getValues(TypeAheadEnum.MUNICIPALITIES);

        validator = new SosParentCertificateValidator(typeAheadProvider);
        draftValidation = new DraftValidation();
    }

    @Nested
    class ValidMunicipalityValue {

        @Test
        void shouldNotAddValidationErrorForStockholm() {
            final var certificate = createCertificateWithMunicipalityValue("Stockholm");

            validator.validate(certificate, draftValidation);

            assertTrue(draftValidation.getMessages().isEmpty());
            assertEquals(ValidationStatus.VALID, draftValidation.getStatus());
        }

        @Test
        void shouldNotAddValidationErrorForGoteborg() {
            final var certificate = createCertificateWithMunicipalityValue("Göteborg");

            validator.validate(certificate, draftValidation);

            assertTrue(draftValidation.getMessages().isEmpty());
            assertEquals(ValidationStatus.VALID, draftValidation.getStatus());
        }

        @Test
        void shouldNotAddValidationErrorForMalmo() {
            final var certificate = createCertificateWithMunicipalityValue("Malmö");

            validator.validate(certificate, draftValidation);

            assertTrue(draftValidation.getMessages().isEmpty());
            assertEquals(ValidationStatus.VALID, draftValidation.getStatus());
        }
    }

    @Nested
    class InvalidMunicipalityValue {

        @Test
        void shouldAddValidationErrorForInvalidMunicipality() {
            final var certificate = createCertificateWithMunicipalityValue("InvalidMunicipality");

            validator.validate(certificate, draftValidation);

            assertEquals(1, draftValidation.getMessages().size());
            assertEquals("Du måste välja ett alternativ.", draftValidation.getMessages().getFirst().getMessage());
            assertEquals(QUESTION_DODSPLATS_KOMMUN, draftValidation.getMessages().getFirst().getField());
            assertEquals(ValidationStatus.INVALID, draftValidation.getStatus());
        }

        @Test
        void shouldAddValidationErrorForRandomText() {
            final var certificate = createCertificateWithMunicipalityValue("Some Random Text");

            validator.validate(certificate, draftValidation);

            assertEquals(1, draftValidation.getMessages().size());
            assertEquals("Du måste välja ett alternativ.", draftValidation.getMessages().getFirst().getMessage());
        }

        @Test
        void shouldAddValidationErrorForPartialMatch() {
            final var certificate = createCertificateWithMunicipalityValue("Stock");

            validator.validate(certificate, draftValidation);

            assertEquals(1, draftValidation.getMessages().size());
            assertEquals("Du måste välja ett alternativ.", draftValidation.getMessages().getFirst().getMessage());
        }
    }

    @Nested
    class EmptyOrNullValues {

        @Test
        void shouldNotAddValidationErrorForEmptyValue() {
            final var certificate = createCertificateWithMunicipalityValue("");

            validator.validate(certificate, draftValidation);

            assertTrue(draftValidation.getMessages().isEmpty(),
                "Empty values should not be validated - there's another validation for required fields");
            assertEquals(ValidationStatus.VALID, draftValidation.getStatus());
        }

        @Test
        void shouldNotAddValidationErrorWhenQuestionNotPresent() {
            final var certificate = createCertificateWithoutMunicipality();

            validator.validate(certificate, draftValidation);

            assertTrue(draftValidation.getMessages().isEmpty());
            assertEquals(ValidationStatus.VALID, draftValidation.getStatus());
        }

        @Test
        void shouldNotAddValidationErrorWhenValueIsNull() {
            final var certificate = createCertificateWithNullValue();

            validator.validate(certificate, draftValidation);

            assertTrue(draftValidation.getMessages().isEmpty());
            assertEquals(ValidationStatus.VALID, draftValidation.getStatus());
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void shouldAddValidationErrorForValueWithTrailingWhitespace() {
            final var certificate = createCertificateWithMunicipalityValue("Stockholm ");

            validator.validate(certificate, draftValidation);

            assertEquals(1, draftValidation.getMessages().size());
            assertEquals("Du måste välja ett alternativ.", draftValidation.getMessages().getFirst().getMessage());
            assertEquals(ValidationStatus.INVALID, draftValidation.getStatus());
        }

        @Test
        void shouldAddValidationErrorForValueWithLeadingWhitespace() {
            final var certificate = createCertificateWithMunicipalityValue(" Stockholm");

            validator.validate(certificate, draftValidation);

            assertEquals(1, draftValidation.getMessages().size());
            assertEquals("Du måste välja ett alternativ.", draftValidation.getMessages().getFirst().getMessage());
            assertEquals(ValidationStatus.INVALID, draftValidation.getStatus());
        }
    }

    private Certificate createCertificateWithMunicipalityValue(String value) {
        final var data = new HashMap<String, CertificateDataElement>();

        final var dataElement = CertificateDataElement.builder()
            .value(CertificateDataValueText.builder()
                .text(value)
                .build())
            .build();

        data.put(QUESTION_DODSPLATS_KOMMUN, dataElement);

        final var certificate = new Certificate();
        final var metadata = CertificateMetadata.builder()
            .id(CERTIFICATE_ID)
            .build();
        certificate.setMetadata(metadata);
        certificate.setData(data);

        return certificate;
    }

    private Certificate createCertificateWithoutMunicipality() {
        final var data = new HashMap<String, CertificateDataElement>();

        final var certificate = new Certificate();
        final var metadata = CertificateMetadata.builder()
            .id(CERTIFICATE_ID)
            .build();
        certificate.setMetadata(metadata);
        certificate.setData(data);

        return certificate;
    }

    private Certificate createCertificateWithNullValue() {
        final var data = new HashMap<String, CertificateDataElement>();

        final var dataElement = CertificateDataElement.builder()
            .value(null)
            .build();

        data.put(QUESTION_DODSPLATS_KOMMUN, dataElement);

        final var certificate = new Certificate();
        final var metadata = CertificateMetadata.builder()
            .id(CERTIFICATE_ID)
            .build();
        certificate.setMetadata(metadata);
        certificate.setData(data);

        return certificate;
    }
}
