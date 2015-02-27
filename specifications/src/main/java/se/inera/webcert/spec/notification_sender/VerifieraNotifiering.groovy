package se.inera.webcert.spec.notification_sender
import se.inera.webcert.spec.util.RestClientFixture
import se.inera.webcert.spec.util.WebcertRestUtils

class VerifieraNotifiering extends RestClientFixture {

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
