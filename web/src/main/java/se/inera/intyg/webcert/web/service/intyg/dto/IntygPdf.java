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

package se.inera.intyg.webcert.web.service.intyg.dto;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

public class IntygPdf {

    private final byte[] pdfData;

    private final String filename;

    public IntygPdf(byte[] pdfData, String filename) {
        notNull(pdfData, "'pdfData' must not be null");
        hasText(filename, "'filename' must not be empty");
        this.pdfData = pdfData;
        this.filename = filename;
    }

    public byte[] getPdfData() {
        return pdfData;
    }

    public String getFilename() {
        return filename;
    }
}
