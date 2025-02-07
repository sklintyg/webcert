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
package se.inera.intyg.webcert.web.web.controller.facade.dto;

import java.util.List;
import se.inera.intyg.common.support.facade.model.metadata.CertificateConfirmationModal;

public class CertificateTypeInfoDTO {

    private String id;
    private String label;
    private String issuerTypeId;
    private String description;
    private String detailedDescription;
    private List<ResourceLinkDTO> links;
    private String message;
    private CertificateConfirmationModal confirmationModal;

    public CertificateTypeInfoDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIssuerTypeId() {
        return issuerTypeId;
    }

    public void setIssuerTypeId(String issuerTypeId) {
        this.issuerTypeId = issuerTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public List<ResourceLinkDTO> getLinks() {
        return links;
    }

    public void setLinks(List<ResourceLinkDTO> links) {
        this.links = links;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CertificateConfirmationModal getConfirmationModal() {
        return confirmationModal;
    }

    public void setConfirmationModal(CertificateConfirmationModal confirmationModal) {
        this.confirmationModal = confirmationModal;
    }
}
