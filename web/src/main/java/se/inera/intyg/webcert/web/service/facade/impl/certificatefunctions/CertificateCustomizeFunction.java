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

package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.ResourceLinkTypeDTO;

@Component
public class CertificateCustomizeFunction {

    private static final String RESOURCE_LINK_BODY = "När du skriver ut ett läkarintyg du ska lämna till din arbetsgivare kan du "
        + "välja om du vill att din diagnos ska visas eller döljas. Ingen annan information kan döljas. ";
    private static final String RESOURCE_LINK_TITLE = "Vill du visa eller dölja diagnos?";
    private static final String RESOURCE_LINK_NAME = "Anpassa intyget";
    private static final String RESOURCE_LINK_DESCRIPTION = "Information om diagnos kan vara viktig för din arbetsgivare."
        + " Det kan underlätta anpassning av din arbetssituation. Det kan också göra att du snabbare kommer tillbaka till arbetet.";
    private static final String AVSTANGNING_SMITTSKYDD_QUESTION_ID = "AVSTANGNING_SMITTSKYDD_SVAR_ID_27";

    public Optional<ResourceLinkDTO> get(Certificate certificate) {
        if (certificateIsCorrectType(certificate) && questionAvstangningSmittskyddIsNullOrFalse(certificate.getData())) {
            return Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.CUSTOMIZE_CERTIFICATE,
                    RESOURCE_LINK_TITLE,
                    RESOURCE_LINK_NAME,
                    RESOURCE_LINK_DESCRIPTION,
                    RESOURCE_LINK_BODY
                )
            );
        }
        return Optional.empty();
    }

    private boolean questionAvstangningSmittskyddIsNullOrFalse(Map<String, CertificateDataElement> data) {
        if (!data.containsKey(AVSTANGNING_SMITTSKYDD_QUESTION_ID)) {
            return false;
        }
        final var value = (CertificateDataValueBoolean) data.get(AVSTANGNING_SMITTSKYDD_QUESTION_ID).getValue();
        return value.getSelected() == null || !value.getSelected();
    }

    private static boolean certificateIsCorrectType(Certificate certificate) {
        return certificate.getMetadata().getType().equals(Ag7804EntryPoint.MODULE_ID);
    }
}
