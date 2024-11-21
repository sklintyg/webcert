/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class LogSjfServiceTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String CARE_UNIT_ID = "careUnitId";
    private static final String CARE_PROVIDER_ID = "careProviderId";
    @InjectMocks
    private LogSjfService logSjfService;
    @Mock
    private MonitoringLogService monitoringLogService;
    @Mock
    private Certificate certificate;
    @Mock
    private WebCertUser user;
    private final CertificateMetadata.CertificateMetadataBuilder certificateMetadataBuilder = CertificateMetadata.builder()
        .id(CERTIFICATE_ID)
        .type(CERTIFICATE_TYPE);
    @Mock
    private Unit careProvider;
    @Mock
    private Unit careUnit;
    @Mock
    private SelectableVardenhet valdVardgivare;
    @Mock
    private SelectableVardenhet valdVardenhet;

    @BeforeEach
    void setUp() {
        when(user.getValdVardgivare()).thenReturn(valdVardgivare);
    }

    @Test
    void logIntegratedOtherCaregiverIsCalled() {
        certificateMetadataBuilder
            .unit(
                Unit.builder()
                    .unitId(CARE_UNIT_ID)
                    .build()
            )
            .careProvider(
                Unit.builder()
                    .unitId(CARE_PROVIDER_ID)
                    .build()
            );

        when(certificate.getMetadata()).thenReturn(certificateMetadataBuilder.build());
        when(valdVardgivare.getId()).thenReturn("differentProvider");

        logSjfService.log(certificate, user);

        verify(monitoringLogService).logIntegratedOtherCaregiver(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_PROVIDER_ID, CARE_UNIT_ID);
    }

    @Test
    void logIntegratedOtherUnitIsCalled() {
        certificateMetadataBuilder
            .unit(
                Unit.builder()
                    .unitId(CARE_UNIT_ID)
                    .build()
            )
            .careProvider(
                Unit.builder()
                    .unitId(CARE_PROVIDER_ID)
                    .build()
            );

        when(certificate.getMetadata()).thenReturn(certificateMetadataBuilder.build());
        when(valdVardgivare.getId()).thenReturn(CARE_PROVIDER_ID);
        when(user.getValdVardenhet()).thenReturn(valdVardenhet);
        when(valdVardenhet.getHsaIds()).thenReturn(List.of("differentUnit"));

        logSjfService.log(certificate, user);
        verify(monitoringLogService).logIntegratedOtherUnit(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_PROVIDER_ID, CARE_UNIT_ID);
    }
}
