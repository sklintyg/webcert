package se.inera.auth.eleg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Service;

import se.inera.webcert.hsa.model.AuthenticationMethod;


/**
 * Created by eriklupander on 2015-08-24.
 */
@Service
public class ElegAuthenticationMethodResolverImpl implements ElegAuthenticationMethodResolver {

    private static final Logger log = LoggerFactory.getLogger(ElegAuthenticationMethodResolverImpl.class);

    @Autowired(required = false)
    ElegAuthenticationAttributeHelper elegAuthenticationAttributeHelper;


    @Override
    public AuthenticationMethod resolveAuthenticationMethod(SAMLCredential samlCredential) {
        String loginMethod = elegAuthenticationAttributeHelper.getAttribute(samlCredential, CgiElegAssertion.LOGIN_METHOD);

        if (loginMethod == null || loginMethod.trim().length() == 0) {
            throw new IllegalArgumentException("Cannot process SAML ticket for e-leg. Null or empty LoginMethod attribute on Assertion. Must be one of ccp8,ccp10,ccp11,ccp12 or ccp13");
        }

        return resolveAuthenticationMethod(loginMethod);
    }

    private AuthenticationMethod resolveAuthenticationMethod(String loginMethod) {

        ElegLoginMethod loginMethodEnum = null;
        try {
            loginMethodEnum = ElegLoginMethod.valueOf(loginMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Cannot resolve AuthenticationMethod from SAML attribute 'LoginMethod': " + loginMethod);
            throw new IllegalArgumentException("Could not parse AuthenticationMethod from SAML attribute 'LoginMethod': " + loginMethod);
        }
        switch(loginMethodEnum) {
            case CCP2:
            case CCP8:
                return AuthenticationMethod.NET_ID;

            case CCP10:
            case CCP12:
                return AuthenticationMethod.BANK_ID;

            case CCP11:
            case CCP13:
                return AuthenticationMethod.MOBILT_BANK_ID;
            default:
                break;
        }
        throw new IllegalArgumentException("Could not parse AuthenticationMethod from SAML attribute 'LoginMethod': " + loginMethod);
    }
}
