/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.modules;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.common.support.modules.service.WebcertModuleService;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponseType;

/**
 * Exposes services to be used by modules.
 *
 * @author npet
 */
@Component
public class WebcertModuleServiceImpl implements WebcertModuleService {

    private static final Logger LOG = LoggerFactory.getLogger(WebcertModuleService.class);

    @Autowired
    private DiagnosService diagnosService;

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.common.support.modules.service.WebcertModuleService#validateDiagnosisCode(java.lang.String,
     * int)
     */
    @Override
    public boolean validateDiagnosisCode(String codeFragment, String codeSystemStr) {

        DiagnosResponse response = diagnosService.getDiagnosisByCode(codeFragment, codeSystemStr);

        LOG.debug("Validation result for diagnosis code '{}' is {}", codeFragment, response);

        return (DiagnosResponseType.OK.equals(response.getResultat()));
    }

    @Override
    public boolean validateDiagnosisCode(String codeFragment, Diagnoskodverk codeSystem) {

        DiagnosResponse response = diagnosService.getDiagnosisByCode(codeFragment, codeSystem);

        LOG.debug("Validation result for diagnosis code '{}' is {}", codeFragment, response);

        return (DiagnosResponseType.OK.equals(response.getResultat()));
    }

    /*
     * The given code may represent a group of multiple diagnosis codes, and thus generate a list of matches instead of only one. 
     * This means that the mapping is no longer 1:1. 
     * In that case, as well as in the case where there is no match, description will be set to "" since we do not wish to interpret 
     * what is being delivered to us - we only wish to forward the information. 
     */
    @Override
    public String getDescriptionFromDiagnosKod(String code, String codeSystemStr) {
        DiagnosResponse response = diagnosService.getDiagnosisByCode(code, codeSystemStr);
        List<se.inera.intyg.webcert.web.service.diagnos.model.Diagnos> diagnoser = response.getDiagnoser();
        String result = (diagnoser == null || diagnoser.size() != 1) ? "" : diagnoser.get(0).getBeskrivning();
        return result;
    }

}
