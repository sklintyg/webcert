package se.inera.intyg.webcert.web.integration.validator;

/**
 * Created by eriklupander on 2017-09-11.
 */
public final class IntygsTypToInternal {

    private IntygsTypToInternal() {

    }

    public static String convertToInternalIntygsTyp(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Cannot pass null code to internal intygstyp converter.");
        }

        switch (code.toLowerCase()) {
            case "tstrk1007":
                return "ts-bas";
            case "tstrk1031":
                return "ts-diabetes";
            default:
                return code.toLowerCase();
        }
    }

}
