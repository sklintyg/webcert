package se.inera.webcert.spec.pp_terms

import se.inera.webcert.spec.util.WsClientFixture
import se.riv.infrastructure.directory.privatepractitioner.approveprivatepractitionerterms.v1.rivtabp21.ApprovePrivatePractitionerTermsResponderInterface
import se.riv.infrastructure.directory.privatepractitioner.approveprivatepractitionertermsresponder.v1.ApprovePrivatePractitionerTermsType
import se.riv.infrastructure.directory.privatepractitioner.terms.types.v1.PersonId
import se.riv.infrastructure.directory.privatepractitioner.terms.v1.AvtalGodkannandeType

/**
 * Created by eriklupander on 2015-08-05.
 */
class GodkannAvtal extends WsClientFixture {

    private def avtalResponder

    String personId
    Integer avtalVersion

    public GodkannAvtal() {
        super()
    }

    public GodkannAvtal(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String serviceUrl = System.getProperty("service.approvePrivatePractitionerTermsUrl")
        String url = serviceUrl ? serviceUrl : baseUrl + "services/approve-private-practitioner-terms/v1.0"
        avtalResponder = createClient(ApprovePrivatePractitionerTermsResponderInterface.class, url)
    }

    public String resultat() {

        PersonId personIdType = new PersonId()
        personIdType.setExtension(personId)

        AvtalGodkannandeType avtalGodkannandeType = new AvtalGodkannandeType();
        avtalGodkannandeType.setAvtalVersion(avtalVersion)
        avtalGodkannandeType.setPersonId(personIdType)

        ApprovePrivatePractitionerTermsType request = new ApprovePrivatePractitionerTermsType();
        request.setAvtalGodkannande(avtalGodkannandeType)

        def response = avtalResponder.approvePrivatePractitionerTerms("", request);

        response.resultCode
    }
}
