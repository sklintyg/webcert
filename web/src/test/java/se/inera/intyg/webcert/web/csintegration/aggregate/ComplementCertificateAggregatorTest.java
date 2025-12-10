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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.service.facade.ComplementCertificateFacadeService;

@ExtendWith(MockitoExtension.class)
class ComplementCertificateAggregatorTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String MESSAGE = "message";
    private static final Certificate CERTIFICATE = new Certificate();

    @Mock
    ComplementCertificateFacadeService complementCertificateFromWC;
    @Mock
    ComplementCertificateFacadeService complementCertificateFromCS;
    ComplementCertificateAggregator complementCertificateAggregator;

    @BeforeEach
    void setUp() {
        complementCertificateAggregator = new ComplementCertificateAggregator(
            complementCertificateFromWC,
            complementCertificateFromCS
        );
    }

    @Nested
    class ComplementTests {

        @Test
        void shouldGetComplementedCertificateFromCSIfResponseFromCSIsNotNull() {
            when(complementCertificateFromCS.complement(CERTIFICATE_ID, MESSAGE))
                .thenReturn(CERTIFICATE);

            complementCertificateAggregator.complement(CERTIFICATE_ID, MESSAGE);

            verify(complementCertificateFromWC, times(0)).complement(CERTIFICATE_ID, MESSAGE);
        }

        @Test
        void shouldGetComplementedCertificateFromWCIfResponseFromCSIsNull() {
            when(complementCertificateFromCS.complement(CERTIFICATE_ID, MESSAGE))
                .thenReturn(null);

            complementCertificateAggregator.complement(CERTIFICATE_ID, MESSAGE);

            verify(complementCertificateFromWC, times(1)).complement(CERTIFICATE_ID, MESSAGE);
        }
    }

    @Nested
    class AnswerComplementTests {

        @Test
        void shouldGetComplementedCertificateFromCSIfResponseFromCSIsNotNull() {
            when(complementCertificateFromCS.answerComplement(CERTIFICATE_ID, MESSAGE))
                .thenReturn(CERTIFICATE);

            complementCertificateAggregator.answerComplement(CERTIFICATE_ID, MESSAGE);

            verify(complementCertificateFromWC, times(0)).answerComplement(CERTIFICATE_ID, MESSAGE);
        }

        @Test
        void shouldGetComplementedCertificateFromWCIfResponseFromCSIsNull() {
            when(complementCertificateFromCS.answerComplement(CERTIFICATE_ID, MESSAGE))
                .thenReturn(null);

            complementCertificateAggregator.answerComplement(CERTIFICATE_ID, MESSAGE);

            verify(complementCertificateFromWC, times(1)).answerComplement(CERTIFICATE_ID, MESSAGE);
        }
    }
}
