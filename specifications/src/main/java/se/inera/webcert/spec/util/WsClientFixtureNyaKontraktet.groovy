package se.inera.webcert.spec.util

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType

class WsClientFixtureNyaKontraktet extends WsClientFixture {

    boolean nyaKontraktet = true
    
	public WsClientFixtureNyaKontraktet() {
		super();
	}

	public WsClientFixtureNyaKontraktet(String address) {
		super(address)
	}

}
