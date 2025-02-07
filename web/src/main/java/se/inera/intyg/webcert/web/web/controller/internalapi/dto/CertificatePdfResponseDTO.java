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

package se.inera.intyg.webcert.web.web.controller.internalapi.dto;

import java.util.Arrays;
import java.util.Objects;

public class CertificatePdfResponseDTO {

    private String filename;
    private byte[] pdfData;

    public static CertificatePdfResponseDTO create(String filename, byte[] pdfData) {
        final var printCertificateResponse = new CertificatePdfResponseDTO();
        printCertificateResponse.setFilename(filename);
        printCertificateResponse.setPdfData(pdfData);
        return printCertificateResponse;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getPdfData() {
        return pdfData;
    }

    public void setPdfData(byte[] pdfData) {
        this.pdfData = pdfData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CertificatePdfResponseDTO that = (CertificatePdfResponseDTO) o;
        return Objects.equals(filename, that.filename) && Arrays.equals(pdfData, that.pdfData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(filename);
        result = 31 * result + Arrays.hashCode(pdfData);
        return result;
    }

    @Override
    public String toString() {
        return "PrintCertificateResponseDTO{"
            + "filename='" + filename + '\''
            + ", pdfData=" + Arrays.toString(pdfData)
            + '}';
    }
}
