package se.inera.certificate.mc2wc.schema;

import java.util.Iterator;
import java.util.List;

public class SchemaValidatorException extends Exception {

    private static final long serialVersionUID = 3138286317278101287L;

    public static String NL = System.getProperty("line.separator");

    private List<String> validationErrors;

    public SchemaValidatorException() {
        super();
    }

    public SchemaValidatorException(String message) {
        super(message);
    }

    public SchemaValidatorException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public SchemaValidatorException(Throwable cause) {
        super(cause);
    }

    public SchemaValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {

        if (this.validationErrors == null) {
            return super.getMessage();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(super.getMessage());

        Iterator<String> iter = this.validationErrors.iterator();

        if (iter.hasNext()) {
            sb.append(NL);
        }

        while (iter.hasNext()) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append(NL);
            }
        }

        return sb.toString();
    }
}
