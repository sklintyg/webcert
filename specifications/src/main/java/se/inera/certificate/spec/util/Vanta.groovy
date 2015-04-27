package se.inera.certificate.spec.util

class Vanta {

    Vanta(int n, String tidsEnhet) {
        int millis = n;
        if (tidsEnhet?.startsWith("sekund")) {
            millis = n * 1000
        }
        Thread.sleep(millis);
    }
}
