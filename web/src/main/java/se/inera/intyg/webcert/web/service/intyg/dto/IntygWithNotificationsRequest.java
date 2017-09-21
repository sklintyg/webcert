/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.intyg.dto;

import java.time.LocalDateTime;
import java.util.List;

import se.inera.intyg.schemas.contract.Personnummer;

public final class IntygWithNotificationsRequest {
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final List<String> enhetId;
    private final String vardgivarId;
    private final Personnummer personnummer;

    public IntygWithNotificationsRequest(LocalDateTime startDate, LocalDateTime endDate, List<String> enhetId, String vardgivarId,
            Personnummer personnummer) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.enhetId = enhetId;
        this.vardgivarId = vardgivarId;
        this.personnummer = personnummer;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public List<String> getEnhetId() {
        return enhetId;
    }

    public String getVardgivarId() {
        return vardgivarId;
    }

    public Personnummer getPersonnummer() {
        return personnummer;
    }

    public boolean shouldUseEnhetId() {
        return enhetId != null && !enhetId.isEmpty();
    }

    public static class Builder {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<String> enhetId;
        private String vardgivarId;
        private Personnummer personnummer;

        public Builder setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder setEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder setEnhetId(List<String> enhetId) {
            this.enhetId = enhetId;
            return this;
        }

        public Builder setVardgivarId(String vardgivarId) {
            this.vardgivarId = vardgivarId;
            return this;
        }

        public Builder setPersonnummer(Personnummer personnummer) {
            this.personnummer = personnummer;
            return this;
        }

        public IntygWithNotificationsRequest build() {
            return new IntygWithNotificationsRequest(startDate, endDate, enhetId, vardgivarId, personnummer);
        }
    }
}
