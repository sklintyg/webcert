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

import java.util.Map;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;

public class CertificateDTO {

    private CertificateMetadata metadata;
    private Map<String, CertificateDataElement> data;
    private ResourceLinkDTO[] links;

    public static CertificateDTO create(Certificate certificate, ResourceLinkDTO[] links) {
        final var certificateDTO = new CertificateDTO();
        certificateDTO.setMetadata(certificate.getMetadata());
        certificateDTO.setData(certificate.getData());
        certificateDTO.setLinks(links);
        return certificateDTO;
    }

    public CertificateMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(CertificateMetadata metadata) {
        this.metadata = metadata;
    }

    public Map<String, CertificateDataElement> getData() {
        return data;
    }

    public void setData(Map<String, CertificateDataElement> data) {
        this.data = data;
    }

    public ResourceLinkDTO[] getLinks() {
        return links;
    }

    public void setLinks(ResourceLinkDTO[] links) {
        this.links = links;
    }
}
