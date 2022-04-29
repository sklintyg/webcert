/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.impl;

import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

import java.util.Arrays;
import java.util.Objects;

import static se.inera.intyg.common.support.facade.model.CertificateRelationType.COMPLEMENTED;
import static se.inera.intyg.common.support.facade.model.CertificateRelationType.REPLACED;
import static se.inera.intyg.common.support.facade.model.CertificateStatus.SIGNED;

public class CertificateForwardFunction {
    private static final String FORWARD_NAME = "Vidarebefodra utkast";
    private static final String FORWARD_DESCRIPTION = "Skapar ett e-postmeddelande i din e-postklient med en direktlänk till utkastet.";

    public static boolean validate(CertificateStatus status) {
        return status == CertificateStatus.UNSIGNED;
    }

    public static ResourceLinkDTO createResourceLink() {
        return ResourceLinkDTO.create(
                ResourceLinkTypeDTO.FORWARD_CERTIFICATE,
                FORWARD_NAME,
                FORWARD_DESCRIPTION,
                true
        );
    }


    public static ResourceLinkDTO createGenericResourceLink() {
        return ResourceLinkDTO.create(
                ResourceLinkTypeDTO.FORWARD_CERTIFICATE,
                "Vidarebefordra",
                FORWARD_DESCRIPTION,
                true
        );
    }
}
