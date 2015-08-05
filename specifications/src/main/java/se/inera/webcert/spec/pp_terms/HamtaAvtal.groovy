package se.inera.webcert.spec.pp_terms

import se.inera.webcert.spec.util.WsClientFixture
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerterms.v1.rivtabp21.GetPrivatePractitionerTermsResponderInterface
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsType

/**
 * Created by eriklupander on 2015-08-05.
 */
class HamtaAvtal extends WsClientFixture {

    private def avtalResponder

    String avtalText
    String avtalVersion

    public HamtaAvtal() {
        super()
    }

    public HamtaAvtal(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String serviceUrl = System.getProperty("service.getPrivatePractitionerTermsUrl")
        String url = serviceUrl ? serviceUrl : baseUrl + "services/get-private-practitioner-terms/v1.0"
        avtalResponder = createClient(GetPrivatePractitionerTermsResponderInterface.class, url)
    }

    public String resultat() {

        GetPrivatePractitionerTermsType request = new GetPrivatePractitionerTermsType();

        def response = avtalResponder.getPrivatePractitionerTerms("", request);

        avtalText = response.avtal.avtalText
        avtalVersion = response.avtal.avtalVersion

        response.resultCode
    }
}
