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
package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for a draft and its current status.
 *
 * @author nikpet
 */
public class DraftHolder {

    private long version;

    private boolean vidarebefordrad;

    private UtkastStatus status;

    private String enhetsNamn;

    private String vardgivareNamn;

    @JsonRawValue
    private String content;

    private String latestTextVersion;

    private List<RelationItem> relations;
    private LocalDateTime markedReadyToSignDateTime;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean isVidarebefordrad() {
        return vidarebefordrad;
    }

    public void setVidarebefordrad(boolean vidarebefordrad) {
        this.vidarebefordrad = vidarebefordrad;
    }

    public UtkastStatus getStatus() {
        return status;
    }

    public void setStatus(UtkastStatus status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEnhetsNamn() {
        return enhetsNamn;
    }

    public void setEnhetsNamn(String enhetsNamn) {
        this.enhetsNamn = enhetsNamn;
    }

    public String getVardgivareNamn() {
        return vardgivareNamn;
    }

    public void setVardgivareNamn(String vardgivareNamn) {
        this.vardgivareNamn = vardgivareNamn;
    }

    public void setLatestTextVersion(String latestTextVersion) {
        this.latestTextVersion = latestTextVersion;
    }

    public String getLatestTextVersion() {
        return latestTextVersion;
    }

    public List<RelationItem> getRelations() {
        if (relations == null) {
            relations = new ArrayList<>();
        }
        return relations;
    }

    public void setMarkedReadyToSignDateTime(LocalDateTime markedReadyToSignDateTime) {
        this.markedReadyToSignDateTime = markedReadyToSignDateTime;
    }

    public LocalDateTime getMarkedReadyToSignDateTime() {
        return markedReadyToSignDateTime;
    }
}
