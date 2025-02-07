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
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.CertificateText;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;

@Service
public class GetTextsForCertificateService {

    private final IntygModuleRegistry intygModuleRegistry;

    public GetTextsForCertificateService(IntygModuleRegistry intygModuleRegistry) {
        this.intygModuleRegistry = intygModuleRegistry;
    }

    public List<CertificateText> get(String type, String typeVersion) {
        try {
            final var moduleApi = intygModuleRegistry.getModuleApi(type, typeVersion);
            return List.of(moduleApi.getPreambleForCitizens());
        } catch (ModuleNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
