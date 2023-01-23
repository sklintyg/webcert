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
package se.inera.intyg.webcert.web.web.controller.testability.facade.dto;

import java.util.List;
import se.inera.intyg.common.support.facade.model.CertificateStatus;

public class CertificateType {

    private String type;
    private String internalType;
    private String name;
    private List<String> versions;
    private List<CertificateStatus> statuses;
    private List<CreateCertificateFillType> fillType;

    public CertificateType(String type, String internalType, String name, List<String> versions, List<CertificateStatus> statuses,
        List<CreateCertificateFillType> fillType) {
        this.type = type;
        this.internalType = internalType;
        this.name = name;
        this.versions = versions;
        this.statuses = statuses;
        this.fillType = fillType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInternalType() {
        return internalType;
    }

    public void setInternalType(String internalType) {
        this.internalType = internalType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public List<CertificateStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<CertificateStatus> statuses) {
        this.statuses = statuses;
    }

    public List<CreateCertificateFillType> getFillType() {
        return fillType;
    }

    public void setFillType(List<CreateCertificateFillType> fillType) {
        this.fillType = fillType;
    }
}
