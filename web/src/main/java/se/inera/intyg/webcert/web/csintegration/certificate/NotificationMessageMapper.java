/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;

@Component
@RequiredArgsConstructor
public class NotificationMessageMapper {

    public NotificationMessage map(Certificate certificate, String encodedXmlRepresentation, HandelsekodEnum eventType) {
        final var now = LocalDateTime.now();
        final var notificationMessage = new NotificationMessage(
            certificate.getMetadata().getId(),
            certificate.getMetadata().getType(),
            now,
            eventType,
            certificate.getMetadata().getUnit().getUnitId(),
            null,
            FragorOchSvar.getEmpty(),
            ArendeCount.getEmpty(),
            ArendeCount.getEmpty(),
            SchemaVersion.VERSION_3,
            certificate.getMetadata().getExternalReference()
        );

        notificationMessage.setStatusUpdateXml(
            CertificateStatusUpdateFactory.create(certificate, encodedXmlRepresentation, eventType, now)
        );
        return notificationMessage;
    }
}
