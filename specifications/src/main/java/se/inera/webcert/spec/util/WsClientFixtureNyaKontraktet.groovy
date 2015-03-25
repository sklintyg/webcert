package se.inera.webcert.spec.util

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType

class WsClientFixtureNyaKontraktet extends WsClientFixture {

	public WsClientFixtureNyaKontraktet() {
		super();
	}

	public WsClientFixtureNyaKontraktet(String address) {
		logicalAddress.setValue(address)
	}

    @Override
	def resultAsString(response) {
        String result = null
		if (response) {
	        switch (response.result.resultCode) {
	            case ResultCodeType.OK:
	                result = response.result.resultCode.toString()
                    break
	            case ResultCodeType.INFO:
	                result = "[${response.result.resultCode.toString()}] - ${response.result.resultText}"
                    break
                case ResultCodeType.ERROR:
					result = "[${response.result.errorId.toString()}] - ${response.result.resultText}"
                    break
	        }
		}
		return result
	}

}
