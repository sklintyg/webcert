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

package se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction;

import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionUtils.getQuestionValue;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionUtils.hasQuestion;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionUtils.isBooleanValueNullOrFalse;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionUtils.isBooleanValueTrue;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionUtils.isCertificateOfType;
import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionUtils.isReplacedOrComplemented;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.ag114.support.Ag114EntryPoint;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.facade.internalapi.AvailableFunctions;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;

@Component
public class CertificatePrintFunction implements AvailableFunctions {

    private static final String AVSTANGNING_SMITTSKYDD_QUESTION_ID = "27";

    private final AuthoritiesHelper authoritiesHelper;

    public CertificatePrintFunction(AuthoritiesHelper authoritiesHelper) {
        this.authoritiesHelper = authoritiesHelper;
    }

    @Override
    public List<AvailableFunctionDTO> get(Certificate certificate) {
        final var availableFunctions = new ArrayList<AvailableFunctionDTO>();
        if (!isPrintFeatureActive(certificate)
            || isReplacedOrComplemented(certificate.getMetadata().getRelations())) {
            return Collections.emptyList();
        }

        if (isCustomizedPrintAvailable(certificate)) {
            availableFunctions.add(AvailableFunctionFactory.customizePrint(true, getFileName(certificate)));
        } else {
            availableFunctions.add(AvailableFunctionFactory.print(true, getFileName(certificate)));
        }

        if (isCustomizedPrintInfoAvailable(certificate)) {
            availableFunctions.add(AvailableFunctionFactory.avstangningSmittskydd(true));
        }

        return availableFunctions;
    }

    private static CertificateDataValueBoolean getQuestionAvstagningSmittskyddValue(Certificate certificate) {
        return (CertificateDataValueBoolean) getQuestionValue(certificate, AVSTANGNING_SMITTSKYDD_QUESTION_ID);
    }

    private static boolean isCustomizedPrintAvailable(Certificate certificate) {

        if (isCertificateOfType(certificate, Ag114EntryPoint.MODULE_ID)) {
            return true;
        }

        return isCertificateOfType(certificate, Ag7804EntryPoint.MODULE_ID)
            && hasQuestion(certificate, AVSTANGNING_SMITTSKYDD_QUESTION_ID)
            && isBooleanValueNullOrFalse(getQuestionAvstagningSmittskyddValue(certificate));
    }

    private static boolean isCustomizedPrintInfoAvailable(Certificate certificate) {
        return isCertificateOfType(certificate, Ag7804EntryPoint.MODULE_ID)
            && hasQuestion(certificate, AVSTANGNING_SMITTSKYDD_QUESTION_ID)
            && isBooleanValueTrue(getQuestionAvstagningSmittskyddValue(certificate));
    }

    private boolean isPrintFeatureActive(Certificate certificate) {
        return authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_UTSKRIFT, certificate.getMetadata().getType());
    }

    private static String getFileName(Certificate certificate) {
        return certificate.getMetadata().getName()
            .replace("å", "a")
            .replace("ä", "a")
            .replace("ö", "o")
            .replace(" ", "_")
            .replace("–", "")
            .replace("__", "_")
            .toLowerCase();
    }
}
