/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.common.specifications.spec

import se.inera.intyg.common.specifications.spec.util.WsClientFixture
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
