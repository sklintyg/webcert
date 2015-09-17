package se.inera.webcert.common.security.authority;

/**
 * Created by Magnus Ekstrand on 27/08/15.
 */
public enum UserRole {

    ROLE_VARDADMINISTRATOR ("Vårdadministratör"),
    ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD ("Vårdadministratör - djupintegrerad"),
    ROLE_VARDADMINISTRATOR_UTHOPP ("Vårdadministratör - uthopp"),
    ROLE_LAKARE ("Läkare"),
    ROLE_LAKARE_DJUPINTEGRERAD ("Läkare - djupintegrerad"),
    ROLE_LAKARE_UTHOPP ("Läkare - uthopp"),
    ROLE_PRIVATLAKARE ("Privatläkare"),
    ROLE_TANDLAKARE ("Tandläkare");

    private final String text;

    private UserRole(String text) {
        this.text = text;
    }

    public boolean equalsText(String otherText) {
        return (otherText == null) ? false : text.equals(otherText);
    }

    public String toString() {
        return this.text;
    }

}
