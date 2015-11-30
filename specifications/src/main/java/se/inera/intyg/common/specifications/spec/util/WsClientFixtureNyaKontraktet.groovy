package se.inera.certificate.spec.util

class WsClientFixtureNyaKontraktet extends WsClientFixture {

    public WsClientFixtureNyaKontraktet() {
		super()
        nyaKontraktet = true
	}

	public WsClientFixtureNyaKontraktet(String address) {
		super(address)
        nyaKontraktet = true
	}

}
