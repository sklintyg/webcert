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

package se.inera.intyg.webcert.persistence.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by pehr on 10/21/13.
 */
public class Filter {

    private List<String> enhetsIds = new ArrayList<>();

    private boolean questionFromFK;
    private boolean questionFromWC;

    private String hsaId;

    private Boolean vidarebefordrad;

    private LocalDateTime changedFrom;
    private LocalDateTime changedTo;

    private VantarPa vantarPa = VantarPa.ALLA_OHANTERADE;

    private LocalDate replyLatest;

    private Integer startFrom;

    private Integer pageSize;

    private Set<String> intygsTyper = new HashSet<>();

    public boolean hasPageSizeAndStartFrom() {
        return (pageSize != null && startFrom != null);
    }

    public List<String> getEnhetsIds() {
        return enhetsIds;
    }

    public void setEnhetsIds(List<String> enhetsIds) {
        this.enhetsIds = enhetsIds;
    }

    public boolean isQuestionFromFK() {
        return questionFromFK;
    }

    public void setQuestionFromFK(boolean questionFromFK) {
        this.questionFromFK = questionFromFK;
    }

    public boolean isQuestionFromWC() {
        return questionFromWC;
    }

    public void setQuestionFromWC(boolean questionFromWC) {
        this.questionFromWC = questionFromWC;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public Boolean getVidarebefordrad() {
        return vidarebefordrad;
    }

    public void setVidarebefordrad(Boolean vidarebefordrad) {
        this.vidarebefordrad = vidarebefordrad;
    }

    public LocalDateTime getChangedFrom() {
        return changedFrom;
    }

    public void setChangedFrom(LocalDateTime changedFrom) {
        this.changedFrom = changedFrom;
    }

    public LocalDateTime getChangedTo() {
        return changedTo;
    }

    public void setChangedTo(LocalDateTime changedTo) {
        this.changedTo = changedTo;
    }

    public LocalDate getReplyLatest() {
        return replyLatest;
    }

    public void setReplyLatest(LocalDate replyLatest) {
        this.replyLatest = replyLatest;
    }

    public VantarPa getVantarPa() {
        return vantarPa;
    }

    public void setVantarPa(VantarPa vantarPa) {
        this.vantarPa = vantarPa;
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

    public Set<String> getIntygsTyper() {
        return intygsTyper;
    }

    public void setIntygsTyper(Set<String>  intygsTyper) {
        this.intygsTyper = intygsTyper;
    }
}
