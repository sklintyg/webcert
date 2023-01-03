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
package se.inera.intyg.webcert.persistence.utkast.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import se.inera.intyg.common.support.model.UtkastStatus;

public class UtkastFilter {

    private String unitHsaId;

    private String savedByHsaId;

    private Boolean notified;

    private LocalDateTime savedFrom;

    private LocalDateTime savedTo;

    private List<UtkastStatus> statusList = new ArrayList<>();

    private Integer startFrom;

    private Integer pageSize;

    private String orderBy;

    private Boolean orderAscending;

    private String patientId;

    public UtkastFilter(String unitHsaId) {
        this.unitHsaId = unitHsaId;
    }

    public boolean hasPageSizeAndStartFrom() {
        return pageSize != null && startFrom != null;
    }

    public String getUnitHsaId() {
        return unitHsaId;
    }

    public void setUnitHsaId(String unitHsaId) {
        this.unitHsaId = unitHsaId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getSavedByHsaId() {
        return savedByHsaId;
    }

    public void setSavedByHsaId(String savedByHsaId) {
        this.savedByHsaId = savedByHsaId;
    }

    public Boolean getNotified() {
        return notified;
    }

    public void setNotified(Boolean notified) {
        this.notified = notified;
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

    public List<UtkastStatus> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<UtkastStatus> statusList) {
        this.statusList = statusList;
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

}
