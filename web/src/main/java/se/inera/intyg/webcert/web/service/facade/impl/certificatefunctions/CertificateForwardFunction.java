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
package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

public class CertificateForwardFunction {

    private static final String FORWARD_NAME = "Vidarebefordra utkast";
    private static final String FORWARD_DESCRIPTION =
        "Skapar ett e-postmeddelande i din e-postklient med en direktlänk till utkastet.";
    private static final String FORWARD_DESCRIPTION_CERTIFICATE =
        "Skapar ett e-postmeddelande med länk till intyget.";
    private static final String FORWARD_DESCRIPTION_QUESTION =
        "Skapar ett e-postmeddelande i din e-postklient med en direktlänk till frågan/svaret.";

    public static boolean validate(CertificateStatus status, WebCertUser user) {
        return status == CertificateStatus.UNSIGNED && !user.isPrivatLakare();
    }

    public static ResourceLinkDTO createResourceLinkForDraft() {
        return ResourceLinkDTO.create(
            ResourceLinkTypeDTO.FORWARD_CERTIFICATE,
            FORWARD_NAME,
            FORWARD_DESCRIPTION,
            true
        );
    }

    public static ResourceLinkDTO createResourceLinkForQuestionList() {
        return ResourceLinkDTO.create(
            ResourceLinkTypeDTO.FORWARD_QUESTION,
            "Vidarebefordra",
            FORWARD_DESCRIPTION_QUESTION,
            true
        );
    }

    public static ResourceLinkDTO createResourceLink() {
        return ResourceLinkDTO.create(
            ResourceLinkTypeDTO.FORWARD_CERTIFICATE,
            "Vidarebefordra",
            FORWARD_DESCRIPTION,
            true
        );
    }
}
