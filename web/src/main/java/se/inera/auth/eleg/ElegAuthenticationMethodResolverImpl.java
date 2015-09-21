package se.inera.auth.eleg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Service;

import se.inera.webcert.hsa.model.AuthenticationMethod;

/**
 * Created by eriklupander on 2015-08-24.
 */
@Service
public class ElegAuthenticationMethodResolverImpl implements ElegAuthenticationMethodResolver {

    @Value("${cgi.saml.issuer.netid.commonNames}")
    private String netIdIssuerCommonNames;

    @Value("${cgi.saml.issuer.bankid.commonNames}")
    private String bankIdIssuerCommonNames;

    @Autowired(required = false)
    ElegAuthenticationAttributeHelper elegAuthenticationAttributeHelper;

    @Override
    public AuthenticationMethod resolveAuthenticationMethod(SAMLCredential samlCredential) {
        String issuerCommonName = elegAuthenticationAttributeHelper.getAttribute(samlCredential, CgiElegAssertion.UTFARDARE_CA_NAMN_ATTRIBUTE);

        return resolveAuthenticationMethod(issuerCommonName);
    }

    private AuthenticationMethod resolveAuthenticationMethod(String issuerCommonName) {
        if (issuerCommonName == null) {
            throw new IllegalArgumentException("Cannot resolve issuerCommonName of SAML ticket.");
        }

        if (netIdIssuerCommonNames == null || netIdIssuerCommonNames.trim().length() == 0) {
            throw new IllegalStateException("Cannot resolve AuthenticationMethod from issuer CN, no value set for NetID identifiers using property 'cgi.saml.issuer.netid.commonNames'");
        }

        if (bankIdIssuerCommonNames == null || bankIdIssuerCommonNames.trim().length() == 0) {
            throw new IllegalStateException("Cannot resolve AuthenticationMethod from issuer CN, no value set for BankID identifiers using property 'cgi.saml.issuer.bankid.commonNames'");
        }


        boolean matchesNetId = commonNameMatchesMethod(issuerCommonName, netIdIssuerCommonNames);
        boolean matchesBankId = commonNameMatchesMethod(issuerCommonName, bankIdIssuerCommonNames);
        if (matchesNetId && matchesBankId) {
            throw new IllegalArgumentException("Cannot resolve AuthenticationMethod, both NetID and BankID match specified issuer '" + issuerCommonName + "'");
        }
        else if (matchesNetId) {
            return AuthenticationMethod.NET_ID;
        } else if (matchesBankId) {
            return AuthenticationMethod.BANK_ID;
        } else {
            throw new IllegalArgumentException("Cannot resolve AuthenticationMethod, issuer '" + issuerCommonName + "' does not match any configured issuer.");
        }
    }

    private boolean commonNameMatchesMethod(String issuerCommonName, String matchString) {
        String[] arr = matchString.split("\\|");
        for (int a = 0; a < arr.length; a++) {
            if (issuerCommonName.toLowerCase().trim().contains(arr[a].toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
