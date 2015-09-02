package se.inera.webcert.common.model;

/**
 * Created by Magnus Ekstrand on 27/08/15.
 */
public enum UserRoles {

    ROLE_VARDADMINISTRATOR ("Vårdadministratör"),
    ROLE_LAKARE ("Läkare"),
    ROLE_LAKARE_DJUPINTEGRERAD ("Läkare - djupintegrerad"),
    ROLE_LAKARE_UTHOPP ("Läkare - uthopp"),
    ROLE_PRIVATLAKARE ("Privatläkare"),
    ROLE_TANDLAKARE ("Tandläkare");

    private final String name;

    private UserRoles(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }

}
