package se.inera.certificate.spec

import se.inera.certificate.spec.util.WsClientFixture
import se.inera.intyg.insuranceprocess.healthreporting.setconsent.rivtabp20.v1.SetConsentResponderInterface
import se.inera.intyg.insuranceprocess.healthreporting.setconsentresponder.v1.SetConsentRequestType
import se.inera.intyg.insuranceprocess.healthreporting.setconsentresponder.v1.SetConsentResponseType

public class Samtycke extends WsClientFixture {

	private SetConsentResponderInterface setConsentResponder

	static String serviceUrl = System.getProperty("service.setConsentUrl")

	public Samtycke() {
        super()
    }

    public Samtycke(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
		String url = serviceUrl ? serviceUrl : baseUrl + "set-consent/v1.0"
		setConsentResponder = createClient(SetConsentResponderInterface.class, url)
	}

	String personnr
	private boolean samtycke

	public void setSamtycke(String value) {
		if (value != null && value.equalsIgnoreCase("ja")) {
			samtycke = true
		} else {
			samtycke = false
		}
	}

	public void execute() {
		SetConsentRequestType setConsentParameters = new SetConsentRequestType()
		setConsentParameters.personnummer = personnr
		setConsentParameters.consentGiven = samtycke
		SetConsentResponseType setConsentResponse = setConsentResponder.setConsent(logicalAddress, setConsentParameters)
	}

}
