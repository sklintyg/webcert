package se.inera.intyg.webcert.specifications.spec.util

class WsClientFixture extends se.inera.intyg.common.specifications.spec.util.WsClientFixture {

	private final static String LOGICAL_ADDRESS = "5565594230"

    public WsClientFixture() {
        super(LOGICAL_ADDRESS, System.getProperty("webcert.baseUrl"))
    }
    
    public WsClientFixture(String address) {
        super(address, System.getProperty("webcert.baseUrl"))
    }
    
}

