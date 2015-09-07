package se.inera.webcert.common.model;

/**
 * Created by Magnus Ekstrand on 27/08/15.
 */
public enum UserPrivileges {

    PRIVILEGE_SKRIVA_INTYG ("Skriva intyg"),
    PRIVILEGE_SIGNERA_INTYG ("Signera intyg"),
    PRIVILEGE_MAKULERA_INTYG ("Makulera intyg"),
    PRIVILEGE_KOPIERA_INTYG ("Kopiera intyg"),
    PRIVILEGE_VIDAREBEFORDRA_UTKAST ("Vidarebefordra utkast"),
    PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR ("Vidarebefordra frågasvar"),
    PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA ("Besvara fråga om komplettering");

    private final String name;

    private UserPrivileges(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }

}
