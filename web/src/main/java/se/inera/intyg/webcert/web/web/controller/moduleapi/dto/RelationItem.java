/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import org.joda.time.LocalDateTime;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;

public final class RelationItem {

    private final String intygsId;
    private final String kod;
    private final String status;
    private final LocalDateTime date;

    public RelationItem(final Utkast reference) {
        intygsId = reference.getIntygsId();
        kod = convert(reference.getRelationKod());
        status = convert(reference.getStatus());
        date = getDate(reference);
    }

    private LocalDateTime getDate(final Utkast reference) {
        return UtkastStatus.SIGNED.equals(reference.getStatus())
                ? reference.getSignatur().getSigneringsDatum()
                : reference.getSenastSparadDatum();
    }

    private String convert(UtkastStatus status) {
        switch (status) {
        case SIGNED:
            return "INTYG";
        default:
            return "UTKAST";
        }
    }

    private String convert(RelationKod relationsKod) {
        return (relationsKod == null) ? null : relationsKod.value();
    }

    public String getIntygsId() {
        return intygsId;
    }

    public String getStatus() {
        return status;
    }

    public String getKod() {
        return kod;
    }

    public LocalDateTime getDate() {
        return date;
    }

}
