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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class IntegratedUnitRegistryHelperTest {

    private static final String UNIT_ID = "UNIT_ID";
    private static final String UNIT_NAME = "UNIT_NAME";
    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";
    private static final String CARE_PROVIDER_NAME = "CARE_PROVIDER_NAME";

    private static final String COPY_UNIT_ID = "COPY_UNIT_ID";
    private static final String COPY_UNIT_NAME = "COPY_UNIT_NAME";
    private static final String COPY_CARE_PROVIDER_ID = "COPY_CARE_PROVIDER_ID";
    private static final String COPY_CARE_PROVIDER_NAME = "COPY_CARE_PROVIDER_NAME";
    private static final String TYPE = "TYPE";

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    IntegreradeEnheterRegistry enheterRegistry;

    @Mock
    WebCertUser webCertUser;

    @InjectMocks
    IntegratedUnitRegistryHelper integratedUnitRegistryHelper;

    @Nested
    class AddUnit {

        @Nested
        class HasUser {

            @Test
            void shouldAdd() {
                when(webCertUser.getValdVardenhet())
                    .thenReturn(new Vardenhet(UNIT_ID, UNIT_NAME));
                when(webCertUser.getValdVardgivare())
                    .thenReturn(new Vardenhet(CARE_PROVIDER_ID, CARE_PROVIDER_NAME));
                final var expectedEntry = new IntegreradEnhetEntry(
                    UNIT_ID,
                    UNIT_NAME,
                    CARE_PROVIDER_ID,
                    CARE_PROVIDER_NAME
                );
                
                integratedUnitRegistryHelper.addUnit(webCertUser);

                verify(enheterRegistry).putIntegreradEnhet(expectedEntry, false, true);
            }
        }
    }

    @Nested
    class AddUnitForCopy {

        @BeforeEach
        void setup() {
            when(webCertUserService.getUser())
                .thenReturn(webCertUser);
        }

        @Test
        void shouldAddUnitDataFromCopyUsingRegistryIfDeepIntegration() {
            when(webCertUser.getOrigin())
                .thenReturn("DJUPINTEGRATION");

            final var copy = new Certificate();
            copy.setMetadata(
                CertificateMetadata.builder()
                    .careUnit(
                        Unit.builder()
                            .unitId(COPY_UNIT_ID)
                            .unitName(COPY_UNIT_NAME)
                            .build()
                    )
                    .careProvider(
                        Unit.builder()
                            .unitName(COPY_CARE_PROVIDER_NAME)
                            .unitId(COPY_CARE_PROVIDER_ID)
                            .build()
                    )
                    .type(TYPE)
                    .build()
            );

            final var original = new Certificate();
            original.setMetadata(
                CertificateMetadata.builder()
                    .careUnit(
                        Unit.builder()
                            .unitId(UNIT_ID)
                            .unitName(UNIT_NAME)
                            .build()
                    )
                    .careProvider(
                        Unit.builder()
                            .unitName(CARE_PROVIDER_NAME)
                            .unitId(CARE_PROVIDER_ID)
                            .build()
                    )
                    .type(TYPE)
                    .build()
            );

            final var expectedEntry = new IntegreradEnhetEntry(
                COPY_UNIT_ID,
                COPY_UNIT_NAME,
                COPY_CARE_PROVIDER_ID,
                COPY_CARE_PROVIDER_NAME
            );

            integratedUnitRegistryHelper.addUnitForCopy(original, copy);

            verify(enheterRegistry).addIfSameVardgivareButDifferentUnits(UNIT_ID, expectedEntry, TYPE);
        }

        @Test
        void shouldNotAddUnitDataFromCopyUsingRegistryIfNotDeepIntegration() {
            when(webCertUser.getOrigin())
                .thenReturn("NORMAL");

            final var copy = new Certificate();
            final var original = new Certificate();

            integratedUnitRegistryHelper.addUnitForCopy(original, copy);

            verify(enheterRegistry, times(0)).addIfSameVardgivareButDifferentUnits(anyString(), any(), anyString());
        }
    }
}
