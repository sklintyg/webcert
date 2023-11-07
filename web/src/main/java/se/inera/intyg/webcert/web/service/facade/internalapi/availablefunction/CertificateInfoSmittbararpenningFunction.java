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

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.webcert.web.service.facade.internalapi.AvailableFunctions;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionTypeDTO;

@Component
public class CertificateInfoSmittbararpenningFunction implements AvailableFunctions {

    private static final String AVSTANGNING_SMITTSKYDD_QUESTION_ID = "AVSTANGNING_SMITTSKYDD_SVAR_ID_27";
    private static final String AVSTANGNING_SMITTSKYDD_INFO_TITLE = "Avstängning enligt smittskyddslagen";
    private static final String AVSTANGNING_SMITTSKYDD_INFO_NAME = "Presentera informationsruta";
    private static final String AVSTANGNING_SMITTSKYDD_INFO_BODY = "I intyg som gäller avstängning enligt smittskyddslagen kan"
        + " du inte dölja din diagnos. När du klickar på 'Skriv ut intyg' hämtas hela intyget.";

    @Override
    public List<AvailableFunctionDTO> get(Certificate certificate) {
        if (certificateIsAg7804(certificate) && certificate.getData().containsKey(AVSTANGNING_SMITTSKYDD_QUESTION_ID)
            && questionSmittbararpenningIsTrue(certificate)) {
            return Collections.singletonList(
                AvailableFunctionDTO.create(
                    AvailableFunctionTypeDTO.INFO,
                    AVSTANGNING_SMITTSKYDD_INFO_TITLE,
                    AVSTANGNING_SMITTSKYDD_INFO_NAME,
                    AVSTANGNING_SMITTSKYDD_INFO_BODY
                )
            );
        }
        return Collections.emptyList();
    }

    private boolean questionSmittbararpenningIsTrue(Certificate certificate) {
        final var value = (CertificateDataValueBoolean) certificate.getData().get(AVSTANGNING_SMITTSKYDD_QUESTION_ID).getValue();
        return value != null && value.getSelected();
    }

    private boolean certificateIsAg7804(Certificate certificate) {
        return certificate.getMetadata().getType().equals(Ag7804EntryPoint.MODULE_ID);
    }
}
