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

package se.inera.intyg.webcert.web.service.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;

@ExtendWith(MockitoExtension.class)
class GetIssuingUnitIdFromWebcertTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String CERTIFICATE_TYPE_VERSION = "certificateTypeVersion";
    @Mock
    IntygService intygService;
    @InjectMocks
    GetIssuingUnitIdFromWebcert getIssuingUnitIdFromWebcert;

    @Test
    void shallReturnUnitIdForCertificate() {
        final var expectedUnitId = "expectedUnitId";
        final var intygTypeInfo = new IntygTypeInfo(CERTIFICATE_ID, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
        doReturn(intygTypeInfo).when(intygService).getIntygTypeInfo(CERTIFICATE_ID);
        doReturn(expectedUnitId).when(intygService).getIssuingVardenhetHsaId(CERTIFICATE_ID, CERTIFICATE_TYPE);

        final var actualResult = getIssuingUnitIdFromWebcert.get(CERTIFICATE_ID);
        assertEquals(expectedUnitId, actualResult);
    }
}
