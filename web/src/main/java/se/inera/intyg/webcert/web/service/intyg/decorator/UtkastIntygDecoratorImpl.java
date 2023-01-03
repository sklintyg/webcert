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
package se.inera.intyg.webcert.web.service.intyg.decorator;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

/**
 * This is a bit of a hack to mitigate states when an intyg is on a resend queue due to a 3rd party being unavailable
 * so IT cannot finish a send/revoke properly. Since the Intyg in IT won't be marked as sent/revoked until the operation
 * has successfully finished, the intyg state shown in Webcert GUI will be as NOT sent/revoked, even though we
 * have performed that operation and indicated in the GUI that we have sent or revoked it.
 *
 * Thus, we decorate the returned information with revoked/sent state from the Utkast if possible.
 */
@Component
public class UtkastIntygDecoratorImpl implements UtkastIntygDecorator {

    @Autowired
    private UtkastRepository utkastRepository;

    @Override
    public void decorateWithUtkastStatus(CertificateResponse certificate) {
        if (certificate.isRevoked()) {
            return;
        }

        boolean isSent = isSent(certificate.getMetaData().getStatus());
        boolean isRevoked = isRevoked(certificate.getMetaData().getStatus());
        if (isSent && isRevoked) {
            return;
        }
        Utkast utkast = utkastRepository.findById(certificate.getMetaData().getCertificateId()).orElse(null);

        // Don't try to decorate if utkast not found. May be a non-webcert intyg.
        if (utkast == null) {
            return;
        }

        if (utkast.getSkickadTillMottagareDatum() != null && !isSent) {
            // Add sent status flag
            Status sentStatus = new Status(CertificateState.SENT, utkast.getSkickadTillMottagare(), utkast.getSkickadTillMottagareDatum());
            certificate.getMetaData().getStatus().add(sentStatus);
        }

        if (utkast.getAterkalladDatum() != null && !isRevoked) {
            // Add revoked status flag
            Status revokedStatus = new Status(CertificateState.CANCELLED, "HSVARD", utkast.getAterkalladDatum());
            certificate.getMetaData().getStatus().add(revokedStatus);
        }
    }

    private boolean isRevoked(List<Status> statuses) {
        for (Status status : statuses) {
            if (status.getType() == CertificateState.CANCELLED) {
                return true;
            }
        }
        return false;
    }

    private boolean isSent(List<Status> statuses) {
        for (Status status : statuses) {
            if (status.getType() == CertificateState.SENT) {
                return true;
            }
        }
        return false;
    }
}
