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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import se.inera.intyg.webcert.persistence.model.Status;

import java.time.LocalDateTime;

public class ArendeListItem {

    private String meddelandeId;
    private String intygId;
    private String intygTyp;
    private String signeratAvNamn;
    private Status status;
    private String patientId;
    private LocalDateTime receivedDate;
    private boolean vidarebefordrad;
    private boolean paminnelse;
    private String fragestallare;
    private String amne;
    private String enhetsnamn;
    private String vardgivarnamn;
    private boolean sekretessmarkering;
    private boolean avliden;

    public String getMeddelandeId() {
        return meddelandeId;
    }

    public void setMeddelandeId(String meddelandeId) {
        this.meddelandeId = meddelandeId;
    }

    public String getIntygId() {
        return intygId;
    }

    public void setIntygId(String intygId) {
        this.intygId = intygId;
    }

    public String getIntygTyp() {
        return intygTyp;
    }

    public void setIntygTyp(String intygTyp) {
        this.intygTyp = intygTyp;
    }

    public String getSigneratAvNamn() {
        return signeratAvNamn;
    }

    public void setSigneratAvNamn(String signeratAvNamn) {
        this.signeratAvNamn = signeratAvNamn;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime localDateTime) {
        this.receivedDate = localDateTime;
    }

    public boolean isVidarebefordrad() {
        return vidarebefordrad;
    }

    public void setVidarebefordrad(boolean vidarebefordrad) {
        this.vidarebefordrad = vidarebefordrad;
    }

    public String getFragestallare() {
        return fragestallare;
    }

    public void setFragestallare(String fragestallare) {
        this.fragestallare = fragestallare;
    }

    public String getAmne() {
        return amne;
    }

    public void setAmne(String amne) {
        this.amne = amne;
    }

    public String getEnhetsnamn() {
        return enhetsnamn;
    }

    public void setEnhetsnamn(String enhetsnamn) {
        this.enhetsnamn = enhetsnamn;
    }

    public String getVardgivarnamn() {
        return vardgivarnamn;
    }

    public void setVardgivarnamn(String vardgivarnamn) {
        this.vardgivarnamn = vardgivarnamn;
    }

    public boolean isPaminnelse() {
        return paminnelse;
    }

    public void setPaminnelse(boolean paminnelse) {
        this.paminnelse = paminnelse;
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
}
