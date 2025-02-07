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

package se.inera.intyg.webcert.web.service.facade.internalapi.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.service.facade.internalapi.AvailableFunctions;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;

@Service
public class GetAvailableFunctionsForCertificateService {

    private final List<AvailableFunctions> availableFunctions;

    public GetAvailableFunctionsForCertificateService(List<AvailableFunctions> availableFunctions) {
        this.availableFunctions = availableFunctions;
    }

    public List<AvailableFunctionDTO> get(Certificate certificate) {
        return availableFunctions.stream()
            .map(function -> function.get(certificate))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }
}
