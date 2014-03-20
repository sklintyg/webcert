package se.inera.certificate.mc2wc.schema;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CollectingErrorHandler implements ErrorHandler {

    private static String NEWLINE = System.getProperty("line.separator");

    private static String MSG_TEMPLATE = "{2} @ line {0}: {1}";

    private static String WARNING = "Warning";
    private static String ERROR = "Error";
    private static String FATAL_ERROR = "Fatal error";

    private List<String> validationErrors;

    private List<String> validationWarnings;

    public CollectingErrorHandler() {
        this.validationErrors = new ArrayList<String>();
        this.validationWarnings = new ArrayList<String>();
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        addValidationWarning(exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        addValidationError(exception, ERROR);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        addValidationError(exception, FATAL_ERROR);
    }

    private void addValidationWarning(SAXParseException ex) {
        String errMsg = formatMessage(ex, WARNING);
        validationWarnings.add(errMsg);
    }

    private void addValidationError(SAXParseException ex, String type) {
        String errMsg = formatMessage(ex, type);
        validationErrors.add(errMsg);
    }

    private String formatMessage(SAXParseException ex, String type) {
        return MessageFormat.format(MSG_TEMPLATE, ex.getLineNumber(), ex.getMessage(), type);
    }

    public boolean hasValidationErrors() {
        return !(this.validationErrors.isEmpty());
    }

    public boolean hasValidationWarnings() {
        return !(this.validationWarnings.isEmpty());
    }

    public String getValidationErrorsAsString() {
        return assembleStrings(this.validationErrors);
    }

    public String getValidationWarningsAsString() {
        return assembleStrings(this.validationWarnings);
    }

    private String assembleStrings(List<String> strings) {
        StringBuilder sb = new StringBuilder();

        Iterator<String> iter = strings.iterator();

        while (iter.hasNext()) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append(NEWLINE);
            }
        }

        return sb.toString();
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public List<String> getValidationWarnings() {
        return validationWarnings;
    }
}
