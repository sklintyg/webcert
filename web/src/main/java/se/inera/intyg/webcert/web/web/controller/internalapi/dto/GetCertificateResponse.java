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
package se.inera.intyg.webcert.web.web.controller.internalapi.dto;

import java.util.List;
import java.util.Objects;
import se.inera.intyg.common.support.facade.model.Certificate;

public class GetCertificateResponse {

    private Certificate certificate;
    private List<ResourceLinkDTO> links;

    public static GetCertificateResponse create(Certificate certificate) {
        final var getCertificateResponse = new GetCertificateResponse();
        getCertificateResponse.setCertificate(certificate);
        return getCertificateResponse;
    }

    public static GetCertificateResponse create(Certificate certificate, List<ResourceLinkDTO> links) {
        final var getCertificateResponse = new GetCertificateResponse();
        getCertificateResponse.setCertificate(certificate);
        getCertificateResponse.setLinks(links);
        return getCertificateResponse;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public List<ResourceLinkDTO> getLinks() {
        return links;
    }

    public void setLinks(List<ResourceLinkDTO> links) {
        this.links = links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final GetCertificateResponse that = (GetCertificateResponse) o;
        return Objects.equals(certificate, that.certificate) && Objects.equals(links, that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificate, links);
    }

    @Override
    public String toString() {
        return "GetCertificateResponse{"
            + "certificate=" + certificate
            + ", links=" + links
            + '}';
    }
}