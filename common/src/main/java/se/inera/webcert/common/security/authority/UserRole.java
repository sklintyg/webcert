package se.inera.webcert.common.security.authority;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Created by Magnus Ekstrand on 27/08/15.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserRole {

    ROLE_VARDADMINISTRATOR ("Vårdadministratör", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD ("Vårdadministratör - djupintegrerad", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_VARDADMINISTRATOR_UTHOPP ("Vårdadministratör - uthopp", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_LAKARE ("Läkare", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_LAKARE_DJUPINTEGRERAD ("Läkare - djupintegrerad", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_LAKARE_UTHOPP ("Läkare - uthopp", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_PRIVATLAKARE ("Privatläkare", "fk7263", "ts-bas", "ts-diabetes"),
    ROLE_TANDLAKARE ("Tandläkare", "fk7263");

    private String text;
    private Set<String> authorizedIntygsTyper;

    private UserRole(String text, String ... intygsTyper) {
        this.text = text;
        authorizedIntygsTyper = new HashSet<>();

        for (String authorizedIntygsTyp : intygsTyper) {
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


    public String text() {
        return this.text;
    }

    /**
     * This is only to make serialization of enum using Jackson Shape.OBJECT to work properly.
     * @return
     */
    public String getName() {
        return this.text;
    }

    public String toString() {
        return this.text;
    }

}
