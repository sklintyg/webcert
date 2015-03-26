package se.inera.webcert.spec.util

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType

class WsClientFixtureNyaKontraktet extends WsClientFixture {

	public WsClientFixtureNyaKontraktet() {
		super();
        nyaKontraktet = true
	}

	public WsClientFixtureNyaKontraktet(String address) {
		super(address)
        nyaKontraktet = true
	}

}
