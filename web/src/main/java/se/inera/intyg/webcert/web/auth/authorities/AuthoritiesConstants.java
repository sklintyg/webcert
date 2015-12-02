package se.inera.intyg.webcert.web.auth.authorities;

/**
 * Created by mango on 25/11/15.
 */
public final class AuthoritiesConstants {

    private AuthoritiesConstants() {
    }

    // Titles, a.k.a 'legitimerad yrkesgrupp', has a coding system governing these titles. See:
    // HSA Innehåll Legitimerad yrkesgrupp
    // http://www.inera.se/TJANSTER--PROJEKT/HSA/Dokument/HSA-kodverk/
    public static final String TITLE_LAKARE = "Läkare";
    public static final String TITLE_TANDLAKARE = "Tandläkare";

    // Title codes, a.k.a 'befattningskod', has a coding system governing these codes. See:
    // HSA Innehåll Befattning
    // http://www.inera.se/TJANSTER--PROJEKT/HSA/Dokument/HSA-kodverk/
    public static final String TITLECODE_AT_LAKARE = "204010";

    // Known roles (these roles are copied from authorities.yaml which is the master authorities configuration)
    public static final String ROLE_LAKARE = "LAKARE";
    public static final String ROLE_PRIVATLAKARE = "PRIVATLAKARE";
    public static final String ROLE_TANDLAKARE = "TANDLAKARE";
    public static final String ROLE_ADMIN = "VARDADMINISTRATOR";

    // Known privileges (these privileges are copied from authorities.yaml which is the master authorities configuration)
    // Note: not all privileges are mapped, only the ones actually used in beckend
    public static final String PRIVILEGE_SIGNERA_INTYG = "SIGNERA_INTYG";
    public static final String PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA = "BESVARA_KOMPLETTERINGSFRAGA";

}
