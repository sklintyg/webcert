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
