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

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.config.CertificateDataConfigType;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRange;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRangeList;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
public class SendCertificateFunctionImpl implements SendCertificateFunction {

    private static final long SICKLEAVE_DAYS_LIMIT = 15;
    private static final String SEND_BODY_FK = "<p>Om du går vidare kommer intyget skickas direkt till "
        + "Försäkringskassans system vilket ska göras i samråd med patienten.</p>";
    private static final String SEND_BODY_TS =
        "<p>Om du går vidare kommer intyget skickas direkt till Transportstyrelsens system"
            + " vilket ska göras i samråd med patienten.</p>";

    private static final String SEND_BODY_LISJP = "<p>Om du går vidare kommer intyget skickas direkt till "
        + "Försäkringskassans system vilket ska göras i samråd med patienten.</p>"
        + "<p>Upplys patienten om att även göra en ansökan om sjukpenning hos Försäkringskassan.</p>";
    private static final String SEND_BODY_SHORT_SICKLEAVE_PERIOD =
        "<div class='ic-alert ic-alert--status ic-alert--info'><div>"
            + "<i class='ic-alert__icon ic-info-icon' style='float: left; margin-top: 3px;'></i>"
            + "<p style='margin-left: 10px'>Om sjukperioden är kortare än 15 dagar ska intyget inte skickas"
            + " till Försäkringskassan utom i vissa undantagsfall.</p></div></div></br>"
            + "Intyget ska skickas till Försäkringskassan från dag 8 i sjukperioden om patienten är:</br>"
            + "<ul><li>Egenföretagare</li>"
            + "<li>Arbetssökande</li>"
            + "<li>Anställd men arbetsgivaren betalar inte ut sjuklön</li>"
            + "<li>Studerande och arbetar med rätt till sjukpenning (tjänar mer än 10 700 per år)</li>"
            + "<li>Ledig med föräldrapenning</li>"
            + "<li>Ledig med graviditetspenning</li></ul>"
            + "</br>Om du går vidare kommer intyget skickas direkt till "
            + "Försäkringskassans system vilket ska göras i samråd med patienten.</br>"
            + "</br>Upplys patienten om att även göra en ansökan om sjukpenning hos Försäkringskassan.";


    private static final String SEND_TO_FK_DESCRIPTION = "Öppnar ett fönster där du kan välja att skicka intyget till Försäkringskassan.";
    private static final String SEND_TO_TS_DESCRIPTION = "Öppnar ett fönster där du kan välja att skicka intyget till Transportstyrelsen.";
    private final List<String> allowedCertificateTypes = List.of(LuaenaEntryPoint.MODULE_ID, LisjpEntryPoint.MODULE_ID,
        TsBasEntryPoint.MODULE_ID, LuseEntryPoint.MODULE_ID, LuaefsEntryPoint.MODULE_ID, TsDiabetesEntryPoint.MODULE_ID);
    private static final String SEND_TO_FK = "Skicka till Försäkringskassan";
    private static final String SEND_TO_TS = "Skicka till Transportstyrelsen";

    @Override
    public Optional<ResourceLinkDTO> get(Certificate certificate) {
        if (!allowedCertificateTypes.contains(certificate.getMetadata().getType()) || isSent(certificate) || isReplacementSigned(
            certificate) || isWrongMajorVersion(certificate)) {
            return Optional.empty();
        }

        return getResourceLinkDTO(certificate);
    }

    private static boolean isWrongMajorVersion(Certificate certificate) {
        return (certificate.getMetadata().getType().equals(TsBasEntryPoint.MODULE_ID)
            || certificate.getMetadata().getType().equals(TsDiabetesEntryPoint.MODULE_ID)) && !certificate.getMetadata()
            .isLatestMajorVersion();
    }

    private Optional<ResourceLinkDTO> getResourceLinkDTO(Certificate certificate) {
        switch (certificate.getMetadata().getType()) {
            case LisjpEntryPoint.MODULE_ID:
                return getResourceLinkDTOFkLisjp(certificate);
            case LuseEntryPoint.MODULE_ID:
            case LuaefsEntryPoint.MODULE_ID:
            case LuaenaEntryPoint.MODULE_ID:
                return getResourceLinkDTOFk();
            case TsBasEntryPoint.MODULE_ID:
            case TsDiabetesEntryPoint.MODULE_ID:
                return getResourceLinkDTOTs();
            default:
                return Optional.empty();
        }
    }

    private static Optional<ResourceLinkDTO> getResourceLinkDTOTs() {
        return Optional.of(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.SEND_CERTIFICATE,
                SEND_TO_TS,
                SEND_TO_TS_DESCRIPTION,
                SEND_BODY_TS,
                true
            )
        );
    }

    private Optional<ResourceLinkDTO> getResourceLinkDTOFkLisjp(Certificate certificate) {
        return Optional.of(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.SEND_CERTIFICATE,
                SEND_TO_FK,
                SEND_TO_FK_DESCRIPTION,
                hasShortSickleavePeriod(certificate) ? SEND_BODY_SHORT_SICKLEAVE_PERIOD : SEND_BODY_LISJP,
                true
            )
        );
    }

    private static Optional<ResourceLinkDTO> getResourceLinkDTOFk() {
        return Optional.of(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.SEND_CERTIFICATE,
                SEND_TO_FK,
                SEND_TO_FK_DESCRIPTION,
                SEND_BODY_FK,
                true));
    }

    private boolean isSent(Certificate certificate) {
        return certificate.getMetadata().isSent();
    }

    private boolean isReplacementSigned(Certificate certificate) {
        return includesChildRelation(certificate.getMetadata().getRelations());
    }

    private boolean includesChildRelation(CertificateRelations relations) {
        if (missingChildRelations(relations)) {
            return false;
        }

        return Arrays.stream(relations.getChildren()).anyMatch(
            relation -> relation.getType().equals(CertificateRelationType.REPLACED) && relation.getStatus().equals(CertificateStatus.SIGNED)
        );
    }

    private boolean missingChildRelations(CertificateRelations relations) {
        return relations == null || relations.getChildren() == null;
    }

    private boolean hasShortSickleavePeriod(Certificate certificate) {
        final var optionalSickLeavePeriod = certificate.getData().values().stream()
            .filter(dataElement -> dataElement.getConfig().getType() == CertificateDataConfigType.UE_CHECKBOX_DATE_RANGE_LIST).findFirst();
        if (optionalSickLeavePeriod.isPresent()) {
            final var sickLeavePeriod = (CertificateDataValueDateRangeList) optionalSickLeavePeriod.get().getValue();
            long sickLeaveLength = 0;
            for (CertificateDataValueDateRange sickLeave : sickLeavePeriod.getList()) {
                sickLeaveLength += ChronoUnit.DAYS.between(sickLeave.getFrom(), sickLeave.getTo());
            }
            return sickLeaveLength < SICKLEAVE_DAYS_LIMIT;
        }
        return false;
    }
}
