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
package se.inera.intyg.webcert.web.service.utkast.dto;

import java.time.LocalDateTime;

public class PreviousIntyg {

    private boolean sameVardgivare;
    private boolean sameEnhet;
    private boolean enableShowDoiButton;
    private String enhetName;
    private String latestIntygsId;
    private LocalDateTime skapat;

    public PreviousIntyg() {
    }

    private PreviousIntyg(final boolean sameVardgivare, final LocalDateTime skapat) {
        this.sameVardgivare = sameVardgivare;
        this.skapat = skapat;
    }

    private PreviousIntyg(
        final boolean sameVardgivare,
        final LocalDateTime skapat,
        final boolean sameEnhet,
        final boolean enableShowDoiButton,
        final String enhetName,
        final String latestIntygsId) {
        this.sameVardgivare = sameVardgivare;
        this.latestIntygsId = latestIntygsId;
        this.sameEnhet = sameEnhet;
        this.enableShowDoiButton = enableShowDoiButton;
        this.enhetName = enhetName;
        this.skapat = skapat;
    }

    public static PreviousIntyg of(
        final boolean sameVardgivare,
        final boolean sameEnhet,
        final boolean enableShowDoiButton,
        final String enhetName,
        final String latestIntygsId,
        final LocalDateTime skapat) {

        if (sameVardgivare) {
            return new PreviousIntyg(sameVardgivare, skapat, sameEnhet, enableShowDoiButton, enhetName, latestIntygsId);
        } else {
            return new PreviousIntyg(sameVardgivare, skapat);
        }
    }

    public boolean isSameVardgivare() {
        return sameVardgivare;
    }

    public boolean isSameEnhet() {
        return sameEnhet;
    }

    public boolean isEnableShowDoiButton() {
        return enableShowDoiButton;
    }

    public String getEnhetName() {
        return enhetName;
    }

    public String getLatestIntygsId() {
        return latestIntygsId;
    }

    public LocalDateTime getSkapat() {
        return skapat;
    }
}
