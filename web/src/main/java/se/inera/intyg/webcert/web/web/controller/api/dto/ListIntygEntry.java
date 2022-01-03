/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;

public class ListIntygEntry {

    private String intygId;

    private Personnummer patientId;

    private IntygSource source;

    private String intygType;

    private String intygTypeVersion;

    @JsonIgnore
    private String intygTypeName;

    private String status;

    @JsonIgnore
    private String statusName;

    private LocalDateTime lastUpdatedSigned;

    private String updatedSignedBy;
    @JsonIgnore
    private String updatedSignedById;

    private boolean vidarebefordrad;

    private long version;

    private String vardenhetId;

    private String vardgivarId;

    private Relations relations = new Relations();

    private boolean sekretessmarkering = false;
    private boolean avliden = false;

    private List<ActionLink> links = new ArrayList<>();

    private boolean isTestIntyg = false;


    public String getIntygId() {
        return intygId;
    }

    public void setIntygId(String intygId) {
        this.intygId = intygId;
    }

    public Personnummer getPatientId() {
        return patientId;
    }

    public void setPatientId(Personnummer patientId) {
        this.patientId = patientId;
    }

    public IntygSource getSource() {
        return source;
    }

    public void setSource(IntygSource source) {
        this.source = source;
    }

    public String getIntygType() {
        return intygType;
    }

    public void setIntygType(String intygType) {
        this.intygType = intygType;
    }

    public String getIntygTypeName() {
        return this.intygTypeName;
    }

    public void setIntygTypeName(String intygTypeName) {
        this.intygTypeName = intygTypeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public LocalDateTime getLastUpdatedSigned() {
        return lastUpdatedSigned;
    }

    public void setLastUpdatedSigned(LocalDateTime lastUpdatedSigned) {
        this.lastUpdatedSigned = lastUpdatedSigned;
    }

    public String getUpdatedSignedBy() {
        return updatedSignedBy;
    }

    public void setUpdatedSignedBy(String updatedSignedBy) {
        this.updatedSignedBy = updatedSignedBy;
    }

    public String getUpdatedSignedById() {
        return updatedSignedById;
    }

    public void setUpdatedSignedById(String updatedSignedById) {
        this.updatedSignedById = updatedSignedById;
    }

    public boolean isVidarebefordrad() {
        return vidarebefordrad;
    }

    public void setVidarebefordrad(boolean vidarebefordrad) {
        this.vidarebefordrad = vidarebefordrad;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Relations getRelations() {
        return relations;
    }

    public void setRelations(Relations relations) {
        this.relations = relations;
    }

    public boolean isSekretessmarkering() {
        return sekretessmarkering;
    }

    public void setSekretessmarkering(boolean sekretessmarkering) {
        this.sekretessmarkering = sekretessmarkering;
    }

    public boolean isAvliden() {
        return avliden;
    }

    public void setAvliden(boolean avliden) {
        this.avliden = avliden;
    }

    public String getIntygTypeVersion() {
        return intygTypeVersion;
    }

    public void setIntygTypeVersion(String intygTypeVersion) {
        this.intygTypeVersion = intygTypeVersion;
    }

    public List<ActionLink> getLinks() {
        return links;
    }

    public void addLink(ActionLink link) {
        this.links.add(link);
    }

    public String getVardenhetId() {
        return vardenhetId;
    }

    public void setVardenhetId(String vardenhetId) {
        this.vardenhetId = vardenhetId;
    }

    public String getVardgivarId() {
        return vardgivarId;
    }

    public void setVardgivarId(String vardgivarId) {
        this.vardgivarId = vardgivarId;
    }

    public boolean isTestIntyg() {
        return isTestIntyg;
    }

    public void setTestIntyg(boolean isTestIntyg) {
        this.isTestIntyg = isTestIntyg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ListIntygEntry)) {
            return false;
        }

        ListIntygEntry intygItem = (ListIntygEntry) o;

        return intygId.equals(intygItem.intygId);
    }

    @Override
    public int hashCode() {
        return intygId.hashCode();
    }
}
