package se.inera.intyg.webcert.specifications.spec.notification_sender
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture
import se.inera.intyg.webcert.specifications.spec.util.WebcertRestUtils

class VerifieraNotifiering {

    String id 
    String kod
	long timeOut = 4000;
	private boolean handelseSkapad = false;
	

    public void execute() {
        handelseSkapad = WebcertRestUtils.awaitNotification(id, kod, timeOut);
    } 

	public boolean handelseSkapad() {
		return handelseSkapad;
	}
}
