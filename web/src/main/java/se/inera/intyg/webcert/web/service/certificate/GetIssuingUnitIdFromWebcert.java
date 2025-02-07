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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.intyg.IntygService;

@Service
@RequiredArgsConstructor
public class GetIssuingUnitIdFromWebcert {

    private final IntygService intygService;

    public String get(String certificateId) {
        final var intygTypeInfo = intygService.getIntygTypeInfo(certificateId);
        final var certificateType = intygTypeInfo.getIntygType();
        return intygService.getIssuingVardenhetHsaId(certificateId, certificateType);
    }
}
