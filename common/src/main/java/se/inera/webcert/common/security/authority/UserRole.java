package se.inera.webcert.common.security.authority;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Magnus Ekstrand on 27/08/15.
 */
public enum UserRole {


    ROLE_VARDADMINISTRATOR ("Vårdadministratör", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD ("Vårdadministratör - djupintegrerad", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_VARDADMINISTRATOR_UTHOPP ("Vårdadministratör - uthopp", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_LAKARE ("Läkare", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_LAKARE_DJUPINTEGRERAD ("Läkare - djupintegrerad", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_LAKARE_UTHOPP ("Läkare - uthopp", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_PRIVATLAKARE ("Privatläkare", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_TANDLAKARE ("Tandläkare", "fk7263");

    private final String text;
    private Set<String> authorizedIntygsTyper;

    private UserRole(String text, String ... intygsTyper) {
        this.text = text;
        authorizedIntygsTyper = new HashSet<>();

        for(String authorizedIntygsTyp : intygsTyper) {
            authorizedIntygsTyper.add(authorizedIntygsTyp);
        }
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name().equals(otherName);
    }

    public boolean equalsText(String otherText) {
        return (otherText == null) ? false : text.equals(otherText);
    }

    
    public Set<String> getAuthorizedIntygsTyper() {
        return authorizedIntygsTyper;
    }

    public String toString() {
        return this.text;
    }

}
