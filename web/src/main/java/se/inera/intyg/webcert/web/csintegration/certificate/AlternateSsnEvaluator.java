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

package se.inera.intyg.webcert.web.csintegration.certificate;

import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.link.ResourceLink;
import se.inera.intyg.common.support.facade.model.link.ResourceLinkTypeEnum;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Component
public class AlternateSsnEvaluator {

    public boolean shouldUpdate(Certificate certificate, WebCertUser user) {
        if (!CertificateStatus.UNSIGNED.equals(certificate.getMetadata().getStatus())) {
            return false;
        }

        if (alternateSsnNotProvided(user)) {
            return false;
        }

        if (removeDash(user.getParameters().getAlternateSsn()).equals(
            removeDash(certificate.getMetadata().getPatient().getPersonId().getId()))) {
            return false;
        }

        return certificate.getLinks().stream()
            .filter(link -> link.getType().equals(ResourceLinkTypeEnum.EDIT_CERTIFICATE))
            .findFirst()
            .map(ResourceLink::isEnabled)
            .orElse(false);
    }

    private String removeDash(String value) {
        return value.replace("-", "");
    }

    private static boolean alternateSsnNotProvided(WebCertUser user) {
        return user.getParameters() == null
            || (user.getParameters().getAlternateSsn() == null
            || user.getParameters().getAlternateSsn().isBlank())
            || Personnummer.createPersonnummer(user.getParameters().getAlternateSsn()).isEmpty();
    }
}
