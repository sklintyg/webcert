package se.inera.intyg.webcert.web.auth.eleg;

/**
 * Defines enumerations for a number of known  (and undocumented)
 *
 * <saml2:Attribute Name="LoginMethod">
 *
 * from the CGI funktionstjänster SAML IdP documentation.
 *
 * Created by eriklupander on 2015-09-23.
 */
public enum ElegLoginMethod {
    /** Legacy NetID production. */
    CCP1,

    /** Legacy NetID test. */
    CCP2,

    /** NetID. */
    CCP8,

    /** BankID. */
    CCP10,

    /** Mobilt BankID. */
    CCP11,

    /** BankID  (Telia, future). */
    CCP12,

    /** Mobilt BankID (Telia, future). */
    CCP13
}
