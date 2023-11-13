/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.webcert.web.service.facade.internalapi.AvailableFunctions;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;

@Component
public class CertificatePrintFunction implements AvailableFunctions {

    private static final String AVSTANGNING_SMITTSKYDD_QUESTION_ID = "27";

    @Override
    public List<AvailableFunctionDTO> get(Certificate certificate) {
        final var availableFunctions = new ArrayList<AvailableFunctionDTO>();
        if (certificateIsAg7804(certificate) && certificate.getData().containsKey(AVSTANGNING_SMITTSKYDD_QUESTION_ID)
            && questionAvstangningSmittskyddIsNullOrFalse(certificate.getData())) {
            availableFunctions.add(AvailableFunctionFactory.customizePrint());
        } else {
            if (certificateIsAg7804(certificate) && certificate.getData().containsKey(AVSTANGNING_SMITTSKYDD_QUESTION_ID)
                && questionSmittbararpenningIsTrue(certificate)) {
                availableFunctions.add(AvailableFunctionFactory.avstangningSmittskydd());
            }
            availableFunctions.add(AvailableFunctionFactory.print());
        }
        return availableFunctions;
    }

    private boolean questionAvstangningSmittskyddIsNullOrFalse(Map<String, CertificateDataElement> data) {
        final var value = (CertificateDataValueBoolean) data.get(AVSTANGNING_SMITTSKYDD_QUESTION_ID).getValue();
        return value.getSelected() == null || !value.getSelected();
    }

    private boolean questionSmittbararpenningIsTrue(Certificate certificate) {
        final var value = (CertificateDataValueBoolean) certificate.getData().get(AVSTANGNING_SMITTSKYDD_QUESTION_ID).getValue();
        return value != null && value.getSelected() != null && value.getSelected();
    }

    private static boolean certificateIsAg7804(Certificate certificate) {
        return certificate.getMetadata().getType().equals(Ag7804EntryPoint.MODULE_ID);
    }
}
