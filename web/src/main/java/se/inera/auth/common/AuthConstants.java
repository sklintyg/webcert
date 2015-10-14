package se.inera.auth.common;

/**
 * Defines some constant strings related to Security, SAML or other auth mechanisms.
 *
 * Created by eriklupander on 2015-10-13.
 */
public interface AuthConstants {
    String ALIAS_SITHS = "defaultAlias";
    String ALIAS_ELEG = "eleg";

    String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
    String SPRING_SECURITY_SAVED_REQUEST_KEY = "SPRING_SECURITY_SAVED_REQUEST";

    String FAKE_AUTHENTICATION_SITHS_CONTEXT_REF = "urn:inera:webcert:siths:fake";
    String FAKE_AUTHENTICATION_ELEG_CONTEXT_REF = "urn:inera:webcert:eleg:fake";

    String URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_TLSCLIENT = "urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient";
    String URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI = "urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI";
}
