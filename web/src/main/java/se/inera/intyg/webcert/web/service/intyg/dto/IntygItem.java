/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import se.inera.intyg.common.support.model.Status;

public class IntygItem {

    private String id;

    private String type;

    private LocalDate fromDate;

    private LocalDate tomDate;

    private List<Status> statuses;

    private LocalDateTime signedDate;

    private String signedBy;

    public IntygItem() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getTomDate() {
        return tomDate;
    }

    public void setTomDate(LocalDate tomDate) {
        this.tomDate = tomDate;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Status> status) {
        this.statuses = status;
    }

    public LocalDateTime getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(LocalDateTime signedDate) {
        this.signedDate = signedDate;
    }

    public String getSignedBy() {
        return signedBy;
    }

    public void setSignedBy(String signedBy) {
        this.signedBy = signedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntygItem)) {
            return false;
        }

        IntygItem intygItem = (IntygItem) o;

        return id.equals(intygItem.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
