package se.inera.webcert.spec.web;

public class Browser {

    public void stäng() {
        geb.Browser.drive {
        }.quit()
    }
}
