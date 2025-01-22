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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3.CreateDraftCertificate;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;

@Slf4j
@Service
public class CreateDraftCertificateAggregator implements CreateDraftCertificate {

    private final CreateDraftCertificate createDraftCertificateFromWC;
    private final CreateDraftCertificate createDraftCertificateFromCS;
    private final CertificateServiceProfile certificateServiceProfile;

    public CreateDraftCertificateAggregator(
        @Qualifier("createDraftCertificateFromWC") CreateDraftCertificate createDraftCertificateFromWC,
        @Qualifier("createDraftCertificateFromCS") CreateDraftCertificate createDraftCertificateFromCS,
        CertificateServiceProfile certificateServiceProfile) {
        this.createDraftCertificateFromWC = createDraftCertificateFromWC;
        this.createDraftCertificateFromCS = createDraftCertificateFromCS;
        this.certificateServiceProfile = certificateServiceProfile;
    }

    @Override
    public CreateDraftCertificateResponseType create(Intyg certificate, IntygUser user) {
        if (!certificateServiceProfile.active()) {
            return createDraftCertificateFromWC.create(certificate, user);
        }

        final var createDraftCertificateResponseType = createDraftCertificateFromCS.create(certificate, user);

        return createDraftCertificateResponseType != null
            ? createDraftCertificateResponseType : createDraftCertificateFromWC.create(certificate, user);
    }
}
