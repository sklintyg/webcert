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
package se.inera.intyg.webcert.web.service.utkast.util;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;

/**
 * @author Magnus Ekstrand on 2019-09-03.
 */
@Component
public final class UtkastServiceHelper {

    @Autowired
    UtkastRepository utkastRepository;
    @Autowired
    private IntygModuleRegistry moduleRegistry;
    @Autowired
    private IntygService intygService;

    public Utlatande getUtlatandeFromIT(String intygId, String intygsTyp, boolean pdlLoggning) {
        IntygContentHolder signedIntygHolder = intygService.fetchIntygData(intygId, intygsTyp, pdlLoggning);
        return signedIntygHolder.getUtlatande();
    }

    public Utlatande getUtlatandeForCandidateFromIT(String intygId, String intygsTyp, boolean pdlLoggning) {
        IntygContentHolder signedIntygHolder = intygService.fetchIntygDataforCandidate(intygId, intygsTyp, pdlLoggning);
        return signedIntygHolder.getUtlatande();
    }

    public Utlatande getUtlatande(String intygId, String intygsTyp, boolean pdlLoggning)
        throws ModuleException, ModuleNotFoundException {
        Utlatande utlatande;
        if (utkastRepository.existsById(intygId)) {
            final Utkast utkast = utkastRepository.findById(intygId).orElse(null);

            if (utkast == null) {
                throw new ModuleException("Could not convert original certificate to Utlatande. Original certificate not found");
            }

            ModuleApi orgModuleApi = moduleRegistry.getModuleApi(intygsTyp, utkast.getIntygTypeVersion());
            try {
                utlatande = orgModuleApi.getUtlatandeFromJson(utkast.getModel());
            } catch (IOException e) {
                throw new ModuleException("Could not convert original certificate to Utlatande", e);
            }
        } else {
            IntygContentHolder signedIntygHolder = intygService.fetchIntygData(intygId, intygsTyp, true);
            utlatande = signedIntygHolder.getUtlatande();
        }

        return utlatande;
    }

}
