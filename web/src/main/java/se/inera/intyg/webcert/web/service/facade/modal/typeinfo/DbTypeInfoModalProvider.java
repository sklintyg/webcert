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

package se.inera.intyg.webcert.web.service.facade.modal.typeinfo;

import java.util.Optional;
import se.inera.intyg.webcert.web.service.facade.impl.CertificateTypeInfoModal;
import se.inera.intyg.webcert.web.service.facade.impl.PreviousCertificateInfo;

public class DbTypeInfoModalProvider implements CertificateTypeInfoModalProvider {

    @Override
    public Optional<CertificateTypeInfoModal> create(PreviousCertificateInfo previousCertificateInfo) {
        if (previousCertificateInfo.isSameCareProvider() && previousCertificateInfo.isSameUnit()) {
            return Optional.empty();
        }

        return Optional.of(CertificateTypeInfoModal.builder()
            .title(getTitle(previousCertificateInfo.isDraft(),
                previousCertificateInfo.isSameCareProvider()))
            .description(buildDescription(previousCertificateInfo))
            .link("Visa vårdenhetens namn och HSA-id")
            .build());
    }

    private String getTitle(boolean isDraft, boolean sameCareProvider) {
        if (sameCareProvider) {
            return isDraft ? "Utkast på dödsbevis på annan vårdenhet" : "Signerat dödsbevis på annan vårdenhet";
        }

        return isDraft ? "Utkast på dödsbevis hos annan vårdgivare" : "Signerat dödsbevis hos annan vårdgivare";
    }
    
    private String buildDescription(PreviousCertificateInfo previousCertificateInfo) {
        return String.format(
            "<p><strong>Vårdgivare</strong><br/>%s</p>"
                + "<p><strong>Vårdenhet</strong><br/>%s</p>"
                + "<p><strong>Vårdenhetens HSA-id</strong><br/>%s</p>",
            previousCertificateInfo.getCareProviderName(),
            previousCertificateInfo.getCareUnitName(),
            previousCertificateInfo.getCareUnitHsaId()
        );
    }
}
