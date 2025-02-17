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

import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent.Source;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEventType;

@Component
public class CertificateRelationToIntygEventInfoConverter {

    public IntygInfoEvent convert(CertificateRelation relation, Certificate relatedCertificate) {
        final var type = getType(relation);

        final var relationEvent = createEvent(
            relation.getCreated(),
            type,
            "intygsId",
            relation.getCertificateId()
        );

        if (relatedCertificate != null) {
            relationEvent.addData("name", relatedCertificate.getMetadata().getIssuedBy().getFullName());
            relationEvent.addData("hsaId", relatedCertificate.getMetadata().getIssuedBy().getPersonId());
        }

        return relationEvent;
    }

    private static IntygInfoEventType getType(CertificateRelation relation) {
        return switch (relation.getType()) {
            case EXTENDED -> IntygInfoEventType.IS007;
            case REPLACED -> IntygInfoEventType.IS008;
            case COMPLEMENTED -> IntygInfoEventType.IS014;
            case COPIED -> IntygInfoEventType.IS026;
        };
    }

    private IntygInfoEvent createEvent(LocalDateTime date, IntygInfoEventType type, String key1, String data1) {
        IntygInfoEvent event = new IntygInfoEvent(Source.WEBCERT, date, type);

        if (!Objects.isNull(key1)) {
            event.addData(key1, data1);
        }

        return event;
    }
}
