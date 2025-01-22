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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import java.time.LocalDateTime;
import se.inera.intyg.common.support.model.UtkastStatus;

public class QueryIntygParameter {

    private Integer startFrom;

    private Integer pageSize;

    private String savedBy;

    private Boolean notified;

    private UtkastStatus status;

    private LocalDateTime savedFrom;

    private LocalDateTime savedTo;

    private LocalDateTime signedFrom;

    private LocalDateTime signedTo;

    private String orderBy;

    private Boolean orderAscending;

    private String patientId;

    private String hsaId;

    private String[] unitIds;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String pnr) {
        patientId = pnr;
    }

    public Integer getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(Integer startFrom) {
        this.startFrom = startFrom;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getSavedBy() {
        return savedBy;
    }

    public void setSavedBy(String savedBy) {
        this.savedBy = savedBy;
    }

    public Boolean getNotified() {
        return notified;
    }

    public void setNotified(Boolean notified) {
        this.notified = notified;
    }

    public UtkastStatus getStatus() {
        return status;
    }

    public void setStatus(UtkastStatus status) {
        this.status = status;
    }

    public LocalDateTime getSavedFrom() {
        return savedFrom;
    }

    public void setSavedFrom(LocalDateTime savedFrom) {
        this.savedFrom = savedFrom;
    }

    public LocalDateTime getSavedTo() {
        return savedTo;
    }

    public void setSavedTo(LocalDateTime savedTo) {
        this.savedTo = savedTo;
    }

    public LocalDateTime getSignedFrom() {
        return signedFrom;
    }

    public void setSignedFrom(LocalDateTime signedFrom) {
        this.signedFrom = signedFrom;
    }

    public LocalDateTime getSignedTo() {
        return signedTo;
    }

    public void setSignedTo(LocalDateTime signedTo) {
        this.signedTo = signedTo;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Boolean getOrderAscending() {
        return orderAscending;
    }

    public void setOrderAscending(Boolean orderAscending) {
        this.orderAscending = orderAscending;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public void setUnitIds(String[] unitIds) {
        this.unitIds = unitIds;
    }

    public String[] getUnitIds() {
        return unitIds;
    }

}
