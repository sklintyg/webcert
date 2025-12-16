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

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.service.facade.ReadyForSignFacadeService;

@Service
public class ReadyForSignAggregator implements ReadyForSignFacadeService {

    private final ReadyForSignFacadeService readyForSignForWC;
    private final ReadyForSignFacadeService readyForSignForCS;

    public ReadyForSignAggregator(
        ReadyForSignFacadeService readyForSignForWC, ReadyForSignFacadeService readyForSignForCS) {
        this.readyForSignForWC = readyForSignForWC;
        this.readyForSignForCS = readyForSignForCS;
    }

    @Override
    public Certificate readyForSign(String certificateId) {
        final var responseFromCS = readyForSignForCS.readyForSign(certificateId);
        return responseFromCS != null ? responseFromCS : readyForSignForWC.readyForSign(certificateId);
    }
}
