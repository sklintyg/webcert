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
package se.inera.intyg.webcert.web.service.integreradeenheter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integreradeenheter.IntegratedUnitDTO;
import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;

@Service
public class IntegreradeEnheterService {

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    public Optional<IntegratedUnitDTO> getIntegratedUnit(String hsaId) {
        IntegreradEnhet enhet = integreradeEnheterRegistry.getIntegreradEnhet(hsaId);

        if (enhet == null) {
            return Optional.empty();
        }

        return Optional.of(convert(enhet));
    }

    public List<IntegratedUnitDTO> getAllIntegratedUnits() {
        List<IntegreradEnhet> enheter = integreradeEnheterRegistry.getAllIntegreradEnhet();

        return enheter.stream().map(this::convert).collect(Collectors.toList());
    }

    private IntegratedUnitDTO convert(IntegreradEnhet enhet) {

        return new IntegratedUnitDTO(enhet.getEnhetsId(), enhet.getEnhetsNamn(), enhet.getVardgivarId(), enhet.getVardgivarNamn(),
            enhet.getSkapadDatum(), enhet.getSenasteKontrollDatum());
    }

}
