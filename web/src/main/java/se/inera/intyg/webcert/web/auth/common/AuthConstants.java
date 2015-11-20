package se.inera.intyg.webcert.web.auth.common;

/**
 * Defines some constant strings related to Security, SAML or other auth mechanisms.
 *
 * Created by eriklupander on 2015-10-13.
 */
public final class AuthConstants {

    private AuthConstants() {
    }

    public static final String ALIAS_SITHS = "defaultAlias";
    public static final String ALIAS_ELEG = "eleg";

    public static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
    public static final String SPRING_SECURITY_SAVED_REQUEST_KEY = "SPRING_SECURITY_SAVED_REQUEST";

    public static final String FAKE_AUTHENTICATION_SITHS_CONTEXT_REF = "urn:inera:webcert:siths:fake";
    public static final String FAKE_AUTHENTICATION_ELEG_CONTEXT_REF = "urn:inera:webcert:eleg:fake";

    public static final String URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT = "urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient";
    public static final String URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI = "urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI";
}
