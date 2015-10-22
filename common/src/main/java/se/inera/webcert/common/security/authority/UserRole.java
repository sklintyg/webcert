package se.inera.webcert.common.security.authority;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Created by Magnus Ekstrand on 27/08/15.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserRole {

    ROLE_VARDADMINISTRATOR ("Vårdadministratör", "fk7263", "ts-bas", "ts-diabetes", "sjukpenning", "sjukersattning"),
    ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD ("Vårdadministratör", "fk7263", "ts-bas", "ts-diabetes", "sjukpenning", "sjukersattning"),
    ROLE_VARDADMINISTRATOR_UTHOPP ("Vårdadministratör", "fk7263", "ts-bas", "ts-diabetes", "sjukpenning", "sjukersattning"),
    ROLE_LAKARE ("Läkare", "fk7263", "ts-bas", "ts-diabetes", "sjukpenning", "sjukersattning"),
    ROLE_LAKARE_DJUPINTEGRERAD ("Läkare", "fk7263", "ts-bas", "ts-diabetes", "sjukpenning", "sjukersattning"),
    ROLE_LAKARE_UTHOPP ("Läkare", "fk7263", "ts-bas", "ts-diabetes", "sjukpenning", "sjukersattning"),
    ROLE_PRIVATLAKARE ("Privatläkare", "fk7263", "ts-bas", "ts-diabetes", "sjukpenning", "sjukersattning"),
    ROLE_TANDLAKARE ("Tandläkare", "fk7263");

    private String text;
    private Set<String> authorizedIntygsTyper;

    UserRole(String text, String... intygsTyper) {
        this.text = text;
        authorizedIntygsTyper = new HashSet<>();

        Collections.addAll(authorizedIntygsTyper, intygsTyper);
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
     */
    public String getName() {
        return this.text;
    }

    public String toString() {
        return this.text;
    }

}
